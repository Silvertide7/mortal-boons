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
import java.util.Optional;

public record Boon(ResourceLocation id, LeveledValue<Integer> weight, int minTier, int maxTier,
                   List<TierScaledAttribute> tierScaledAttributes, List<AbilityGrantSpec> abilityGrants,
                   Optional<LeveledValue<ResourceLocation>> icon, List<ResourceLocation> types) {

    private static final LeveledValue<Integer> DEFAULT_WEIGHT = new LeveledValue<>(List.of(10));

    public int weight(int tier) {
        return weight.resolve(tier);
    }

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

    public record Definition(LeveledValue<Integer> weight, int minTier, int maxTier,
                             List<TierScaledAttribute> tierScaledAttributes,
                             List<AbilityGrantSpec> abilityGrants,
                             Optional<LeveledValue<ResourceLocation>> icon,
                             List<ResourceLocation> types) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LeveledValue.codec(Codec.INT).optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(Definition::weight),
                Codec.intRange(1, 4).optionalFieldOf("min_tier", 1).forGetter(Definition::minTier),
                Codec.intRange(1, 4).optionalFieldOf("max_tier", 4).forGetter(Definition::maxTier),
                TierScaledAttribute.CODEC.listOf().optionalFieldOf("attribute_grants", List.of())
                        .forGetter(Definition::tierScaledAttributes),
                AbilityGrantSpec.CODEC.listOf().optionalFieldOf("ability_grants", List.of())
                        .forGetter(Definition::abilityGrants),
                LeveledValue.codec(ResourceLocation.CODEC).optionalFieldOf("icon").forGetter(Definition::icon),
                ResourceLocation.CODEC.listOf().optionalFieldOf("types", List.of()).forGetter(Definition::types)
        ).apply(instance, Definition::new));

        public Boon toBoon(ResourceLocation id) {
            return new Boon(id, weight, minTier, maxTier, tierScaledAttributes, abilityGrants, icon, types);
        }
    }

    public List<AttributeGrant> attributeGrants(int tier) {
        return tierScaledAttributes.stream().map(scaled -> scaled.resolve(tier)).toList();
    }

    public boolean requiresPlayerAbilities() {
        return tierScaledAttributes.isEmpty() && !abilityGrants.isEmpty();
    }

    public Optional<ResourceLocation> iconTexture(int tier) {
        if (icon.isPresent()) {
            return Optional.of(icon.get().resolve(tier));
        }
        if (!abilityGrants.isEmpty()) {
            ResourceLocation abilityId = abilityGrants.getFirst().abilityId();
            return Optional.of(ResourceLocation.fromNamespaceAndPath(abilityId.getNamespace(),
                    "textures/ability/" + abilityId.getPath() + ".png"));
        }
        return Optional.empty();
    }

    public Component displayName() {
        return Component.translatable("boon." + id.getNamespace() + "." + id.getPath());
    }
}
