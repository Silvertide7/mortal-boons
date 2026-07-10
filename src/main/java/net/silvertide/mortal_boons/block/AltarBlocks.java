package net.silvertide.mortal_boons.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.mortal_boons.MortalBoons;

@EventBusSubscriber(modid = MortalBoons.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class AltarBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MortalBoons.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MortalBoons.MODID);

    public static final DeferredBlock<BoonAltarBlock> BOON_ALTAR = BLOCKS.register("boon_altar",
            () -> new BoonAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0F, 1200.0F)
                    .sound(SoundType.STONE)));

    public static final DeferredItem<BlockItem> BOON_ALTAR_ITEM =
            ITEMS.registerSimpleBlockItem("boon_altar", BOON_ALTAR);

    private AltarBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    @SubscribeEvent
    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(BOON_ALTAR_ITEM);
        }
    }
}
