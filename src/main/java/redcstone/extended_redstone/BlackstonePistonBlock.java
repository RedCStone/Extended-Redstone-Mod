package redcstone.extended_redstone;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BlackstonePistonBlock extends PistonBlock {
    private final boolean sticky;

    public BlackstonePistonBlock(boolean sticky, AbstractBlock.Settings settings) {
        super(sticky, settings);
        this.sticky = sticky;
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            this.tryMove(world, pos, state);
        }

    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            this.tryMove(world, pos, state);
        }

    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            if (!world.isClient && world.getBlockEntity(pos) == null) {
                this.tryMove(world, pos, state);
            }

        }
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite())).with(EXTENDED, false);
    }

    private void tryMove(World world, BlockPos pos, BlockState state) {
        Direction direction = (Direction)state.get(FACING);
        boolean bl = this.shouldExtend(world, pos, direction);
        if (bl && !(Boolean)state.get(EXTENDED)) {
            if ((new PistonHandler(world, pos, direction, true)).calculatePush()) {
                world.addSyncedBlockEvent(pos, this, 0, direction.getId());
            }
        } else if (!bl && (Boolean)state.get(EXTENDED)) {
            BlockPos blockPos = pos.offset((Direction)direction, 2);
            BlockState blockState = world.getBlockState(blockPos);
            int i = 1;
            if (blockState.isOf(Blocks.MOVING_PISTON) && blockState.get(FACING) == direction) {
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity instanceof PistonBlockEntity) {
                    PistonBlockEntity pistonBlockEntity = (PistonBlockEntity)blockEntity;
                    if (pistonBlockEntity.isExtending() && (pistonBlockEntity.getProgress(0.0F) < 0.5F || world.getTime() == pistonBlockEntity.getSavedWorldTime() || ((ServerWorld)world).isInBlockTick())) {
                        i = 2;
                    }
                }
            }

            world.addSyncedBlockEvent(pos, this, i, direction.getId());
        }

    }

    private boolean shouldExtend(World world, BlockPos pos, Direction pistonFace) {
        Direction[] var4 = Direction.values();
        int var5 = var4.length;

        int var6;
        for(var6 = 0; var6 < var5; ++var6) {
            Direction direction = var4[var6];
            if (direction != pistonFace && world.isEmittingRedstonePower(pos.offset(direction), direction)) {
                return true;
            }
        }
        return false;
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        Direction direction = (Direction)state.get(FACING);
        if (!world.isClient) {
            boolean bl = this.shouldExtend(world, pos, direction);
            if (bl && (type == 1 || type == 2)) {
                world.setBlockState(pos, (BlockState)state.with(EXTENDED, true), 2);
                return false;
            }

            if (!bl && type == 0) {
                return false;
            }
        }

        if (type == 0) {
            if (!this.move(world, pos, direction, true)) {
                return false;
            }

            world.setBlockState(pos, (BlockState)state.with(EXTENDED, true), 67);
            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
        } else if (type == 1 || type == 2) {
            BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));
            if (blockEntity instanceof PistonBlockEntity) {
                ((PistonBlockEntity)blockEntity).finish();
            }

            BlockState blockState = (BlockState)((BlockState)Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, direction)).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
            world.setBlockState(pos, blockState, 20);
            world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(pos, blockState, (BlockState)this.getDefaultState().with(FACING, Direction.byId(data & 7)), direction, false, true));
            world.updateNeighbors(pos, blockState.getBlock());
            blockState.updateNeighbors(world, pos, 2);
            if (this.sticky) {
                BlockPos blockPos = pos.add(direction.getOffsetX() * 2, direction.getOffsetY() * 2, direction.getOffsetZ() * 2);
                BlockState blockState2 = world.getBlockState(blockPos);
                boolean bl2 = false;
                if (blockState2.isOf(Blocks.MOVING_PISTON)) {
                    BlockEntity blockEntity2 = world.getBlockEntity(blockPos);
                    if (blockEntity2 instanceof PistonBlockEntity) {
                        PistonBlockEntity pistonBlockEntity = (PistonBlockEntity)blockEntity2;
                        if (pistonBlockEntity.getFacing() == direction && pistonBlockEntity.isExtending()) {
                            pistonBlockEntity.finish();
                            bl2 = true;
                        }
                    }
                }

                if (!bl2) {
                    if (type != 1 || blockState2.isAir() || !isMovable(blockState2, world, blockPos, direction.getOpposite(), false, direction) || blockState2.getPistonBehavior() != PistonBehavior.NORMAL && !blockState2.isOf(Blocks.PISTON) && !blockState2.isOf(Blocks.STICKY_PISTON)&& !blockState2.isOf(Extended_redstone.BLACKSTONE_PISTON) && !blockState2.isOf(Extended_redstone.BLACKSTONE_STICKY_PISTON)) {
                        world.removeBlock(pos.offset(direction), false);
                    } else {
                        this.move(world, pos, direction, false);
                    }
                }
            } else {
                world.removeBlock(pos.offset(direction), false);
            }

            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }


    public static boolean isMovable(BlockState blockState, World world, BlockPos blockPos, Direction direction, boolean canBreak, Direction pistonDir) {
        if (blockPos.getY() >= 0 && blockPos.getY() <= world.getHeight() - 1 && world.getWorldBorder().contains(blockPos)) {
            if (blockState.isAir()) {
                return true;
            } else if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.CRYING_OBSIDIAN) && !blockState.isOf(Blocks.RESPAWN_ANCHOR)) {
                if (direction == Direction.DOWN && blockPos.getY() == 0) {
                    return false;
                } else if (direction == Direction.UP && blockPos.getY() == world.getHeight() - 1) {
                    return false;
                } else {
                    if (!blockState.isOf(Blocks.PISTON) && !blockState.isOf(Blocks.STICKY_PISTON) && !blockState.isOf(Extended_redstone.BLACKSTONE_PISTON) && !blockState.isOf(Extended_redstone.BLACKSTONE_STICKY_PISTON)) {
                        if (blockState.getHardness(world, blockPos) == -1.0F) {
                            return false;
                        }

                        switch(blockState.getPistonBehavior()) {
                            case BLOCK:
                                return false;
                            case DESTROY:
                                return canBreak;
                            case PUSH_ONLY:
                                return direction == pistonDir;
                        }
                    } else if ((Boolean)blockState.get(EXTENDED)) {
                        return false;
                    }

                    return !blockState.hasBlockEntity();
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean move(World world, BlockPos pos, Direction dir, boolean retract) {
        BlockPos blockPos = pos.offset(dir);
        if (!retract && world.getBlockState(blockPos).isOf(Blocks.PISTON_HEAD)) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 20);
        }

        PistonHandler pistonHandler = new PistonHandler(world, pos, dir, retract);
        if (!pistonHandler.calculatePush()) {
            return false;
        } else {
            Map<BlockPos, BlockState> map = Maps.newHashMap();
            List<BlockPos> list = pistonHandler.getMovedBlocks();
            List<BlockState> list2 = Lists.newArrayList();

            for(int i = 0; i < list.size(); ++i) {
                BlockPos blockPos2 = (BlockPos)list.get(i);
                BlockState blockState = world.getBlockState(blockPos2);
                list2.add(blockState);
                map.put(blockPos2, blockState);
            }

            List<BlockPos> list3 = pistonHandler.getBrokenBlocks();
            BlockState[] blockStates = new BlockState[list.size() + list3.size()];
            Direction direction = retract ? dir : dir.getOpposite();
            int j = 0;

            int l;
            BlockPos blockPos4;
            BlockState blockState9;
            for(l = list3.size() - 1; l >= 0; --l) {
                blockPos4 = (BlockPos)list3.get(l);
                blockState9 = world.getBlockState(blockPos4);
                BlockEntity blockEntity = blockState9.hasBlockEntity() ? world.getBlockEntity(blockPos4) : null;
                dropStacks(blockState9, world, blockPos4, blockEntity);
                world.setBlockState(blockPos4, Blocks.AIR.getDefaultState(), 18);
                if (!blockState9.isIn(BlockTags.FIRE)) {
                    world.addBlockBreakParticles(blockPos4, blockState9);
                }

                blockStates[j++] = blockState9;
            }

            for(l = list.size() - 1; l >= 0; --l) {
                blockPos4 = (BlockPos)list.get(l);
                blockState9 = world.getBlockState(blockPos4);
                blockPos4 = blockPos4.offset(direction);
                map.remove(blockPos4);
                BlockState blockState4 = (BlockState)Blocks.MOVING_PISTON.getDefaultState().with(FACING, dir);
                world.setBlockState(blockPos4, blockState4, 68);
                world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(blockPos4, blockState4, (BlockState)list2.get(l), dir, retract, false));
                blockStates[j++] = blockState9;
            }

            if (retract) {
                PistonType pistonType = this.sticky ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState blockState5 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.FACING, dir)).with(PistonHeadBlock.TYPE, pistonType);
                blockState9 = (BlockState)((BlockState)Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, dir)).with(PistonExtensionBlock.TYPE, this.sticky ? PistonType.STICKY : PistonType.DEFAULT);
                map.remove(blockPos);
                world.setBlockState(blockPos, blockState9, 68);
                world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(blockPos, blockState9, blockState5, dir, true, true));
            }

            BlockState blockState7 = Blocks.AIR.getDefaultState();
            Iterator var25 = map.keySet().iterator();

            while(var25.hasNext()) {
                BlockPos blockPos5 = (BlockPos)var25.next();
                world.setBlockState(blockPos5, blockState7, 82);
            }

            var25 = map.entrySet().iterator();

            BlockPos blockPos7;
            while(var25.hasNext()) {
                Map.Entry<BlockPos, BlockState> entry = (Map.Entry)var25.next();
                blockPos7 = (BlockPos)entry.getKey();
                BlockState blockState8 = (BlockState)entry.getValue();
                blockState8.prepare(world, blockPos7, 2);
                blockState7.updateNeighbors(world, blockPos7, 2);
                blockState7.prepare(world, blockPos7, 2);
            }

            j = 0;

            int n;
            for(n = list3.size() - 1; n >= 0; --n) {
                blockState9 = blockStates[j++];
                blockPos7 = (BlockPos)list3.get(n);
                blockState9.prepare(world, blockPos7, 2);
                world.updateNeighborsAlways(blockPos7, blockState9.getBlock());
            }

            for(n = list.size() - 1; n >= 0; --n) {
                world.updateNeighborsAlways((BlockPos)list.get(n), blockStates[j++].getBlock());
            }

            if (retract) {
                world.updateNeighborsAlways(blockPos, Blocks.PISTON_HEAD);
            }

            return true;
        }
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
    }

    public boolean hasSidedTransparency(BlockState state) {
        return (Boolean)state.get(EXTENDED);
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

}
