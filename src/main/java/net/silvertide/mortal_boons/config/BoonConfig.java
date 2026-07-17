package net.silvertide.mortal_boons.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BoonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ROLL_BASE_XP_LEVEL_COST = BUILDER
            .comment("XP levels the first roll costs")
            .defineInRange("rollBaseXpLevelCost", 5, 0, 10000);

    public static final ModConfigSpec.IntValue ROLL_XP_LEVEL_COST_PER_ROLL = BUILDER
            .comment("Extra XP levels added to the cost for each previous roll this life")
            .defineInRange("rollXpLevelCostPerRoll", 5, 0, 10000);

    public static final ModConfigSpec.IntValue ROLL_COOLDOWN_TICKS = BUILDER
            .comment("Ticks a player must wait between Fatestone actions (20 ticks = 1 second)")
            .defineInRange("rollCooldownTicks", 6000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue REFORGE_XP_LEVEL_COST = BUILDER
            .comment("XP levels a reforge (tier reroll) costs")
            .defineInRange("reforgeXpLevelCost", 5, 0, 10000);

    public static final ModConfigSpec.IntValue REROLL_XP_LEVEL_COST = BUILDER
            .comment("XP levels a reroll (boon replacement) costs")
            .defineInRange("rerollXpLevelCost", 10, 0, 10000);

    public static final ModConfigSpec.BooleanValue REROLL_REQUIRES_FULL_SLOTS = BUILDER
            .comment("Whether all 3 boon slots must be filled before a boon can be rerolled")
            .define("rerollRequiresFullSlots", true);

    public static final ModConfigSpec.BooleanValue ALLOW_REROLL = BUILDER
            .comment("Whether boons can be rerolled (replaced with a new random boon)")
            .define("allowReroll", true);

    public static final ModConfigSpec.BooleanValue ALLOW_REFORGE = BUILDER
            .comment("Whether boons can be reforged (tier rerolled)")
            .define("allowReforge", true);

    public static final ModConfigSpec.BooleanValue ALLOW_FORSAKE = BUILDER
            .comment("Whether boons can be forsaken (removed, leaving the slot empty)")
            .define("allowForsake", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BoonConfig() {
    }
}
