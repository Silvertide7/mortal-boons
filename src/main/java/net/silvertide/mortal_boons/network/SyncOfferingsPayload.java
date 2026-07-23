package net.silvertide.mortal_boons.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.BoonTypeManager;
import net.silvertide.mortal_boons.boon.Offering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncOfferingsPayload(List<Offering> offerings,
                                   Map<ResourceLocation, BoonTypeManager.BoonType> boonTypes)
        implements CustomPacketPayload {
    public static final Type<SyncOfferingsPayload> TYPE = new Type<>(MortalBoons.id("sync_offerings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncOfferingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Offering.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncOfferingsPayload::offerings,
                    ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC,
                            BoonTypeManager.BoonType.STREAM_CODEC), SyncOfferingsPayload::boonTypes,
                    SyncOfferingsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
