package net.silvertide.mortal_boons.network;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.block.FatestoneBlock;
import net.silvertide.mortal_boons.boon.AttributeGrant;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.mortal_boons.boon.BoonManager;
import net.silvertide.mortal_boons.boon.HeldBoon;
import net.silvertide.mortal_boons.boon.Tier;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.data.BoonAttachments;
import net.silvertide.mortal_boons.roll.RollManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FatestoneScreenPayload(int power, boolean candlesLit, boolean beaconBelow,
                                     AllowedActions allowedActions, BlockPos pos,
                                     List<SlotDisplay> slots) implements CustomPacketPayload {
    public static final Type<FatestoneScreenPayload> TYPE = new Type<>(MortalBoons.id("fatestone_screen"));

    public record AllowedActions(boolean reroll, boolean reforge, boolean forsake) {
        public static final StreamCodec<RegistryFriendlyByteBuf, AllowedActions> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, AllowedActions::reroll,
                ByteBufCodecs.BOOL, AllowedActions::reforge,
                ByteBufCodecs.BOOL, AllowedActions::forsake,
                AllowedActions::new);
    }

    public record SlotDisplay(Component title, List<Component> lines, int tier, Optional<ResourceLocation> icon,
                              Component name, List<Component> effects) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.TRUSTED_STREAM_CODEC, SlotDisplay::title,
                ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list()), SlotDisplay::lines,
                ByteBufCodecs.VAR_INT, SlotDisplay::tier,
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), SlotDisplay::icon,
                ComponentSerialization.TRUSTED_STREAM_CODEC, SlotDisplay::name,
                ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list()), SlotDisplay::effects,
                SlotDisplay::new);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, FatestoneScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FatestoneScreenPayload::power,
            ByteBufCodecs.BOOL, FatestoneScreenPayload::candlesLit,
            ByteBufCodecs.BOOL, FatestoneScreenPayload::beaconBelow,
            AllowedActions.STREAM_CODEC, FatestoneScreenPayload::allowedActions,
            BlockPos.STREAM_CODEC, FatestoneScreenPayload::pos,
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), FatestoneScreenPayload::slots,
            FatestoneScreenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static FatestoneScreenPayload snapshot(ServerPlayer player, BlockPos pos) {
        boolean candlesLit = FatestoneBlock.hasCandleRing(player.serverLevel(), pos);
        boolean beaconBelow = FatestoneBlock.hasBeaconBelow(player.serverLevel(), pos);
        int power = FatestoneBlock.powerAt(player.serverLevel(), pos);
        AllowedActions allowedActions = new AllowedActions(BoonConfig.ALLOW_REROLL.get(),
                BoonConfig.ALLOW_REFORGE.get(), BoonConfig.ALLOW_FORSAKE.get());
        List<HeldBoon> heldBoons = player.getData(BoonAttachments.BOON_DATA).getHeldBoons();
        List<SlotDisplay> slots = new ArrayList<>(RollManager.MAX_BOONS);
        for (int slotIndex = 0; slotIndex < RollManager.MAX_BOONS; slotIndex++) {
            if (slotIndex < heldBoons.size()) {
                slots.add(heldSlot(heldBoons.get(slotIndex)));
            } else if (slotIndex < power) {
                slots.add(new SlotDisplay(Component.translatable("mortal_boons.screen.empty"), List.of(), 0,
                        Optional.empty(), Component.empty(), List.of()));
            } else {
                slots.add(new SlotDisplay(Component.translatable("mortal_boons.screen.locked"), List.of(), 0,
                        Optional.empty(), Component.empty(), List.of()));
            }
        }
        return new FatestoneScreenPayload(power, candlesLit, beaconBelow, allowedActions, pos, slots);
    }

    private static SlotDisplay heldSlot(HeldBoon held) {
        Optional<Boon> boonLookup = BoonManager.get(held.boonId());
        Tier tier = Tier.fromLevel(held.tier());
        if (boonLookup.isEmpty()) {
            Component rawId = Component.literal(held.boonId().toString());
            return new SlotDisplay(rawId, List.of(), held.tier(), Optional.empty(), rawId, List.of());
        }
        Boon boon = boonLookup.get();
        Component title = Component.empty()
                .append(boon.displayName())
                .append(" (")
                .append(tier.displayName())
                .append(")")
                .withStyle(tier.color());
        List<Component> lines = new ArrayList<>();
        List<Component> effects = new ArrayList<>();
        boon.attributeGrants(held.tier()).forEach(grant -> {
            Component described = describeAttribute(grant);
            lines.add(described);
            effects.add(described);
        });
        boon.abilityGrants().forEach(spec -> {
            Component abilityName = abilityName(spec.abilityId());
            effects.add(abilityName);
            lines.add(Component.translatable("mortal_boons.screen.ability_grant",
                    abilityName, spec.abilityLevel().resolve(held.tier())));
            String descriptionKey = abilityNameKey(spec.abilityId()) + ".description";
            if (Language.getInstance().has(descriptionKey)) {
                lines.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
            }
        });
        return new SlotDisplay(title, lines, held.tier(), boon.iconTexture(held.tier()), boon.displayName(),
                effects);
    }

    private static Component abilityName(ResourceLocation abilityId) {
        String nameKey = abilityNameKey(abilityId);
        return Language.getInstance().has(nameKey)
                ? Component.translatable(nameKey)
                : Component.literal(abilityId.toString());
    }

    private static String abilityNameKey(ResourceLocation abilityId) {
        return "ability." + abilityId.getNamespace() + "." + abilityId.getPath().replace('/', '.');
    }

    private static Component describeAttribute(AttributeGrant grant) {
        boolean isFlat = grant.operation() == AttributeModifier.Operation.ADD_VALUE;
        double displayAmount = isFlat ? grant.amount() : grant.amount() * 100;
        String prefix = grant.amount() >= 0 ? "+" : "";
        String suffix = isFlat ? " " : "% ";
        return Component.literal(prefix + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount) + suffix)
                .append(Component.translatable(grant.attribute().value().getDescriptionId()));
    }
}
