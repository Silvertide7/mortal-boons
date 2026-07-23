package net.silvertide.mortal_boons.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.silvertide.mortal_boons.boon.OfferingMode;

import java.util.List;

public final class BoonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ROLL_BASE_XP_LEVEL_COST = BUILDER
            .comment("XP levels the first roll costs")
            .defineInRange("rollBaseXpLevelCost", 5, 0, 10000);

    public static final ModConfigSpec.IntValue ROLL_XP_LEVEL_COST_PER_ROLL = BUILDER
            .comment("Extra XP levels added to the cost for each previous roll this life")
            .defineInRange("rollXpLevelCostPerRoll", 5, 0, 10000);

    public static final ModConfigSpec.IntValue ROLL_COOLDOWN_SECONDS = BUILDER
            .comment("Seconds a player must wait between Fatestone actions")
            .defineInRange("rollCooldownSeconds", 300, 0, Integer.MAX_VALUE / 20);

    public static final ModConfigSpec.IntValue REFORGE_XP_LEVEL_COST = BUILDER
            .comment("XP levels a reforge (tier reroll) costs")
            .defineInRange("reforgeXpLevelCost", 5, 0, 10000);

    public static final ModConfigSpec.IntValue REROLL_XP_LEVEL_COST = BUILDER
            .comment("XP levels a reroll (boon replacement) costs")
            .defineInRange("rerollXpLevelCost", 10, 0, 10000);

    public static final ModConfigSpec.IntValue FORSAKE_XP_LEVEL_COST = BUILDER
            .comment("XP levels forsaking a boon costs")
            .defineInRange("forsakeXpLevelCost", 5, 0, 10000);

    public static final ModConfigSpec.IntValue REROLL_REQUIRED_POWER = BUILDER
            .comment("Fatestone power (1-3) required to reroll a boon")
            .defineInRange("rerollRequiredPower", 2, 1, 3);

    public static final ModConfigSpec.IntValue REFORGE_REQUIRED_POWER = BUILDER
            .comment("Fatestone power (1-3) required to reforge a boon")
            .defineInRange("reforgeRequiredPower", 3, 1, 3);

    public static final ModConfigSpec.IntValue REROLL_REQUIRED_BOONS = BUILDER
            .comment("Held boons required before rerolling unlocks")
            .defineInRange("rerollRequiredBoons", 3, 0, 3);

    public static final ModConfigSpec.IntValue REFORGE_REQUIRED_BOONS = BUILDER
            .comment("Held boons required before reforging unlocks")
            .defineInRange("reforgeRequiredBoons", 3, 0, 3);

    public static final ModConfigSpec.BooleanValue ALLOW_REROLL = BUILDER
            .comment("Whether boons can be rerolled (replaced with a new random boon)")
            .define("allowReroll", true);

    public static final ModConfigSpec.BooleanValue ALLOW_REFORGE = BUILDER
            .comment("Whether boons can be reforged (tier rerolled)")
            .define("allowReforge", true);

    public static final ModConfigSpec.BooleanValue ALLOW_FORSAKE = BUILDER
            .comment("Whether boons can be forsaken (removed, leaving the slot empty)")
            .define("allowForsake", false);

    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> TIER_WEIGHTS = BUILDER
            .comment("Base weights for rolling Iron, Gold, Diamond, Netherite tiers")
            .defineList("tierWeights", List.of(40, 30, 20, 10),
                    entry -> entry instanceof Integer weight && weight >= 0);

    public static final ModConfigSpec.EnumValue<OfferingMode> OFFERING_MODE = BUILDER
            .comment("Item offerings when tempting fate: NONE disables the slot, OPTIONAL lets offerings sway the roll, REQUIRED demands one")
            .defineEnum("offeringMode", OfferingMode.OPTIONAL);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BoonConfig() {
    }
}
