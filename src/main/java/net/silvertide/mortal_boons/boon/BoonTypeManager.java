package net.silvertide.mortal_boons.boon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.silvertide.mortal_boons.MortalBoons;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = MortalBoons.MODID)
public class BoonTypeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static Map<ResourceLocation, BoonType> types = Map.of();

    public record BoonType(Optional<String> title, Optional<String> description, Optional<TextColor> color) {
        public static final Codec<BoonType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("title").forGetter(BoonType::title),
                Codec.STRING.optionalFieldOf("description").forGetter(BoonType::description),
                TextColor.CODEC.optionalFieldOf("color").forGetter(BoonType::color)
        ).apply(instance, BoonType::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BoonType> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), BoonType::title,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), BoonType::description,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs::optional)
                        .map(rgb -> rgb.map(TextColor::fromRgb), color -> color.map(TextColor::getValue)),
                BoonType::color,
                BoonType::new);
    }

    public BoonTypeManager() {
        super(GSON, "mortal_boons/boon_types");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new BoonTypeManager());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonById, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, BoonType> parsed = new HashMap<>();
        jsonById.forEach((id, json) -> BoonType.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> MortalBoons.LOGGER.error("Invalid boon type {}: {}", id, error))
                .ifPresent(type -> parsed.put(id, type)));
        types = Map.copyOf(parsed);
    }

    public static Map<ResourceLocation, BoonType> all() {
        return types;
    }

    public static void replaceSynced(Map<ResourceLocation, BoonType> synced) {
        types = Map.copyOf(synced);
    }

    public static MutableComponent displayName(ResourceLocation typeId) {
        BoonType type = types.get(typeId);
        MutableComponent name = type != null && type.title().isPresent()
                ? Component.literal(type.title().get())
                : Component.translatable(
                "boon_type." + typeId.getNamespace() + "." + typeId.getPath().replace('/', '.'));
        if (type != null) {
            type.color().ifPresent(color -> name.withStyle(style -> style.withColor(color)));
        }
        return name;
    }

    public static Optional<String> description(ResourceLocation typeId) {
        return Optional.ofNullable(types.get(typeId)).flatMap(BoonType::description);
    }
}
