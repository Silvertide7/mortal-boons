package net.silvertide.mortal_boons.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.AttributeGrant;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.mortal_boons.boon.BoonManager;
import net.silvertide.mortal_boons.boon.HeldBoon;
import net.silvertide.mortal_boons.block.BoonAltarBlock;
import net.silvertide.mortal_boons.boon.Tier;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.data.BoonAttachments;
import net.silvertide.mortal_boons.roll.RollManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record AltarScreenPayload(int altarPower, boolean candlesLit, boolean beaconBelow,
                                 AllowedActions allowedActions, BlockPos altarPos,
                                 List<SlotDisplay> slots) implements CustomPacketPayload {
    public static final Type<AltarScreenPayload> TYPE = new Type<>(MortalBoons.id("altar_screen"));

    public record AllowedActions(boolean reroll, boolean reforge, boolean forsake) {
        public static final StreamCodec<RegistryFriendlyByteBuf, AllowedActions> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, AllowedActions::reroll,
                ByteBufCodecs.BOOL, AllowedActions::reforge,
                ByteBufCodecs.BOOL, AllowedActions::forsake,
                AllowedActions::new);
    }

    public record SlotDisplay(Component title, List<Component> lines, int tier) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.TRUSTED_STREAM_CODEC, SlotDisplay::title,
                ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list()), SlotDisplay::lines,
                ByteBufCodecs.VAR_INT, SlotDisplay::tier,
                SlotDisplay::new);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AltarScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AltarScreenPayload::altarPower,
            ByteBufCodecs.BOOL, AltarScreenPayload::candlesLit,
            ByteBufCodecs.BOOL, AltarScreenPayload::beaconBelow,
            AllowedActions.STREAM_CODEC, AltarScreenPayload::allowedActions,
            BlockPos.STREAM_CODEC, AltarScreenPayload::altarPos,
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarScreenPayload::slots,
            AltarScreenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static AltarScreenPayload snapshot(ServerPlayer player, BlockPos altarPos) {
        boolean candlesLit = BoonAltarBlock.hasCandleRing(player.serverLevel(), altarPos);
        boolean beaconBelow = BoonAltarBlock.hasBeaconBelow(player.serverLevel(), altarPos);
        int altarPower = BoonAltarBlock.altarPowerAt(player.serverLevel(), altarPos);
        AllowedActions allowedActions = new AllowedActions(BoonConfig.ALLOW_REROLL.get(),
                BoonConfig.ALLOW_REFORGE.get(), BoonConfig.ALLOW_FORSAKE.get());
        List<HeldBoon> heldBoons = player.getData(BoonAttachments.BOON_DATA).getHeldBoons();
        List<SlotDisplay> slots = new ArrayList<>(RollManager.MAX_BOONS);
        for (int slotIndex = 0; slotIndex < RollManager.MAX_BOONS; slotIndex++) {
            if (slotIndex < heldBoons.size()) {
                slots.add(heldSlot(heldBoons.get(slotIndex)));
            } else if (slotIndex < altarPower) {
                slots.add(new SlotDisplay(Component.translatable("mortal_boons.screen.empty"), List.of(), 0));
            } else {
                slots.add(new SlotDisplay(Component.translatable("mortal_boons.screen.locked"), List.of(), 0));
            }
        }
        return new AltarScreenPayload(altarPower, candlesLit, beaconBelow, allowedActions, altarPos, slots);
    }

    private static SlotDisplay heldSlot(HeldBoon held) {
        Optional<Boon> boonLookup = BoonManager.get(held.boonId());
        Tier tier = Tier.fromLevel(held.tier());
        if (boonLookup.isEmpty()) {
            return new SlotDisplay(Component.literal(held.boonId().toString()), List.of(), held.tier());
        }
        Boon boon = boonLookup.get();
        Component title = Component.empty()
                .append(boon.displayName())
                .append(" (")
                .append(tier.displayName())
                .append(")")
                .withStyle(tier.color());
        List<Component> lines = new ArrayList<>();
        boon.attributeGrants(held.tier()).forEach(grant -> lines.add(describeAttribute(grant)));
        boon.abilityGrants().forEach(spec -> lines.add(Component.translatable("mortal_boons.screen.ability_grant",
                spec.abilityId().toString(), spec.abilityLevel().resolve(held.tier()))));
        return new SlotDisplay(title, lines, held.tier());
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
