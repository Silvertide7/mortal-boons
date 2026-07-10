package net.silvertide.mortal_boons.boon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum Tier {
    IRON(ChatFormatting.GRAY),
    GOLD(ChatFormatting.GOLD),
    DIAMOND(ChatFormatting.AQUA),
    NETHERITE(ChatFormatting.DARK_PURPLE);

    private final ChatFormatting color;

    Tier(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting color() {
        return color;
    }

    public Component displayName() {
        return Component.translatable("mortal_boons.tier." + name().toLowerCase(Locale.ROOT));
    }

    public static Tier fromLevel(int level) {
        return switch (level) {
            case 1 -> IRON;
            case 2 -> GOLD;
            case 3 -> DIAMOND;
            case 4 -> NETHERITE;
            default -> throw new IllegalArgumentException("No boon tier for level " + level);
        };
    }
}
