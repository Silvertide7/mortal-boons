package net.silvertide.mortal_boons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.silvertide.mortal_boons.roll.RollManager;

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
        BlockPos bottomPos = pos;
        while (level.getBlockState(bottomPos.below()).is(this)) {
            bottomPos = bottomPos.below();
        }
        int altarPower = 0;
        while (altarPower < MAX_STACK_HEIGHT && level.getBlockState(bottomPos.above(altarPower)).is(this)) {
            altarPower++;
        }
        int clickedSegment = pos.getY() - bottomPos.getY() + 1;
        switch (clickedSegment) {
            case 1 -> RollManager.roll(serverPlayer, altarPower);
            case 2 -> RollManager.reforge(serverPlayer);
            case 3 -> RollManager.reroll(serverPlayer);
            default -> {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.CONSUME;
    }
}
