package net.silvertide.mortal_boons.boon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record HeldBoon(ResourceLocation boonId, int tier) {
    public static final Codec<HeldBoon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("boon_id").forGetter(HeldBoon::boonId),
            Codec.intRange(1, 4).fieldOf("tier").forGetter(HeldBoon::tier)
    ).apply(instance, HeldBoon::new));
}
