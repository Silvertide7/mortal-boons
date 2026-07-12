package net.silvertide.mortal_boons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.network.AltarScreenPayload;
import org.jetbrains.annotations.Nullable;

public class BoonAltarBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty POWER = IntegerProperty.create("power", 1, 3);

    public BoonAltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWER, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWER, computePower(context.getLevel(), context.getClickedPos(), facing));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        int power = computePower(level, pos, state.getValue(FACING));
        if (power != state.getValue(POWER)) {
            level.setBlock(pos, state.setValue(POWER, power), Block.UPDATE_ALL);
        }
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
        PacketDistributor.sendToPlayer(serverPlayer, AltarScreenPayload.snapshot(serverPlayer, pos));
        return InteractionResult.CONSUME;
    }

    public static int altarPowerAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BoonAltarBlock ? state.getValue(POWER) : 0;
    }

    public static boolean hasCandleRing(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BoonAltarBlock
                && hasLitCandlesOnBackAndSides(level, pos, state.getValue(FACING));
    }

    public static boolean hasBeaconBelow(Level level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.BEACON);
    }

    private static int computePower(Level level, BlockPos pos, Direction facing) {
        int power = 1;
        if (hasLitCandlesOnBackAndSides(level, pos, facing)) {
            power++;
        }
        if (hasBeaconBelow(level, pos)) {
            power++;
        }
        return power;
    }

    private static boolean hasLitCandlesOnBackAndSides(Level level, BlockPos pos, Direction facing) {
        Direction[] candleDirections = {facing.getOpposite(), facing.getClockWise(), facing.getCounterClockWise()};
        for (Direction direction : candleDirections) {
            BlockState neighborState = level.getBlockState(pos.relative(direction));
            if (!(neighborState.getBlock() instanceof CandleBlock) || !neighborState.getValue(CandleBlock.LIT)) {
                return false;
            }
        }
        return true;
    }
}
