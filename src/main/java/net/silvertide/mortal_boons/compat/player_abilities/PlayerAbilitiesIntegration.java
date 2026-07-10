package net.silvertide.mortal_boons.compat.player_abilities;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.silvertide.mortal_boons.boon.Boon;

public final class PlayerAbilitiesIntegration {
    private static final String PLAYER_ABILITIES_MOD_ID = "player_abilities";

    private PlayerAbilitiesIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(PLAYER_ABILITIES_MOD_ID);
    }

    public static void applyBoon(ServerPlayer player, Boon boon, int tier) {
        if (isLoaded() && !boon.abilityGrants().isEmpty()) {
            PlayerAbilitiesCompat.applyBoon(player, boon, tier);
        }
    }

    public static void removeBoon(ServerPlayer player, Boon boon) {
        if (isLoaded() && !boon.abilityGrants().isEmpty()) {
            PlayerAbilitiesCompat.removeBoon(player, boon);
        }
    }

    public static void revokeAllGrants(ServerPlayer player) {
        if (isLoaded()) {
            PlayerAbilitiesCompat.revokeAllGrants(player);
        }
    }
}
