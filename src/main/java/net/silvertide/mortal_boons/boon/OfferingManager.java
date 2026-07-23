package net.silvertide.mortal_boons.boon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.config.BoonConfig;
import net.silvertide.mortal_boons.network.SyncOfferingsPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = MortalBoons.MODID)
public class OfferingManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static List<Offering> offerings = List.of();

    public OfferingManager() {
        super(GSON, "mortal_boons/offerings");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new OfferingManager());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SyncOfferingsPayload payload = new SyncOfferingsPayload(offerings, BoonTypeManager.all());
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonById, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        List<Offering> parsed = new ArrayList<>();
        jsonById.forEach((id, json) -> Offering.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> MortalBoons.LOGGER.error("Invalid offering {}: {}", id, error))
                .ifPresent(parsed::add));
        offerings = List.copyOf(parsed);
        MortalBoons.LOGGER.info("Loaded {} offerings", offerings.size());
        if (offerings.isEmpty() && BoonConfig.OFFERING_MODE.get() == OfferingMode.REQUIRED) {
            MortalBoons.LOGGER.warn("Offering mode is REQUIRED but no offerings are loaded; rolling is impossible");
        }
    }

    public static void replaceSynced(List<Offering> synced) {
        offerings = List.copyOf(synced);
    }

    public static Optional<Offering> matching(ItemStack stack) {
        return offerings.stream().filter(offering -> offering.matches(stack)).findFirst();
    }

    public static boolean matches(ItemStack stack) {
        return matching(stack).isPresent();
    }

    public static boolean offeringsEnabled() {
        return BoonConfig.OFFERING_MODE.get() != OfferingMode.NONE && !offerings.isEmpty();
    }

    public static boolean offeringRequired() {
        return BoonConfig.OFFERING_MODE.get() == OfferingMode.REQUIRED && offeringsEnabled();
    }
}
