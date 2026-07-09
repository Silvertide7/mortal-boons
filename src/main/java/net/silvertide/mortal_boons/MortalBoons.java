package net.silvertide.mortal_boons;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MortalBoons.MODID)
public class MortalBoons {
    public static final String MODID = "mortal_boons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MortalBoons(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Mortal Boons loaded.");
    }
}
