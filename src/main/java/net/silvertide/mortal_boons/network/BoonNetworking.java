package net.silvertide.mortal_boons.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.mortal_boons.MortalBoons;

@EventBusSubscriber(modid = MortalBoons.MODID)
public final class BoonNetworking {
    private BoonNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MortalBoons.MODID).versioned("1");
        registrar.playToClient(FatestoneScreenPayload.TYPE, FatestoneScreenPayload.STREAM_CODEC,
                ClientPayloadHandlers::handleFatestoneScreen);
        registrar.playToServer(FatestoneActionPayload.TYPE, FatestoneActionPayload.STREAM_CODEC,
                ServerPayloadHandlers::handleFatestoneAction);
    }
}
