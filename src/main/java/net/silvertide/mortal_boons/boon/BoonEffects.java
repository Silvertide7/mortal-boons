package net.silvertide.mortal_boons.boon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.compat.player_abilities.PlayerAbilitiesIntegration;
import net.silvertide.mortal_boons.data.BoonAttachments;

import java.util.List;

public final class BoonEffects {
    private BoonEffects() {
    }

    public static void apply(ServerPlayer player, Boon boon, int tier) {
        List<AttributeGrant> grants = boon.attributeGrants(tier);
        for (int grantIndex = 0; grantIndex < grants.size(); grantIndex++) {
            AttributeGrant grant = grants.get(grantIndex);
            AttributeInstance attributeInstance = player.getAttribute(grant.attribute());
            if (attributeInstance == null) {
                continue;
            }
            ResourceLocation modifierId = modifierId(boon, grantIndex);
            attributeInstance.removeModifier(modifierId);
            attributeInstance.addTransientModifier(
                    new AttributeModifier(modifierId, grant.amount(), grant.operation()));
        }
        PlayerAbilitiesIntegration.applyBoon(player, boon, tier);
    }

    public static void remove(ServerPlayer player, Boon boon, int tier) {
        List<AttributeGrant> grants = boon.attributeGrants(tier);
        for (int grantIndex = 0; grantIndex < grants.size(); grantIndex++) {
            AttributeInstance attributeInstance = player.getAttribute(grants.get(grantIndex).attribute());
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifierId(boon, grantIndex));
            }
        }
        PlayerAbilitiesIntegration.removeBoon(player, boon);
    }

    public static void applyAllHeld(ServerPlayer player) {
        player.getData(BoonAttachments.BOON_DATA).getHeldBoons().forEach(held ->
                BoonManager.get(held.boonId()).ifPresent(boon -> apply(player, boon, held.tier())));
    }

    public static void removeAllHeld(ServerPlayer player) {
        player.getData(BoonAttachments.BOON_DATA).getHeldBoons().forEach(held ->
                BoonManager.get(held.boonId()).ifPresent(boon -> remove(player, boon, held.tier())));
    }

    private static ResourceLocation modifierId(Boon boon, int grantIndex) {
        return MortalBoons.id("boon/" + boon.id().getPath() + "/" + grantIndex);
    }
}
