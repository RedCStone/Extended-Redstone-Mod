package redcstone.extended_redstone;

import net.minecraft.block.BlockState;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HalfTickRepeaterBlock extends RepeaterBlock {

    public HalfTickRepeaterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 2 * (Integer)state.get(DELAY) - 1;
    }
}
