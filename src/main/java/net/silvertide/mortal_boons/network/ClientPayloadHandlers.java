package net.silvertide.mortal_boons.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.mortal_boons.boon.BoonTypeManager;
import net.silvertide.mortal_boons.boon.OfferingManager;
import net.silvertide.mortal_boons.client.FatestoneScreen;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void handleFatestoneScreen(FatestoneScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof FatestoneScreen fatestoneScreen) {
                fatestoneScreen.updateData(payload);
            }
        });
    }

    public static void handleSyncOfferings(SyncOfferingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            OfferingManager.replaceSynced(payload.offerings());
            BoonTypeManager.replaceSynced(payload.boonTypes());
        });
    }
}
