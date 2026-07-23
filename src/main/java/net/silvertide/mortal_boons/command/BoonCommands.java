package net.silvertide.mortal_boons.command;

import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.Boon;
import net.silvertide.mortal_boons.boon.BoonEffects;
import net.silvertide.mortal_boons.boon.BoonManager;
import net.silvertide.mortal_boons.boon.HeldBoon;
import net.silvertide.mortal_boons.boon.Tier;
import net.silvertide.mortal_boons.compat.player_abilities.PlayerAbilitiesIntegration;
import net.silvertide.mortal_boons.data.BoonAttachments;
import net.silvertide.mortal_boons.data.BoonData;
import net.silvertide.mortal_boons.roll.RollManager;

import java.util.List;

@EventBusSubscriber(modid = MortalBoons.MODID)
public final class BoonCommands {
    private BoonCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mortalboons")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("roll").executes(context -> roll(context.getSource())))
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("clear").executes(context -> clear(context.getSource()))));
    }

    private static ServerPlayer requirePlayer(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("mortal_boons.command.player_only"));
        }
        return player;
    }

    private static int roll(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        return RollManager.roll(player, RollManager.MAX_BOONS) ? Command.SINGLE_SUCCESS : 0;
    }

    private static int list(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        List<HeldBoon> heldBoons = player.getData(BoonAttachments.BOON_DATA).getHeldBoons();
        if (heldBoons.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("mortal_boons.list.empty"), false);
            return Command.SINGLE_SUCCESS;
        }
        for (HeldBoon held : heldBoons) {
            Component boonName = BoonManager.get(held.boonId())
                    .map(Boon::displayName)
                    .orElse(Component.literal(held.boonId().toString()));
            source.sendSuccess(() -> Component.translatable("mortal_boons.list.entry",
                    boonName, Tier.fromLevel(held.tier()).displayName()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clear(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        int clearedCount = player.getData(BoonAttachments.BOON_DATA).getHeldBoons().size();
        BoonEffects.removeAllHeld(player);
        PlayerAbilitiesIntegration.revokeAllGrants(player);
        player.setData(BoonAttachments.BOON_DATA, new BoonData());
        player.setData(BoonAttachments.ROLL_COOLDOWN_END_GAME_TIME, 0L);
        source.sendSuccess(() -> Component.translatable("mortal_boons.clear.success", clearedCount), false);
        return Command.SINGLE_SUCCESS;
    }
}
