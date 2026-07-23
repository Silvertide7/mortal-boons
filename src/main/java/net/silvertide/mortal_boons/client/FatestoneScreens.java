package net.silvertide.mortal_boons.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.menu.FatestoneMenus;

@EventBusSubscriber(modid = MortalBoons.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FatestoneScreens {
    private FatestoneScreens() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(FatestoneMenus.FATESTONE_MENU.get(), FatestoneScreen::new);
    }
}
