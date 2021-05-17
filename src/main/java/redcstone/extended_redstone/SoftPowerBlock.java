package redcstone.extended_redstone;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;


public class SoftPowerBlock extends FacingBlock {

    public SoftPowerBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.SOUTH)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            if (!block.is(this))
                update(world, pos, state);
        }
    }
    private void update(World world, BlockPos pos, BlockState state) {
        this.updateNeighbors(world, pos);
    }

    private void updateNeighbors(World world, BlockPos pos) {
        if (world.getBlockState(pos).isOf(this)) {
            world.updateNeighbor(pos, this, pos.offset(world.getBlockState(pos).get(FACING)));

            world.updateNeighborsExcept(pos.offset(world.getBlockState(pos).get(FACING)), this, world.getBlockState(pos).get(FACING).getOpposite());
        }
    }

    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakRedstonePower(state,world, pos, direction);
    }

    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(FACING).getOpposite() == direction ? getReceivedRedstonePower((World) world, pos, state): 0;
    }

    private int getReceivedRedstonePower(World world, BlockPos pos, BlockState state){
        int power = 0;
        for (Direction direction : Direction.values()){
            if (direction == state.get(FACING))
                continue;
            power = Math.max(world.getStrongRedstonePower(pos.offset(direction), direction), power);
        }
        return power;
    }


    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) {
            update(world, pos, state);
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient) {
            update(world, pos, state);
        }
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

}
