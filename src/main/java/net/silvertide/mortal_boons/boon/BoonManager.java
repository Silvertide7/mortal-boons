package net.silvertide.mortal_boons.boon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.silvertide.mortal_boons.MortalBoons;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = MortalBoons.MODID)
public class BoonManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static Map<ResourceLocation, Boon> boons = Map.of();

    public BoonManager() {
        super(GSON, "mortal_boons/boons");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new BoonManager());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonById, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, Boon> parsed = new LinkedHashMap<>();
        jsonById.forEach((id, json) -> Boon.Definition.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> MortalBoons.LOGGER.error("Invalid boon {}: {}", id, error))
                .ifPresent(definition -> {
                    if (definition.minTier() > definition.maxTier()) {
                        MortalBoons.LOGGER.error("Boon {} has min_tier {} above max_tier {}",
                                id, definition.minTier(), definition.maxTier());
                        return;
                    }
                    parsed.put(id, definition.toBoon(id));
                }));
        boons = Collections.unmodifiableMap(parsed);
        MortalBoons.LOGGER.info("Loaded {} boons", boons.size());
    }

    public static Optional<Boon> get(ResourceLocation id) {
        return Optional.ofNullable(boons.get(id));
    }

    public static Collection<Boon> all() {
        return boons.values();
    }
}
