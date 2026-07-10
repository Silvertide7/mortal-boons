package net.silvertide.mortal_boons.data;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.silvertide.mortal_boons.MortalBoons;

import java.util.function.Supplier;

public final class BoonAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MortalBoons.MODID);

    public static final Supplier<AttachmentType<BoonData>> BOON_DATA = ATTACHMENT_TYPES.register(
            "boon_data",
            () -> AttachmentType.builder(BoonData::new).serialize(BoonData.CODEC).build());

    private BoonAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
