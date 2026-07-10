package net.silvertide.mortal_boons.boon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;

public record Boon(ResourceLocation id, int weight, int minTier, int maxTier,
                   List<TierScaledAttribute> tierScaledAttributes, List<AbilityGrantSpec> abilityGrants) {

    public record TierScaledAttribute(Holder<Attribute> attribute, LeveledValue<Double> amount,
                                      AttributeModifier.Operation operation) {
        public static final Codec<TierScaledAttribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(TierScaledAttribute::attribute),
                LeveledValue.codec(Codec.DOUBLE).fieldOf("amount").forGetter(TierScaledAttribute::amount),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(TierScaledAttribute::operation)
        ).apply(instance, TierScaledAttribute::new));

        public AttributeGrant resolve(int tier) {
            return new AttributeGrant(attribute, amount.resolve(tier), operation);
        }
    }

    public record AbilityGrantSpec(ResourceLocation abilityId, LeveledValue<Integer> abilityLevel) {
        private static final LeveledValue<Integer> LEVEL_ONE = new LeveledValue<>(List.of(1));

        public static final Codec<AbilityGrantSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("ability").forGetter(AbilityGrantSpec::abilityId),
                LeveledValue.codec(Codec.INT).optionalFieldOf("level", LEVEL_ONE).forGetter(AbilityGrantSpec::abilityLevel)
        ).apply(instance, AbilityGrantSpec::new));
    }

    public record Definition(int weight, int minTier, int maxTier,
                             List<TierScaledAttribute> tierScaledAttributes,
                             List<AbilityGrantSpec> abilityGrants) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("weight", 10).forGetter(Definition::weight),
                Codec.intRange(1, 4).optionalFieldOf("min_tier", 1).forGetter(Definition::minTier),
                Codec.intRange(1, 4).optionalFieldOf("max_tier", 4).forGetter(Definition::maxTier),
                TierScaledAttribute.CODEC.listOf().optionalFieldOf("attribute_grants", List.of())
                        .forGetter(Definition::tierScaledAttributes),
                AbilityGrantSpec.CODEC.listOf().optionalFieldOf("ability_grants", List.of())
                        .forGetter(Definition::abilityGrants)
        ).apply(instance, Definition::new));

        public Boon toBoon(ResourceLocation id) {
            return new Boon(id, weight, minTier, maxTier, tierScaledAttributes, abilityGrants);
        }
    }

    public List<AttributeGrant> attributeGrants(int tier) {
        return tierScaledAttributes.stream().map(scaled -> scaled.resolve(tier)).toList();
    }

    public boolean requiresPlayerAbilities() {
        return tierScaledAttributes.isEmpty() && !abilityGrants.isEmpty();
    }

    public Component displayName() {
        return Component.translatable("boon." + id.getNamespace() + "." + id.getPath());
    }
}
