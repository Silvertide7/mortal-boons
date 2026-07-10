package net.silvertide.mortal_boons.roll;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.mortal_boons.boon.BoonEffects;
import net.silvertide.mortal_boons.boon.BoonManager;
import net.silvertide.mortal_boons.boon.HeldBoon;
import net.silvertide.mortal_boons.boon.Tier;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.data.BoonAttachments;
import net.silvertide.mortal_boons.data.BoonData;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public final class RollManager {
    public static final int MAX_BOONS = 3;
    private static final int TICKS_PER_SECOND = 20;

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
        if (rejectIfOnCooldown(player, boonData, gameTime) || rejectIfCannotAfford(player, cost)) {
            return false;
        }
        Optional<Boon> rolledBoon = pickWeightedExcludingHeld(player.getRandom(), boonData);
        if (rolledBoon.isEmpty()) {
            player.displayClientMessage(Component.translatable("mortal_boons.roll.none_available"), false);
            return false;
        }
        Boon boon = rolledBoon.get();
        int tier = rollTier(player.getRandom(), boon);
        chargeXpAndStartCooldown(player, boonData, gameTime, cost);
        boonData.addBoon(new HeldBoon(boon.id(), tier));
        boonData.incrementLifetimeRollCount();
        BoonEffects.apply(player, boon, tier);
        player.displayClientMessage(Component.translatable("mortal_boons.roll.success",
                boon.displayName(), Tier.fromLevel(tier).displayName()), false);
        return true;
    }

    public static boolean reforge(ServerPlayer player) {
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        long gameTime = player.level().getGameTime();
        List<HeldBoon> heldBoons = boonData.getHeldBoons();
        List<Integer> reforgeableIndices = knownBoonIndices(heldBoons);
        if (reforgeableIndices.isEmpty()) {
            player.displayClientMessage(Component.translatable("mortal_boons.reforge.none"), false);
            return false;
        }
        int cost = BoonConfig.REFORGE_XP_LEVEL_COST.get();
        if (rejectIfOnCooldown(player, boonData, gameTime) || rejectIfCannotAfford(player, cost)) {
            return false;
        }
        int reforgedIndex = reforgeableIndices.get(player.getRandom().nextInt(reforgeableIndices.size()));
        HeldBoon current = heldBoons.get(reforgedIndex);
        Boon boon = BoonManager.get(current.boonId()).orElseThrow();
        int newTier = rollTier(player.getRandom(), boon);
        chargeXpAndStartCooldown(player, boonData, gameTime, cost);
        boonData.replaceBoonAt(reforgedIndex, new HeldBoon(boon.id(), newTier));
        BoonEffects.apply(player, boon, newTier);
        player.displayClientMessage(Component.translatable("mortal_boons.reforge.success",
                boon.displayName(), Tier.fromLevel(newTier).displayName()), false);
        return true;
    }

    public static boolean reroll(ServerPlayer player) {
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        long gameTime = player.level().getGameTime();
        List<HeldBoon> heldBoons = boonData.getHeldBoons();
        if (heldBoons.isEmpty()) {
            player.displayClientMessage(Component.translatable("mortal_boons.reroll.none"), false);
            return false;
        }
        if (BoonConfig.REROLL_REQUIRES_FULL_SLOTS.get() && heldBoons.size() < MAX_BOONS) {
            player.displayClientMessage(Component.translatable("mortal_boons.reroll.requires_full", MAX_BOONS), false);
            return false;
        }
        int cost = BoonConfig.REROLL_XP_LEVEL_COST.get();
        if (rejectIfOnCooldown(player, boonData, gameTime) || rejectIfCannotAfford(player, cost)) {
            return false;
        }
        Optional<Boon> replacementBoon = pickWeightedExcludingHeld(player.getRandom(), boonData);
        if (replacementBoon.isEmpty()) {
            player.displayClientMessage(Component.translatable("mortal_boons.roll.none_available"), false);
            return false;
        }
        int removedIndex = player.getRandom().nextInt(heldBoons.size());
        HeldBoon removed = heldBoons.get(removedIndex);
        Component removedName = BoonManager.get(removed.boonId())
                .map(Boon::displayName)
                .orElse(Component.literal(removed.boonId().toString()));
        Boon newBoon = replacementBoon.get();
        int newTier = rollTier(player.getRandom(), newBoon);
        chargeXpAndStartCooldown(player, boonData, gameTime, cost);
        BoonManager.get(removed.boonId()).ifPresent(oldBoon ->
                BoonEffects.remove(player, oldBoon, removed.tier()));
        boonData.replaceBoonAt(removedIndex, new HeldBoon(newBoon.id(), newTier));
        BoonEffects.apply(player, newBoon, newTier);
        player.displayClientMessage(Component.translatable("mortal_boons.reroll.success",
                removedName, newBoon.displayName(), Tier.fromLevel(newTier).displayName()), false);
        return true;
    }

    private static List<Integer> knownBoonIndices(List<HeldBoon> heldBoons) {
        return IntStream.range(0, heldBoons.size())
                .filter(index -> BoonManager.get(heldBoons.get(index).boonId()).isPresent())
                .boxed()
                .toList();
    }

    private static boolean rejectIfOnCooldown(ServerPlayer player, BoonData boonData, long gameTime) {
        if (player.isCreative() || boonData.isRollReady(gameTime)) {
            return false;
        }
        long ticksRemaining = boonData.getRollCooldownEndGameTime() - gameTime;
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

    private static void chargeXpAndStartCooldown(ServerPlayer player, BoonData boonData, long gameTime, int cost) {
        if (!player.isCreative()) {
            player.giveExperienceLevels(-cost);
        }
        boonData.startRollCooldown(gameTime, BoonConfig.ROLL_COOLDOWN_TICKS.get());
    }

    private static Optional<Boon> pickWeightedExcludingHeld(RandomSource random, BoonData boonData) {
        List<Boon> candidates = BoonManager.all().stream()
                .filter(boon -> !boonData.holds(boon.id()))
                .filter(boon -> boon.weight() > 0)
                .toList();
        int totalWeight = candidates.stream().mapToInt(Boon::weight).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }
        int pick = random.nextInt(totalWeight);
        for (Boon candidate : candidates) {
            pick -= candidate.weight();
            if (pick < 0) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static int rollTier(RandomSource random, Boon boon) {
        return boon.minTier() + random.nextInt(boon.maxTier() - boon.minTier() + 1);
    }
}
