package net.silvertide.mortal_boons.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.mortal_boons.client.AltarScreen;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void handleAltarScreen(AltarScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new AltarScreen(payload)));
    }
}
