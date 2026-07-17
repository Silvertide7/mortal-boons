package net.silvertide.mortal_boons.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.mortal_boons.client.FatestoneScreen;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void handleFatestoneScreen(FatestoneScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new FatestoneScreen(payload)));
    }
}
