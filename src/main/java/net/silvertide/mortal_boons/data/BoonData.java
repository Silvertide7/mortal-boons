package net.silvertide.mortal_boons.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.mortal_boons.boon.HeldBoon;

import java.util.ArrayList;
import java.util.List;

public class BoonData {
    public static final Codec<BoonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HeldBoon.CODEC.listOf().optionalFieldOf("held_boons", List.of()).forGetter(BoonData::getHeldBoons),
            Codec.LONG.optionalFieldOf("roll_cooldown_end_game_time", 0L).forGetter(BoonData::getRollCooldownEndGameTime),
            Codec.INT.optionalFieldOf("lifetime_roll_count", 0).forGetter(BoonData::getLifetimeRollCount)
    ).apply(instance, BoonData::new));

    private final List<HeldBoon> heldBoons;
    private long rollCooldownEndGameTime;
    private int lifetimeRollCount;

    public BoonData() {
        this(List.of(), 0L, 0);
    }

    private BoonData(List<HeldBoon> heldBoons, long rollCooldownEndGameTime, int lifetimeRollCount) {
        this.heldBoons = new ArrayList<>(heldBoons);
        this.rollCooldownEndGameTime = rollCooldownEndGameTime;
        this.lifetimeRollCount = lifetimeRollCount;
    }

    public List<HeldBoon> getHeldBoons() {
        return List.copyOf(heldBoons);
    }

    public boolean holds(ResourceLocation boonId) {
        return heldBoons.stream().anyMatch(held -> held.boonId().equals(boonId));
    }

    public void addBoon(HeldBoon heldBoon) {
        if (holds(heldBoon.boonId())) {
            throw new IllegalStateException("Player already holds boon " + heldBoon.boonId());
        }
        heldBoons.add(heldBoon);
    }

    public void replaceBoonAt(int index, HeldBoon replacement) {
        heldBoons.set(index, replacement);
    }

    public long getRollCooldownEndGameTime() {
        return rollCooldownEndGameTime;
    }

    public void startRollCooldown(long currentGameTime, int cooldownTicks) {
        rollCooldownEndGameTime = currentGameTime + cooldownTicks;
    }

    public boolean isRollReady(long currentGameTime) {
        return currentGameTime >= rollCooldownEndGameTime;
    }

    public int getLifetimeRollCount() {
        return lifetimeRollCount;
    }

    public void incrementLifetimeRollCount() {
        lifetimeRollCount++;
    }
}
