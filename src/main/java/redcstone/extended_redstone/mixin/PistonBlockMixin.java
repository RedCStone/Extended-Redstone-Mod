package redcstone.extended_redstone.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import redcstone.extended_redstone.Extended_redstone;

import static net.minecraft.block.PistonBlock.EXTENDED;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin {
    @Shadow
    private boolean move(World world, BlockPos pos, Direction dir, boolean retract) {
        return false;
    }
    @Shadow
    public static boolean isMovable(BlockState blockState, World world, BlockPos blockPos, Direction direction, boolean canBreak, Direction pistonDir) {
        return false;
    }


    @Inject(method = "isMovable", at = @At(value = "RETURN"), cancellable = true)
    private static void isMovableInject(BlockState blockState, World world, BlockPos blockPos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir)
    {
        if (blockState.isOf(Extended_redstone.BLACKSTONE_PISTON) || blockState.isOf(Extended_redstone.BLACKSTONE_STICKY_PISTON)) {
            if ((Boolean)blockState.get(EXTENDED)) {
                cir.setReturnValue(false);
                return;
            }
            cir.setReturnValue(true);
        }
    }


    @Inject(method = "onSyncedBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PistonBlock;isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z", by = -3), cancellable = true)
    protected void onSyncedBlockEventInject(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir)
    {
        Direction direction = (Direction)state.get(FacingBlock.FACING);
        BlockPos blockPos = pos.add(direction.getOffsetX() * 2, direction.getOffsetY() * 2, direction.getOffsetZ() * 2);
        BlockState blockState2 = world.getBlockState(blockPos);

        if (isMovable(blockState2, world, blockPos, direction.getOpposite(), false, direction) && (blockState2.isOf(Extended_redstone.BLACKSTONE_PISTON) || blockState2.isOf(Extended_redstone.BLACKSTONE_STICKY_PISTON))) {
            move(world, pos, direction, false);
            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
            cir.setReturnValue(true);
        }
    }
}
