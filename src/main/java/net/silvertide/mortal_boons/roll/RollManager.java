package net.silvertide.mortal_boons.roll;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.mortal_boons.boon.BoonEffects;
import net.silvertide.mortal_boons.boon.BoonManager;
import net.silvertide.mortal_boons.boon.HeldBoon;
import net.silvertide.mortal_boons.boon.Offering;
import net.silvertide.mortal_boons.boon.OfferingManager;
import net.silvertide.mortal_boons.boon.Tier;
import net.silvertide.mortal_boons.compat.player_abilities.PlayerAbilitiesIntegration;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.data.BoonAttachments;
import net.silvertide.mortal_boons.data.BoonData;
import net.silvertide.mortal_boons.menu.FatestoneMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RollManager {
    public static final int MAX_BOONS = 3;
    private static final int TICKS_PER_SECOND = 20;
    private static final int TIER_COUNT = 4;

    private RollManager() {
    }

    public static int xpLevelCost(int lifetimeRollCount) {
        return BoonConfig.ROLL_BASE_XP_LEVEL_COST.get()
                + BoonConfig.ROLL_XP_LEVEL_COST_PER_ROLL.get() * lifetimeRollCount;
    }

    public static boolean roll(ServerPlayer player, int slotCap) {
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        long gameTime = player.level().getGameTime();
        if (boonData.getHeldBoons().size() >= slotCap) {
            player.displayClientMessage(Component.translatable("mortal_boons.roll.slots_full"), false);
            return false;
        }
        int cost = xpLevelCost(boonData.getLifetimeRollCount());
        if (rejectIfOnCooldown(player, gameTime) || rejectIfCannotAfford(player, cost)) {
            return false;
        }
        ItemStack offeringStack = ItemStack.EMPTY;
        Offering offering = null;
        if (OfferingManager.offeringsEnabled() && player.containerMenu instanceof FatestoneMenu fatestoneMenu) {
            offeringStack = fatestoneMenu.getOfferingItem();
            offering = OfferingManager.matching(offeringStack).orElse(null);
            if (offering == null && !player.isCreative() && OfferingManager.offeringRequired()) {
                player.displayClientMessage(Component.translatable("mortal_boons.roll.requires_offering"), false);
                return false;
            }
        }
        Map<Integer, List<Boon>> candidatesByTier = candidatesByTier(boonData);
        int tier = rollTier(player.getRandom(), candidatesByTier, offering);
        if (tier < 0) {
            player.displayClientMessage(Component.translatable("mortal_boons.roll.none_available"), false);
            return false;
        }
        Boon boon = pickBoon(player.getRandom(), candidatesByTier.get(tier), tier, offering);
        chargeXpAndStartCooldown(player, gameTime, cost);
        if (offering != null && !player.isCreative()) {
            offeringStack.shrink(1);
        }
        boonData.addBoon(new HeldBoon(boon.id(), tier));
        boonData.incrementLifetimeRollCount();
        BoonEffects.apply(player, boon, tier);
        playFatestoneEffects(player, SoundEvents.ENCHANTMENT_TABLE_USE, tier);
        player.displayClientMessage(Component.translatable("mortal_boons.roll.success",
                boon.displayName(), Tier.fromLevel(tier).displayName()), false);
        return true;
    }

    public static boolean reforge(ServerPlayer player, int slotIndex) {
        if (!BoonConfig.ALLOW_REFORGE.get()) {
            player.displayClientMessage(Component.translatable("mortal_boons.reforge.disabled"), false);
            return false;
        }
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        long gameTime = player.level().getGameTime();
        List<HeldBoon> heldBoons = boonData.getHeldBoons();
        if (heldBoons.size() < BoonConfig.REFORGE_REQUIRED_BOONS.get()) {
            player.displayClientMessage(Component.translatable("mortal_boons.reforge.requires_boons",
                    BoonConfig.REFORGE_REQUIRED_BOONS.get()), false);
            return false;
        }
        if (slotIndex < 0 || slotIndex >= heldBoons.size()) {
            rejectInvalidSlot(player);
            return false;
        }
        HeldBoon current = heldBoons.get(slotIndex);
        Optional<Boon> boonLookup = BoonManager.get(current.boonId());
        if (boonLookup.isEmpty()) {
            rejectInvalidSlot(player);
            return false;
        }
        int cost = BoonConfig.REFORGE_XP_LEVEL_COST.get();
        if (rejectIfOnCooldown(player, gameTime) || rejectIfCannotAfford(player, cost)) {
            return false;
        }
        Boon boon = boonLookup.get();
        int newTier = reforgeTier(player.getRandom(), boon);
        chargeXpAndStartCooldown(player, gameTime, cost);
        boonData.replaceBoonAt(slotIndex, new HeldBoon(boon.id(), newTier));
        BoonEffects.apply(player, boon, newTier);
        playFatestoneEffects(player, SoundEvents.ANVIL_USE, newTier);
        player.displayClientMessage(Component.translatable("mortal_boons.reforge.success",
                boon.displayName(), Tier.fromLevel(newTier).displayName()), false);
        return true;
    }

    public static boolean reroll(ServerPlayer player, int slotIndex) {
        if (!BoonConfig.ALLOW_REROLL.get()) {
            player.displayClientMessage(Component.translatable("mortal_boons.reroll.disabled"), false);
            return false;
        }
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        long gameTime = player.level().getGameTime();
        List<HeldBoon> heldBoons = boonData.getHeldBoons();
        if (heldBoons.size() < BoonConfig.REROLL_REQUIRED_BOONS.get()) {
            player.displayClientMessage(Component.translatable("mortal_boons.reroll.requires_boons",
                    BoonConfig.REROLL_REQUIRED_BOONS.get()), false);
            return false;
        }
        if (slotIndex < 0 || slotIndex >= heldBoons.size()) {
            rejectInvalidSlot(player);
            return false;
        }
        int cost = BoonConfig.REROLL_XP_LEVEL_COST.get();
        if (rejectIfOnCooldown(player, gameTime) || rejectIfCannotAfford(player, cost)) {
            return false;
        }
        Map<Integer, List<Boon>> candidatesByTier = candidatesByTier(boonData);
        int newTier = rollTier(player.getRandom(), candidatesByTier, null);
        if (newTier < 0) {
            player.displayClientMessage(Component.translatable("mortal_boons.roll.none_available"), false);
            return false;
        }
        HeldBoon removed = heldBoons.get(slotIndex);
        Component removedName = BoonManager.get(removed.boonId())
                .map(Boon::displayName)
                .orElse(Component.literal(removed.boonId().toString()));
        Boon newBoon = pickBoon(player.getRandom(), candidatesByTier.get(newTier), newTier, null);
        chargeXpAndStartCooldown(player, gameTime, cost);
        BoonManager.get(removed.boonId()).ifPresent(oldBoon ->
                BoonEffects.remove(player, oldBoon, removed.tier()));
        boonData.replaceBoonAt(slotIndex, new HeldBoon(newBoon.id(), newTier));
        BoonEffects.apply(player, newBoon, newTier);
        playFatestoneEffects(player, SoundEvents.EVOKER_CAST_SPELL, newTier);
        player.displayClientMessage(Component.translatable("mortal_boons.reroll.success",
                removedName, newBoon.displayName(), Tier.fromLevel(newTier).displayName()), false);
        return true;
    }

    public static boolean forsake(ServerPlayer player, int slotIndex) {
        if (!BoonConfig.ALLOW_FORSAKE.get()) {
            player.displayClientMessage(Component.translatable("mortal_boons.forsake.disabled"), false);
            return false;
        }
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        List<HeldBoon> heldBoons = boonData.getHeldBoons();
        if (slotIndex < 0 || slotIndex >= heldBoons.size()) {
            rejectInvalidSlot(player);
            return false;
        }
        int cost = BoonConfig.FORSAKE_XP_LEVEL_COST.get();
        if (rejectIfCannotAfford(player, cost)) {
            return false;
        }
        if (!player.isCreative()) {
            player.giveExperienceLevels(-cost);
        }
        HeldBoon removed = heldBoons.get(slotIndex);
        Component removedName = BoonManager.get(removed.boonId())
                .map(Boon::displayName)
                .orElse(Component.literal(removed.boonId().toString()));
        BoonManager.get(removed.boonId()).ifPresent(boon ->
                BoonEffects.remove(player, boon, removed.tier()));
        boonData.removeBoonAt(slotIndex);
        player.displayClientMessage(Component.translatable("mortal_boons.forsake.success", removedName), false);
        return true;
    }

    private static void rejectInvalidSlot(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("mortal_boons.action.invalid_slot"), false);
    }

    private static boolean rejectIfOnCooldown(ServerPlayer player, long gameTime) {
        long cooldownEndGameTime = player.getData(BoonAttachments.ROLL_COOLDOWN_END_GAME_TIME);
        if (player.isCreative() || gameTime >= cooldownEndGameTime) {
            return false;
        }
        long ticksRemaining = cooldownEndGameTime - gameTime;
        long secondsRemaining = (ticksRemaining + TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;
        player.displayClientMessage(Component.translatable("mortal_boons.roll.on_cooldown", secondsRemaining), false);
        return true;
    }

    private static boolean rejectIfCannotAfford(ServerPlayer player, int cost) {
        if (player.isCreative() || player.experienceLevel >= cost) {
            return false;
        }
        player.displayClientMessage(Component.translatable("mortal_boons.roll.not_enough_xp", cost), false);
        return true;
    }

    private static void playFatestoneEffects(ServerPlayer player, SoundEvent sound, int tier) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.BLOCKS, 1.0F, 0.7F + 0.15F * tier);
        level.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.5, player.getZ(),
                20 + tier * 15, 0.6, 0.6, 0.6, 0.8);
        if (tier == 4) {
            level.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    24, 0.4, 0.7, 0.4, 0.05);
        }
    }

    private static void chargeXpAndStartCooldown(ServerPlayer player, long gameTime, int cost) {
        if (!player.isCreative()) {
            player.giveExperienceLevels(-cost);
        }
        player.setData(BoonAttachments.ROLL_COOLDOWN_END_GAME_TIME,
                gameTime + (long) BoonConfig.ROLL_COOLDOWN_SECONDS.get() * TICKS_PER_SECOND);
    }

    private static Map<Integer, List<Boon>> candidatesByTier(BoonData boonData) {
        Map<Integer, List<Boon>> candidatesByTier = new HashMap<>();
        for (Boon boon : BoonManager.all()) {
            if (boonData.holds(boon.id())
                    || (boon.requiresPlayerAbilities() && !PlayerAbilitiesIntegration.isLoaded())) {
                continue;
            }
            for (int tier = boon.minTier(); tier <= boon.maxTier(); tier++) {
                if (boon.weight(tier) > 0) {
                    candidatesByTier.computeIfAbsent(tier, key -> new ArrayList<>()).add(boon);
                }
            }
        }
        return candidatesByTier;
    }

    private static int rollTier(RandomSource random, Map<Integer, List<Boon>> candidatesByTier,
                                @Nullable Offering offering) {
        int tier = rollTierWithModifiers(random, candidatesByTier, offering);
        return tier < 0 && offering != null ? rollTierWithModifiers(random, candidatesByTier, null) : tier;
    }

    private static int rollTierWithModifiers(RandomSource random, Map<Integer, List<Boon>> candidatesByTier,
                                             @Nullable Offering offering) {
        List<? extends Integer> configuredWeights = BoonConfig.TIER_WEIGHTS.get();
        double[] weights = new double[TIER_COUNT];
        double totalWeight = 0;
        for (int tier = 1; tier <= TIER_COUNT; tier++) {
            double weight = candidatesByTier.containsKey(tier) ? baseTierWeight(configuredWeights, tier) : 0;
            if (offering != null) {
                weight *= offering.tierMultiplier(tier);
                if (offering.minTier().isPresent() && tier < offering.minTier().get()) {
                    weight = 0;
                }
            }
            weights[tier - 1] = weight;
            totalWeight += weight;
        }
        if (totalWeight <= 0) {
            return -1;
        }
        double pick = random.nextDouble() * totalWeight;
        for (int tier = 1; tier <= TIER_COUNT; tier++) {
            pick -= weights[tier - 1];
            if (pick < 0) {
                return tier;
            }
        }
        for (int tier = TIER_COUNT; tier >= 1; tier--) {
            if (weights[tier - 1] > 0) {
                return tier;
            }
        }
        return -1;
    }

    private static double baseTierWeight(List<? extends Integer> configuredWeights, int tier) {
        return tier - 1 < configuredWeights.size() ? Math.max(0, configuredWeights.get(tier - 1)) : 1;
    }

    private static Boon pickBoon(RandomSource random, List<Boon> pool, int tier, @Nullable Offering offering) {
        Boon picked = pickBoonWeighted(random, pool, tier, offering);
        return picked != null ? picked : pickBoonWeighted(random, pool, tier, null);
    }

    @Nullable
    private static Boon pickBoonWeighted(RandomSource random, List<Boon> pool, int tier,
                                         @Nullable Offering offering) {
        double[] weights = new double[pool.size()];
        double totalWeight = 0;
        for (int poolIndex = 0; poolIndex < pool.size(); poolIndex++) {
            Boon boon = pool.get(poolIndex);
            double weight = boon.weight(tier);
            if (offering != null) {
                for (ResourceLocation type : boon.types()) {
                    weight *= Math.max(0, offering.typeWeightMultiplier().getOrDefault(type, 1.0));
                }
            }
            weights[poolIndex] = weight;
            totalWeight += weight;
        }
        if (totalWeight <= 0) {
            return null;
        }
        double pick = random.nextDouble() * totalWeight;
        for (int poolIndex = 0; poolIndex < pool.size(); poolIndex++) {
            pick -= weights[poolIndex];
            if (pick < 0) {
                return pool.get(poolIndex);
            }
        }
        return pool.getLast();
    }

    private static int reforgeTier(RandomSource random, Boon boon) {
        List<Integer> availableTiers = new ArrayList<>();
        for (int tier = boon.minTier(); tier <= boon.maxTier(); tier++) {
            if (boon.weight(tier) > 0) {
                availableTiers.add(tier);
            }
        }
        if (availableTiers.isEmpty()) {
            return boon.minTier();
        }
        List<? extends Integer> configuredWeights = BoonConfig.TIER_WEIGHTS.get();
        double[] weights = new double[availableTiers.size()];
        double totalWeight = 0;
        for (int tierIndex = 0; tierIndex < availableTiers.size(); tierIndex++) {
            weights[tierIndex] = baseTierWeight(configuredWeights, availableTiers.get(tierIndex));
            totalWeight += weights[tierIndex];
        }
        if (totalWeight <= 0) {
            return availableTiers.get(random.nextInt(availableTiers.size()));
        }
        double pick = random.nextDouble() * totalWeight;
        for (int tierIndex = 0; tierIndex < availableTiers.size(); tierIndex++) {
            pick -= weights[tierIndex];
            if (pick < 0) {
                return availableTiers.get(tierIndex);
            }
        }
        return availableTiers.getLast();
    }
}
