package net.silvertide.mortal_boons.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.mortal_boons.block.BoonAltarBlock;
import net.silvertide.mortal_boons.roll.RollManager;

public final class ServerPayloadHandlers {
    private static final double ALTAR_INTERACTION_PADDING = 2.0;

    private ServerPayloadHandlers() {
    }

    public static void handleAltarAction(AltarActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!serverPlayer.canInteractWithBlock(payload.altarPos(), ALTAR_INTERACTION_PADDING)) {
                return;
            }
            int altarPower = BoonAltarBlock.altarPowerAt(serverPlayer.serverLevel(), payload.altarPos());
            if (altarPower <= 0) {
                return;
            }
            switch (payload.action()) {
                case ROLL -> RollManager.roll(serverPlayer, altarPower);
                case REFORGE -> {
                    if (altarPower >= 2) {
                        RollManager.reforge(serverPlayer, payload.slotIndex());
                    }
                }
                case REROLL -> {
                    if (altarPower >= 3) {
                        RollManager.reroll(serverPlayer, payload.slotIndex());
                    }
                }
                case FORSAKE -> RollManager.forsake(serverPlayer, payload.slotIndex());
            }
            PacketDistributor.sendToPlayer(serverPlayer,
                    AltarScreenPayload.snapshot(serverPlayer, payload.altarPos()));
        });
    }
}
