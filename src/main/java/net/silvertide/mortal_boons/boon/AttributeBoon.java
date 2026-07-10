package net.silvertide.mortal_boons.boon;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;

public class AttributeBoon implements Boon {
    public record TierScaledAttribute(Holder<Attribute> attribute, LeveledValue<Double> amount,
                                      AttributeModifier.Operation operation) {
        public AttributeGrant resolve(int tier) {
            return new AttributeGrant(attribute, amount.resolve(tier), operation);
        }
    }

    private final ResourceLocation id;
    private final int weight;
    private final int minTier;
    private final int maxTier;
    private final List<TierScaledAttribute> tierScaledAttributes;

    public AttributeBoon(ResourceLocation id, int weight, int minTier, int maxTier,
                         List<TierScaledAttribute> tierScaledAttributes) {
        this.id = id;
        this.weight = weight;
        this.minTier = minTier;
        this.maxTier = maxTier;
        this.tierScaledAttributes = List.copyOf(tierScaledAttributes);
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public int minTier() {
        return minTier;
    }

    @Override
    public int maxTier() {
        return maxTier;
    }

    @Override
    public List<AttributeGrant> attributeGrants(int tier) {
        return tierScaledAttributes.stream().map(scaled -> scaled.resolve(tier)).toList();
    }
}
