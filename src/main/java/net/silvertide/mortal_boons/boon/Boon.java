package net.silvertide.mortal_boons.boon;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface Boon {
    ResourceLocation id();

    int weight();

    int minTier();

    int maxTier();

    List<AttributeGrant> attributeGrants(int tier);
}
