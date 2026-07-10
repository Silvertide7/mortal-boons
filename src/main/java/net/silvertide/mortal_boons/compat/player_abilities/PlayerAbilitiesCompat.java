package net.silvertide.mortal_boons.compat.player_abilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.player_abilities.api.Ability;
import net.silvertide.player_abilities.api.AbilityAPI;
import net.silvertide.player_abilities.api.AbilityRegistry;

import java.util.Map;

final class PlayerAbilitiesCompat {
    private static final ResourceLocation GRANT_SOURCE = MortalBoons.id("boons");

    private PlayerAbilitiesCompat() {
    }

    static void applyBoon(ServerPlayer player, Boon boon, int tier) {
        boon.abilityGrants().forEach(spec -> {
            Ability ability = AbilityRegistry.ABILITIES.get(spec.abilityId());
            if (ability == null) {
                MortalBoons.LOGGER.warn("Boon {} grants unknown ability {}", boon.id(), spec.abilityId());
                return;
            }
            AbilityAPI.grant(player, GRANT_SOURCE, ability, spec.abilityLevel().resolve(tier));
        });
    }

    static void removeBoon(ServerPlayer player, Boon boon) {
        boon.abilityGrants().forEach(spec -> {
            Ability ability = AbilityRegistry.ABILITIES.get(spec.abilityId());
            if (ability != null) {
                AbilityAPI.revoke(player, GRANT_SOURCE, ability);
            }
        });
    }

    static void revokeAllGrants(ServerPlayer player) {
        Map<Ability, Integer> grantedByUs = AbilityAPI.getGrantsBySource(player)
                .getOrDefault(GRANT_SOURCE, Map.of());
        grantedByUs.keySet().forEach(ability -> AbilityAPI.revoke(player, GRANT_SOURCE, ability));
    }
}
