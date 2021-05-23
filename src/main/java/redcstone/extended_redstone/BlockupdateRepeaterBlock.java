package redcstone.extended_redstone;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Random;

public class BlockupdateRepeaterBlock extends AbstractRedstoneGateBlock {
    public static final BooleanProperty LOCKED;
    private int BLOCKUPDATECOUNT = 0;

    public BlockupdateRepeaterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH))).with(LOCKED, false)).with(POWERED, false));

    }



    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        if (world.isClient)
            return true;

        updateNeighbors(world, pos);
        if (type == 0){
            BLOCKUPDATECOUNT = this.getUpdateDelayInternal(state);
            world.addSyncedBlockEvent(pos, this, 1, data);
        }
        else if (type == 1){
            if (BLOCKUPDATECOUNT == 0)
                world.addSyncedBlockEvent(pos, this, 2, data);
            else {
                world.addSyncedBlockEvent(pos, this, 1, data);
                BLOCKUPDATECOUNT--;
            }
        }
        else if (type == 2)
            scheduledUpdate(state, (ServerWorld) world, pos, data);
        return true;
    }
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (state.canPlaceAt(world, pos)) {
            boolean hasPower = this.hasPower(world, pos, state);
            boolean isPowered = (Boolean)state.get(POWERED);
            if (hasPower && !isPowered)
                world.addSyncedBlockEvent(pos, this, 0, 1);
            else if (!hasPower && isPowered)
                world.addSyncedBlockEvent(pos, this, 0, 0);
        } else {
            BlockEntity blockEntity = this.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            dropStacks(state, world, pos, blockEntity);
            world.removeBlock(pos, false);

            for (Direction direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }
        }
    }
    private void updateNeighbors(World world, BlockPos pos) {
        if (world.getBlockState(pos).isOf(this)) {


        }
    }

    public void scheduledUpdate(BlockState state, ServerWorld world, BlockPos pos, int data) {
        if (!this.isLocked(world, pos, state)) {
            boolean isPowered = (Boolean)state.get(POWERED);
            boolean hasPower = this.hasPower(world, pos, state);

            if (isPowered && data == 0) {
                world.setBlockState(pos, (BlockState)state.with(POWERED, false), 2);
                if (hasPower)
                    world.addSyncedBlockEvent(pos, this, 0, 1);
            } else if (!isPowered && data == 1) {
                world.setBlockState(pos, (BlockState)state.with(POWERED, true), 2);
                if (!hasPower)
                    world.addSyncedBlockEvent(pos, this, 0, 0);
            }
        }
    }



    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        scheduledUpdate(state, world, pos, 0);
    }

    protected void updateTarget(World world, BlockPos pos, BlockState state) {
        Direction direction = (Direction)state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.abilities.allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            return ActionResult.success(world.isClient);
        }
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
