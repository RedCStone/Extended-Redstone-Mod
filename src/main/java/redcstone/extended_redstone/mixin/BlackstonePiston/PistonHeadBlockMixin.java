package redcstone.extended_redstone.mixin.BlackstonePiston;

import net.minecraft.block.*;
import net.minecraft.block.enums.PistonType;
import net.minecraft.item.ItemStack;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import redcstone.extended_redstone.Extended_redstone;

import static net.minecraft.block.FacingBlock.FACING;
import static net.minecraft.block.PistonHeadBlock.TYPE;

@Mixin(PistonHeadBlock.class)
public class PistonHeadBlockMixin {
    @Inject(method = "method_26980", at = @At("RETURN"), cancellable = true)
    private void isOnPistonBase(BlockState HeadBlock, BlockState BaseBlock, CallbackInfoReturnable<Boolean> cir)
    {
        Block block = HeadBlock.get(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
        Block block2 = HeadBlock.get(TYPE) == PistonType.DEFAULT ? Extended_redstone.BLACKSTONE_PISTON : Extended_redstone.BLACKSTONE_STICKY_PISTON;
        cir.setReturnValue((BaseBlock.isOf(block) || BaseBlock.isOf(block2)) && (Boolean)BaseBlock.get(PistonBlock.EXTENDED) && BaseBlock.get(FACING) == HeadBlock.get(FACING));
    }

    @Inject(method = "getPickStack", at = @At("RETURN"), cancellable = true)
    private void getPickStack(BlockView world, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir)
    {
        BlockPos BasePos = pos.offset(state.get(FACING).getOpposite());
        BlockState BaseState = world.getBlockState(BasePos);
        cir.setReturnValue(new ItemStack(state.get(TYPE) == PistonType.STICKY ? (BaseState.isOf(Blocks.STICKY_PISTON) ? Blocks.STICKY_PISTON : Extended_redstone.BLACKSTONE_STICKY_PISTON) : (BaseState.isOf(Blocks.PISTON) ? Blocks.PISTON : Extended_redstone.BLACKSTONE_PISTON)));
    }
}
