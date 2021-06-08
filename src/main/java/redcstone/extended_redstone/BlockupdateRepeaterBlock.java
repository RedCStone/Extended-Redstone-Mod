package redcstone.extended_redstone;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Random;

public class BlockupdateRepeaterBlock extends AbstractRedstoneGateBlock {
    public static final BooleanProperty LOCKED;

    public BlockupdateRepeaterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH))).with(LOCKED, false)).with(POWERED, false));

    }



    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (state.canPlaceAt(world, pos)) {
            boolean hasPower = this.hasPower(world, pos, state);
            boolean isPowered = (Boolean)state.get(POWERED);
            if (hasPower && !isPowered) {
                world.addSyncedBlockEvent(pos, this, getUpdateDelayInternal(state), 1);
            }
            else if (!hasPower && isPowered) {
                world.addSyncedBlockEvent(pos, this, getUpdateDelayInternal(state), 0);
            }
        } else {
            BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            dropStacks(state, world, pos, blockEntity);
            world.removeBlock(pos, false);

            for (Direction direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }
        }
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        if (world.isClient)
            return true;
        if (type == 0)
            scheduledUpdate(state, (ServerWorld) world, pos, data);
        else if (type > 0)
            world.addSyncedBlockEvent(pos, this, type - 1, data);
        return true;
    }

    public void scheduledUpdate(BlockState state, ServerWorld world, BlockPos pos, int data) {
        if (!this.isLocked(world, pos, state)) {
            boolean isPowered = (Boolean)state.get(POWERED);
            boolean hasPower = this.hasPower(world, pos, state);

            if (isPowered && data == 0) {
                world.setBlockState(pos, (BlockState)state.with(POWERED, false), 2);
                if (hasPower)
                    world.addSyncedBlockEvent(pos, this, getUpdateDelayInternal(state), 1);
                updateNeighbors(world,pos,state);
            } else if (!isPowered && data == 1) {
                world.setBlockState(pos, (BlockState)state.with(POWERED, true), 2);
                if (!hasPower)
                    world.addSyncedBlockEvent(pos, this, getUpdateDelayInternal(state), 0);
                updateNeighbors(world,pos,state);
            }

        }
    }
    public void updateNeighbors(World world, BlockPos pos, BlockState state)
    {
        if (world.isClient())
            return;
        world.updateNeighborsAlways(pos, this);
        world.updateNeighborsExcept(pos.offset(state.get(FACING)), this, state.get(FACING).getOpposite());
    }


    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        scheduledUpdate(state, world, pos, 0);
    }

    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!(Boolean)state.get(POWERED)) {
            return 0;
        } else {
            return state.get(FACING) == direction ? this.getOutputLevel(world, pos, state) : 0;
        }
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    protected int getUpdateDelayInternal(BlockState state) { return 3; }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = super.getPlacementState(ctx);
        return (BlockState)blockState.with(LOCKED, this.isLocked(ctx.getWorld(), ctx.getBlockPos(), blockState));
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return !world.isClient() && direction.getAxis() != ((Direction)state.get(FACING)).getAxis() ? (BlockState)state.with(LOCKED, this.isLocked(world, pos, state)) : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    public boolean isLocked(WorldView worldView, BlockPos pos, BlockState state) {
        return this.getMaxInputLevelSides(worldView, pos, state) > 0;
    }

    protected boolean isValidInput(BlockState state) {
        return isRedstoneGate(state);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED, POWERED);
    }

    static {
        LOCKED = Properties.LOCKED;
    }
}
