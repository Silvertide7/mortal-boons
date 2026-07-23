package net.silvertide.mortal_boons.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.mortal_boons.MortalBoons;

public final class FatestoneMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MortalBoons.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FatestoneMenu>> FATESTONE_MENU =
            MENUS.register("fatestone", () -> IMenuTypeExtension.create(FatestoneMenu::new));

    private FatestoneMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
