package net.silvertide.mortal_boons.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.silvertide.mortal_boons.MortalBoons;

public record FatestoneActionPayload(BlockPos pos, Action action, int slotIndex, int revision)
        implements CustomPacketPayload {
    public static final Type<FatestoneActionPayload> TYPE = new Type<>(MortalBoons.id("fatestone_action"));

    public enum Action {
        TEMPT_FATE,
        REFORGE,
        REROLL,
        FORSAKE
    }

    private static final StreamCodec<ByteBuf, Action> ACTION_STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ordinal -> Action.values()[ordinal], Action::ordinal);

    public static final StreamCodec<ByteBuf, FatestoneActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, FatestoneActionPayload::pos,
            ACTION_STREAM_CODEC, FatestoneActionPayload::action,
            ByteBufCodecs.VAR_INT, FatestoneActionPayload::slotIndex,
            ByteBufCodecs.VAR_INT, FatestoneActionPayload::revision,
            FatestoneActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
