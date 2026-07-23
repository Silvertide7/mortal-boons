package net.silvertide.mortal_boons.compat.player_abilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.player_abilities.api.Ability;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityRegistry;

final class PlayerAbilitiesCompat {
    private static final String GRANT_SOURCE_PATH_PREFIX = "boons/";

    private PlayerAbilitiesCompat() {
    }

    private static ResourceLocation grantSource(Boon boon) {
        return MortalBoons.id(GRANT_SOURCE_PATH_PREFIX + boon.id().getNamespace() + "/" + boon.id().getPath());
    }

    static void applyBoon(ServerPlayer player, Boon boon, int tier) {
        boon.abilityGrants().forEach(spec -> {
            Ability ability = AbilityRegistry.ABILITIES.get(spec.abilityId());
            if (ability == null) {
                MortalBoons.LOGGER.warn("Boon {} grants unknown ability {}", boon.id(), spec.abilityId());
                return;
            }
            AbilityAPI.grant(player, grantSource(boon), ability, spec.abilityLevel().resolve(tier));
        });
    }

    static void removeBoon(ServerPlayer player, Boon boon) {
        boon.abilityGrants().forEach(spec -> {
            Ability ability = AbilityRegistry.ABILITIES.get(spec.abilityId());
            if (ability != null) {
                AbilityAPI.revoke(player, grantSource(boon), ability);
            }
        });
    }

    static void revokeAllGrants(ServerPlayer player) {
        AbilityAPI.getGrantsBySource(player).forEach((source, grants) -> {
            if (source.getNamespace().equals(MortalBoons.MODID)
                    && source.getPath().startsWith(GRANT_SOURCE_PATH_PREFIX)) {
                grants.keySet().forEach(ability -> AbilityAPI.revoke(player, source, ability));
            }
        });
    }
}
