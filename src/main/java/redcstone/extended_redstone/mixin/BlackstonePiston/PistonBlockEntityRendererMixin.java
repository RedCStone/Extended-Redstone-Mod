package redcstone.extended_redstone.mixin.BlackstonePiston;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redcstone.extended_redstone.Extended_redstone;

@Mixin(PistonBlockEntityRenderer.class)
public class PistonBlockEntityRendererMixin {

    @Shadow
    private void renderModel(BlockPos blockPos, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, World world, boolean bl, int i){}

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"), cancellable = true)
    private void RenderBlackstonePiston(PistonBlockEntity pistonBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci)
    {
        World world = pistonBlockEntity.getWorld();
        BlockPos blockPos = pistonBlockEntity.getPos().offset(pistonBlockEntity.getMovementDirection().getOpposite());
        BlockState blockState = pistonBlockEntity.getPushedBlock();

        PistonType pistonType = blockState.isOf(Blocks.STICKY_PISTON) || blockState.isOf(Extended_redstone.BLACKSTONE_STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
        BlockState blockState2 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.TYPE, pistonType)).with(PistonHeadBlock.FACING, blockState.get(PistonBlock.FACING));
        blockState2 = (BlockState)blockState2.with(PistonHeadBlock.SHORT, pistonBlockEntity.getProgress(f) >= 0.5F);
        this.renderModel(blockPos, blockState2, matrixStack, vertexConsumerProvider, world, false, j);
        BlockPos blockPos2 = blockPos.offset(pistonBlockEntity.getMovementDirection());
        matrixStack.pop();
        matrixStack.push();
        blockState = (BlockState)blockState.with(PistonBlock.EXTENDED, true);
        this.renderModel(blockPos2, blockState, matrixStack, vertexConsumerProvider, world, true, j);

        matrixStack.pop();
        BlockModelRenderer.disableBrightnessCache();

        ci.cancel();
    }

}
