package net.silvertide.mortal_boons.boon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record Offering(Ingredient item, List<Double> tierWeightMultiplier,
                       Map<ResourceLocation, Double> typeWeightMultiplier, Optional<Integer> minTier) {

    public static final Codec<Offering> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(Offering::item),
            Codec.DOUBLE.listOf(0, 4).optionalFieldOf("tier_weight_multiplier", List.of())
                    .forGetter(Offering::tierWeightMultiplier),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)
                    .optionalFieldOf("type_weight_multiplier", Map.of()).forGetter(Offering::typeWeightMultiplier),
            Codec.intRange(1, 4).optionalFieldOf("min_tier").forGetter(Offering::minTier)
    ).apply(instance, Offering::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Offering> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, Offering::item,
            ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()), Offering::tierWeightMultiplier,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.DOUBLE),
            Offering::typeWeightMultiplier,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs::optional), Offering::minTier,
            Offering::new);

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && item.test(stack);
    }

    public double tierMultiplier(int tier) {
        return tier - 1 < tierWeightMultiplier.size() ? Math.max(0, tierWeightMultiplier.get(tier - 1)) : 1.0;
    }

    public List<Component> describe() {
        List<Component> lines = new ArrayList<>();
        if (!tierWeightMultiplier.isEmpty()) {
            lines.add(Component.translatable("mortal_boons.offering.tier_bias").withStyle(ChatFormatting.GRAY));
        }
        typeWeightMultiplier.forEach((type, multiplier) -> lines.add(Component.translatable(
                "mortal_boons.offering.type_boost",
                BoonTypeManager.displayName(type),
                String.format(Locale.ROOT, "%.1f", multiplier)).withStyle(ChatFormatting.GRAY)));
        minTier.ifPresent(tier -> lines.add(Component.translatable("mortal_boons.offering.min_tier",
                Tier.fromLevel(tier).displayName()).withStyle(ChatFormatting.GRAY)));
        if (lines.isEmpty()) {
            lines.add(Component.translatable("mortal_boons.offering.requirement_only")
                    .withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
