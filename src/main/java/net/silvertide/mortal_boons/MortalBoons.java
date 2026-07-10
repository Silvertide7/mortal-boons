package net.silvertide.mortal_boons;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.silvertide.mortal_boons.block.AltarBlocks;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.data.BoonAttachments;
import org.slf4j.Logger;

@Mod(MortalBoons.MODID)
public class MortalBoons {
    public static final String MODID = "mortal_boons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public MortalBoons(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, BoonConfig.SPEC);
        BoonAttachments.register(modEventBus);
        AltarBlocks.register(modEventBus);
        modEventBus.addListener(MortalBoons::addPackFinders);
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(id("builtin_data_packs/default_boons"), PackType.SERVER_DATA,
                Component.literal("Mortal Boons Default Boons"), PackSource.DEFAULT, false, Pack.Position.TOP);
    }
}
