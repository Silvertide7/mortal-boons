package net.silvertide.mortal_boons.boon;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeGrant(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
}
