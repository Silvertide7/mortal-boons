package net.silvertide.mortal_boons.data;

import com.mojang.serialization.Codec;
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

    public static final Supplier<AttachmentType<Long>> ROLL_COOLDOWN_END_GAME_TIME = ATTACHMENT_TYPES.register(
            "roll_cooldown_end_game_time",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());

    private BoonAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
