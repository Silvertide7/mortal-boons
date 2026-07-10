package net.silvertide.mortal_boons.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.BoonEffects;
import net.silvertide.mortal_boons.data.BoonAttachments;

@EventBusSubscriber(modid = MortalBoons.MODID)
public final class BoonLifecycleHandler {
    private BoonLifecycleHandler() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BoonEffects.applyAllHeld(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BoonEffects.applyAllHeld(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onDeathClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()
                && event.getEntity() instanceof ServerPlayer newPlayer
                && event.getOriginal() instanceof ServerPlayer originalPlayer) {
            int lostBoonCount = originalPlayer.getData(BoonAttachments.BOON_DATA).getHeldBoons().size();
            if (lostBoonCount > 0) {
                newPlayer.displayClientMessage(Component.translatable("mortal_boons.death.lost", lostBoonCount), false);
            }
        }
    }
}
