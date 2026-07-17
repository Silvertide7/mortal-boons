package net.silvertide.mortal_boons.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.mortal_boons.block.FatestoneBlock;
import net.silvertide.mortal_boons.roll.RollManager;

public final class ServerPayloadHandlers {
    private static final double FATESTONE_INTERACTION_PADDING = 2.0;

    private ServerPayloadHandlers() {
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
            switch (payload.action()) {
                case TEMPT_FATE -> RollManager.roll(serverPlayer, power);
                case REFORGE -> {
                    if (power >= 2) {
                        RollManager.reforge(serverPlayer, payload.slotIndex());
                    }
                }
                case REROLL -> {
                    if (power >= 3) {
                        RollManager.reroll(serverPlayer, payload.slotIndex());
                    }
                }
                case FORSAKE -> RollManager.forsake(serverPlayer, payload.slotIndex());
            }
            PacketDistributor.sendToPlayer(serverPlayer,
                    FatestoneScreenPayload.snapshot(serverPlayer, payload.pos()));
        });
    }
}
