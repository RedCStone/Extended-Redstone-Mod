package redcstone.extended_redstone.mixin.BlockupdateRepeater;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import redcstone.extended_redstone.BlockupdateRepeaterBlock;
import redcstone.extended_redstone.Extended_redstone;

import java.util.Iterator;
import java.util.Set;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE"), cancellable = true)
    private static void connectsToInject(BlockState state, Direction dir, CallbackInfoReturnable<Boolean> cir)
    {
        if (state.isOf(Extended_redstone.BLOCKUPDATE_REPEATER)) {
            Direction direction = (Direction) state.get(BlockupdateRepeaterBlock.FACING);
            cir.setReturnValue(direction == dir || direction.getOpposite() == dir);
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;values()[Lnet/minecraft/util/math/Direction;", shift = At.Shift.BEFORE), cancellable = true)
    private void updateMixin(World world, BlockPos pos, BlockState state, CallbackInfo ci)
    {
        Set<BlockPos> set = Sets.newHashSet();
        set.add(pos);

        for (Direction direction : Direction.values()) {
            set.add(pos.offset(direction));
        }

        for (BlockPos blockPos : set) {
            if (!world.getBlockState(blockPos).isOf(Extended_redstone.BLOCKUPDATE_REPEATER))
                world.updateNeighborsAlways(blockPos, state.getBlock());
        }
        ci.cancel();
    }

}
