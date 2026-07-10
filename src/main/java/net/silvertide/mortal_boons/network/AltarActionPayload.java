package net.silvertide.mortal_boons.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.silvertide.mortal_boons.MortalBoons;

public record AltarActionPayload(BlockPos altarPos, Action action) implements CustomPacketPayload {
    public static final Type<AltarActionPayload> TYPE = new Type<>(MortalBoons.id("altar_action"));

    public enum Action {
        ROLL,
        REFORGE,
        REROLL
    }

    private static final StreamCodec<ByteBuf, Action> ACTION_STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ordinal -> Action.values()[ordinal], Action::ordinal);

    public static final StreamCodec<ByteBuf, AltarActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AltarActionPayload::altarPos,
            ACTION_STREAM_CODEC, AltarActionPayload::action,
            AltarActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
