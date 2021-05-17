package redcstone.extended_redstone.mixin.StairDirectionalSolidness;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Shadow
    private boolean wiresGivePower;

    @Shadow
    private int increasePower(BlockState state) { return 0; }

    @Inject(method = "getReceivedRedstonePower", at = @At(value = "INVOKE"), cancellable = true)
    private void getReceivedRedstonePowerMixin(World world, BlockPos pos, CallbackInfoReturnable<Integer> cir)
    {
        this.wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        this.wiresGivePower = true;
        int j = 0;
        if (i < 15) {

            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.up())));
                } else if (StairsBlock.isStairs(blockState) && blockState.isSideSolid(world, blockPos, direction.getOpposite(), SideShapeType.FULL)&& !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)){
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.up())));
                } else if (!blockState.isSolidBlock(world, blockPos)) {
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.down())));
                }
            }

                cir.setReturnValue(Math.max(i, j - 1));
        } else {
            cir.setReturnValue(i);
        }
    }

}
