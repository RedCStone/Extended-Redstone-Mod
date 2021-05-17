package redcstone.extended_redstone.mixin.BlockupdateRepeater;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import redcstone.extended_redstone.BlockupdateRepeaterBlock;
import redcstone.extended_redstone.Extended_redstone;

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

}
