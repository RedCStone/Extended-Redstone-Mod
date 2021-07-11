package redcstone.extended_redstone.mixin.SoftPowerBlock;

import net.minecraft.block.*;
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
import redcstone.extended_redstone.Extended_redstone;

@Mixin(World.class)
public class WorldMixin {
    @Shadow
    public BlockState getBlockState(BlockPos pos){
        return null;
    }
    @Shadow
    public int getReceivedStrongRedstonePower(BlockPos pos) {return 0;}

    @Inject(method = "getEmittedRedstonePower", at = @At(value = "INVOKE"), cancellable = true)
    private void getEmittedRedstonePowerInject(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir)
    {
        BlockState RequestedBlock = this.getBlockState(pos);
        BlockState RequesterBlock = this.getBlockState(pos.offset(direction.getOpposite()));
        if (!RequestedBlock.isSolidBlock((BlockView)this, pos))
            cir.setReturnValue(getEmittedRedstonePower_Mixin(pos, direction));

        int i = RequestedBlock.getWeakRedstonePower((WorldView)this, pos, direction);

        cir.setReturnValue(RequestedBlock.isSolidBlock((WorldView)this, pos) ? Math.max(i, getReceivedStrongRedstonePower_SoftPower(pos, RequesterBlock)) : i);
    }
    private int getEmittedRedstonePower_Mixin(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = blockState.getWeakRedstonePower((WorldView)this, pos, direction);
        return blockState.isSolidBlock((WorldView)this, pos) ? Math.max(i, this.getReceivedStrongRedstonePower(pos)) : i;
    }


    private int getReceivedStrongRedstonePower_SoftPower(BlockPos pos, BlockState RequesterBlock)
    {
        int i = 0;
        for (Direction direction : Direction.values()) {
            BlockState CheckedBlock = this.getBlockState(pos.offset(direction));
            if (CheckedBlock.isOf(Extended_redstone.SOFTPOWER_BLOCK))
                if (RequesterBlock.isOf(Blocks.REDSTONE_WIRE))
                    continue;
            i = Math.max(i, getStrongRedstonePowerMixin(pos.offset(direction), direction));
        }
        return i;
    }

    private int getStrongRedstonePowerMixin(BlockPos pos, Direction direction) {
        return this.getBlockState(pos).getStrongRedstonePower((BlockView) this, pos, direction);
    }
}
