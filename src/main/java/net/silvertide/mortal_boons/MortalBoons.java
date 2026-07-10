package net.silvertide.mortal_boons;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MortalBoons.MODID)
public class MortalBoons {
    public static final String MODID = "mortal_boons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public MortalBoons(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Mortal Boons loaded.");
    }
}
