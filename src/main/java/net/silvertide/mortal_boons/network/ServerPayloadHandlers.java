package net.silvertide.mortal_boons.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.mortal_boons.block.FatestoneBlock;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.menu.FatestoneMenu;
import net.silvertide.mortal_boons.roll.RollManager;

public final class ServerPayloadHandlers {
    private static final double FATESTONE_INTERACTION_PADDING = 4.0;

    private ServerPayloadHandlers() {
    }

    public static void refreshOpenFatestoneMenu(ServerPlayer serverPlayer) {
        if (serverPlayer.containerMenu instanceof FatestoneMenu fatestoneMenu) {
            FatestoneScreenPayload refreshed = FatestoneScreenPayload.snapshot(serverPlayer,
                    fatestoneMenu.getSnapshot().pos(), fatestoneMenu.bumpRevision());
            fatestoneMenu.updateFromSnapshot(refreshed);
            fatestoneMenu.returnInaccessibleOffering(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, refreshed);
        }
    }

    public static void handleFatestoneAction(FatestoneActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!serverPlayer.canInteractWithBlock(payload.pos(), FATESTONE_INTERACTION_PADDING)) {
                return;
            }
            int power = FatestoneBlock.powerAt(serverPlayer.serverLevel(), payload.pos());
            if (power <= 0) {
                return;
            }
            if (!(serverPlayer.containerMenu instanceof FatestoneMenu fatestoneMenu)
                    || !fatestoneMenu.getSnapshot().pos().equals(payload.pos())) {
                return;
            }
            if (payload.revision() == fatestoneMenu.getRevision()) {
                switch (payload.action()) {
                    case TEMPT_FATE -> RollManager.roll(serverPlayer, power);
                    case REFORGE -> {
                        if (power >= BoonConfig.REFORGE_REQUIRED_POWER.get()) {
                            RollManager.reforge(serverPlayer, payload.slotIndex());
                        }
                    }
                    case REROLL -> {
                        if (power >= BoonConfig.REROLL_REQUIRED_POWER.get()) {
                            RollManager.reroll(serverPlayer, payload.slotIndex());
                        }
                    }
                    case FORSAKE -> RollManager.forsake(serverPlayer, payload.slotIndex());
                }
            }
            refreshOpenFatestoneMenu(serverPlayer);
        });
    }
}
