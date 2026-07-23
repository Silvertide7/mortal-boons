package net.silvertide.mortal_boons.network;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
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
import net.silvertide.mortal_boons.boon.BoonTypeManager;
import net.silvertide.mortal_boons.boon.HeldBoon;
import net.silvertide.mortal_boons.boon.OfferingManager;
import net.silvertide.mortal_boons.boon.Tier;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.data.BoonAttachments;
import net.silvertide.mortal_boons.data.BoonData;
import net.silvertide.mortal_boons.roll.RollManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FatestoneScreenPayload(int power, boolean candlesLit, boolean beaconBelow,
                                     AllowedActions allowedActions, int temptFateXpCost,
                                     boolean offeringsEnabled, boolean offeringRequired, int revision,
                                     BlockPos pos, List<SlotDisplay> slots) implements CustomPacketPayload {
    public static final Type<FatestoneScreenPayload> TYPE = new Type<>(MortalBoons.id("fatestone_screen"));

    public record AllowedActions(boolean reroll, boolean reforge, boolean forsake) {
        public static final StreamCodec<RegistryFriendlyByteBuf, AllowedActions> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, AllowedActions::reroll,
                ByteBufCodecs.BOOL, AllowedActions::reforge,
                ByteBufCodecs.BOOL, AllowedActions::forsake,
                AllowedActions::new);
    }

    public record SlotDisplay(Component title, List<Component> lines, int tier, Optional<ResourceLocation> icon,
                              Component name, Component types, List<Component> effects) {
        private static final StreamCodec<RegistryFriendlyByteBuf, List<Component>> COMPONENT_LIST_STREAM_CODEC =
                ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<io.netty.buffer.ByteBuf, Optional<ResourceLocation>> ICON_STREAM_CODEC =
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional);

        public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = StreamCodec.of(
                (buf, slot) -> {
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, slot.title());
                    COMPONENT_LIST_STREAM_CODEC.encode(buf, slot.lines());
                    ByteBufCodecs.VAR_INT.encode(buf, slot.tier());
                    ICON_STREAM_CODEC.encode(buf, slot.icon());
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, slot.name());
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, slot.types());
                    COMPONENT_LIST_STREAM_CODEC.encode(buf, slot.effects());
                },
                buf -> new SlotDisplay(
                        ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf),
                        COMPONENT_LIST_STREAM_CODEC.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ICON_STREAM_CODEC.decode(buf),
                        ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf),
                        ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf),
                        COMPONENT_LIST_STREAM_CODEC.decode(buf)));
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, List<SlotDisplay>> SLOTS_STREAM_CODEC =
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, FatestoneScreenPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.power());
                ByteBufCodecs.BOOL.encode(buf, payload.candlesLit());
                ByteBufCodecs.BOOL.encode(buf, payload.beaconBelow());
                AllowedActions.STREAM_CODEC.encode(buf, payload.allowedActions());
                ByteBufCodecs.VAR_INT.encode(buf, payload.temptFateXpCost());
                ByteBufCodecs.BOOL.encode(buf, payload.offeringsEnabled());
                ByteBufCodecs.BOOL.encode(buf, payload.offeringRequired());
                ByteBufCodecs.VAR_INT.encode(buf, payload.revision());
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                SLOTS_STREAM_CODEC.encode(buf, payload.slots());
            },
            buf -> new FatestoneScreenPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    AllowedActions.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    BlockPos.STREAM_CODEC.decode(buf),
                    SLOTS_STREAM_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static FatestoneScreenPayload snapshot(ServerPlayer player, BlockPos pos, int revision) {
        boolean candlesLit = FatestoneBlock.hasCandleRing(player.serverLevel(), pos);
        boolean beaconBelow = FatestoneBlock.hasBeaconBelow(player.serverLevel(), pos);
        int power = FatestoneBlock.powerAt(player.serverLevel(), pos);
        BoonData boonData = player.getData(BoonAttachments.BOON_DATA);
        int temptFateXpCost = RollManager.xpLevelCost(boonData.getLifetimeRollCount());
        List<HeldBoon> heldBoons = boonData.getHeldBoons();
        AllowedActions allowedActions = new AllowedActions(
                BoonConfig.ALLOW_REROLL.get() && power >= BoonConfig.REROLL_REQUIRED_POWER.get()
                        && heldBoons.size() >= BoonConfig.REROLL_REQUIRED_BOONS.get(),
                BoonConfig.ALLOW_REFORGE.get() && power >= BoonConfig.REFORGE_REQUIRED_POWER.get()
                        && heldBoons.size() >= BoonConfig.REFORGE_REQUIRED_BOONS.get(),
                BoonConfig.ALLOW_FORSAKE.get());
        List<SlotDisplay> slots = new ArrayList<>(RollManager.MAX_BOONS);
        for (int slotIndex = 0; slotIndex < RollManager.MAX_BOONS; slotIndex++) {
            if (slotIndex < heldBoons.size()) {
                slots.add(heldSlot(heldBoons.get(slotIndex)));
            } else if (slotIndex < power) {
                slots.add(new SlotDisplay(Component.translatable("mortal_boons.screen.empty"), List.of(), 0,
                        Optional.empty(), Component.empty(), Component.empty(), List.of()));
            } else {
                slots.add(new SlotDisplay(Component.translatable("mortal_boons.screen.locked"), List.of(), 0,
                        Optional.empty(), Component.empty(), Component.empty(), List.of()));
            }
        }
        return new FatestoneScreenPayload(power, candlesLit, beaconBelow, allowedActions, temptFateXpCost,
                OfferingManager.offeringsEnabled(), OfferingManager.offeringRequired(), revision, pos, slots);
    }

    private static SlotDisplay heldSlot(HeldBoon held) {
        Optional<Boon> boonLookup = BoonManager.get(held.boonId());
        Tier tier = Tier.fromLevel(held.tier());
        if (boonLookup.isEmpty()) {
            Component rawId = Component.literal(held.boonId().toString());
            return new SlotDisplay(rawId, List.of(), held.tier(), Optional.empty(), rawId, Component.empty(),
                    List.of());
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
        boon.types().forEach(type -> BoonTypeManager.description(type).ifPresent(description ->
                lines.add(Component.literal(description).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
        return new SlotDisplay(title, lines, held.tier(), boon.iconTexture(held.tier()), boon.displayName(),
                composeTypes(boon.types()), effects);
    }

    private static Component composeTypes(List<ResourceLocation> types) {
        if (types.isEmpty()) {
            return Component.empty();
        }
        MutableComponent typesLine = Component.empty();
        for (int typeIndex = 0; typeIndex < types.size(); typeIndex++) {
            if (typeIndex > 0) {
                typesLine.append(", ");
            }
            typesLine.append(BoonTypeManager.displayName(types.get(typeIndex)));
        }
        return typesLine;
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
