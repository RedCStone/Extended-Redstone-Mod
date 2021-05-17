package redcstone.extended_redstone.mixin.BlackstonePiston;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import redcstone.extended_redstone.Extended_redstone;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin {
    @Shadow
    abstract boolean isExtending();
    @Shadow
    abstract boolean isSource();
    @Shadow
    private BlockState pushedBlock;
    @Shadow
    private float progress;


    @Inject(method = "getHeadBlockState", at = @At("RETURN"), cancellable = true)
    private void getHeadBlockState(CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(!this.isExtending() && this.isSource() && this.pushedBlock.getBlock() instanceof PistonBlock ? (BlockState)((BlockState)((BlockState) Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.SHORT, this.progress > 0.25F)).with(PistonHeadBlock.TYPE, this.pushedBlock.isOf(Extended_redstone.BLACKSTONE_STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)).with(PistonHeadBlock.FACING, this.pushedBlock.get(PistonBlock.FACING)) : this.pushedBlock);
    }
}
