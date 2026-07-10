package net.silvertide.mortal_boons.boon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.silvertide.mortal_boons.MortalBoons;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Boons {
    private static final Map<ResourceLocation, Boon> REGISTRY = new LinkedHashMap<>();

    public static final Boon IRON_BLOODED = register(new AttributeBoon(
            MortalBoons.id("iron_blooded"), 10, 1, 4,
            List.of(new AttributeBoon.TierScaledAttribute(
                    Attributes.ARMOR,
                    new LeveledValue<>(List.of(3.0, 4.0, 5.0, 6.0)),
                    AttributeModifier.Operation.ADD_VALUE))));

    public static final Boon FLEETFOOT = register(new AttributeBoon(
            MortalBoons.id("fleetfoot"), 10, 1, 4,
            List.of(new AttributeBoon.TierScaledAttribute(
                    Attributes.MOVEMENT_SPEED,
                    new LeveledValue<>(List.of(0.10, 0.15, 0.20, 0.25)),
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE))));

    private Boons() {
    }

    private static Boon register(Boon boon) {
        if (REGISTRY.putIfAbsent(boon.id(), boon) != null) {
            throw new IllegalStateException("Duplicate boon id " + boon.id());
        }
        return boon;
    }

    public static Optional<Boon> get(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Collection<Boon> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
