package net.silvertide.mortal_boons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.network.AltarScreenPayload;

public class BoonAltarBlock extends Block {
    public static final int MAX_STACK_HEIGHT = 3;

    public BoonAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        PacketDistributor.sendToPlayer(serverPlayer,
                AltarScreenPayload.snapshot(serverPlayer, altarPowerAt(level, pos), pos));
        return InteractionResult.CONSUME;
    }

    public static int altarPowerAt(Level level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof BoonAltarBlock)) {
            return 0;
        }
        BlockPos bottomPos = bottomOf(level, pos);
        int altarPower = 0;
        while (altarPower < MAX_STACK_HEIGHT
                && level.getBlockState(bottomPos.above(altarPower)).getBlock() instanceof BoonAltarBlock) {
            altarPower++;
        }
        return altarPower;
    }

    private static BlockPos bottomOf(Level level, BlockPos pos) {
        BlockPos bottomPos = pos;
        while (level.getBlockState(bottomPos.below()).getBlock() instanceof BoonAltarBlock) {
            bottomPos = bottomPos.below();
        }
        return bottomPos;
    }
}
