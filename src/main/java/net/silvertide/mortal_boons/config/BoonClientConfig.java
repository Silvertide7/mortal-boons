package net.silvertide.mortal_boons.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BoonClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_BOON_TYPES = BUILDER
            .comment("Whether boon type names (Combat, Agility, ...) are shown on the cards")
            .define("showBoonTypes", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BoonClientConfig() {
    }
}
