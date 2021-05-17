package redcstone.extended_redstone.mixin.StairDirectionalSolidness;

import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;

@Mixin(World.class)
public class WorldMixin {

    @Shadow
    BlockState getBlockState(BlockPos pos){
        return null;
    };
    @Shadow
    int getReceivedStrongRedstonePower(BlockPos pos) {
        return 0;
    }

    @Inject(method = "getEmittedRedstonePower", at = @At(value = "INVOKE"), cancellable = true)
    private void getEmittedRedstonePowerInject(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir)
    {
        BlockState blockState = this.getBlockState(pos);
        int i = this.getReceivedStrongRedstonePower(pos);
        if (StairsBlock.isStairs(blockState)) {
            cir.setReturnValue(blockState.isSideSolid((BlockView) this, pos, direction.getOpposite(), SideShapeType.FULL) ? i : 0);
        }
    }

    @Inject(method = "getReceivedStrongRedstonePower", at = @At(value = "INVOKE"), cancellable = true)
    private void getReceivedStrongRedstonePowerInject(BlockPos pos, CallbackInfoReturnable<Integer> cir)
    {
        BlockState blockState = this.getBlockState(pos);
        if (!StairsBlock.isStairs(blockState))
            return;

        int i = 0;
        int Power = 0;
        Direction[] senseDirection = Direction.values();

        for (Direction direction : senseDirection) {
            if (!blockState.isSideSolid((BlockView) this, pos, direction, SideShapeType.FULL))
                continue;
            Power = getStrongRedstonePowerMixin(pos.offset(direction), direction);
            i = Math.max(i, Power);
        }
        cir.setReturnValue(i);
    }


    private int getStrongRedstonePowerMixin(BlockPos pos, Direction direction) {
        return this.getBlockState(pos).getStrongRedstonePower((BlockView) this, pos, direction);
    }
}
