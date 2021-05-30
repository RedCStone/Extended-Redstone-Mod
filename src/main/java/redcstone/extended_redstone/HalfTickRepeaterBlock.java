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

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.abilities.allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            world.setBlockState(pos, (BlockState)state.cycle(DELAY), 3);
            return ActionResult.success(world.isClient);
        }
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return (Integer)state.get(DELAY);
    }
}
