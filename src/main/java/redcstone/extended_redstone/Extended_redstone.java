package redcstone.extended_redstone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

public class Extended_redstone implements ModInitializer {
    public static final Block BLACKSTONE_PISTON;
    public static final Block BLACKSTONE_STICKY_PISTON;
    public static final Block SOFTPOWER_BLOCK;
    public static final Block BLOCKUPDATE_REPEATER;

    static {
        BLACKSTONE_PISTON = createBlackstonePistonBlock(false);
        BLACKSTONE_STICKY_PISTON = createBlackstonePistonBlock(true);
        SOFTPOWER_BLOCK = createSoftpowerBlock();
        BLOCKUPDATE_REPEATER = new BlockupdateRepeaterBlock(FabricBlockSettings.of(Material.PISTON));
    }


    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("extended_redstone", "blackstone_piston"), BLACKSTONE_PISTON);
        Registry.register(Registry.ITEM, new Identifier("extended_redstone", "blackstone_piston"), new BlockItem(BLACKSTONE_PISTON, new FabricItemSettings().group(ItemGroup.REDSTONE)));

        Registry.register(Registry.BLOCK, new Identifier("extended_redstone", "blackstone_sticky_piston"), BLACKSTONE_STICKY_PISTON);
        Registry.register(Registry.ITEM, new Identifier("extended_redstone", "blackstone_sticky_piston"), new BlockItem(BLACKSTONE_STICKY_PISTON, new FabricItemSettings().group(ItemGroup.REDSTONE)));

        Registry.register(Registry.BLOCK, new Identifier("extended_redstone", "softpower_block"), SOFTPOWER_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("extended_redstone", "softpower_block"), new BlockItem(SOFTPOWER_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE)));

        Registry.register(Registry.BLOCK, new Identifier("extended_redstone", "blockupdate_repeater"), BLOCKUPDATE_REPEATER);
        Registry.register(Registry.ITEM, new Identifier("extended_redstone", "blockupdate_repeater"), new BlockItem(BLOCKUPDATE_REPEATER, new FabricItemSettings().group(ItemGroup.REDSTONE)));
    }

    private static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    private static BlackstonePistonBlock createBlackstonePistonBlock(boolean sticky) {
        AbstractBlock.ContextPredicate contextPredicate = (blockState, blockView, blockPos) -> {
            return !(Boolean)blockState.get(BlackstonePistonBlock.EXTENDED);
        };
        return new BlackstonePistonBlock(sticky, AbstractBlock.Settings.of(Material.PISTON).strength(1.5F).solidBlock(Extended_redstone::never).suffocates(contextPredicate).blockVision(contextPredicate));
    }

    private static SoftPowerBlock createSoftpowerBlock() {
        return new SoftPowerBlock(AbstractBlock.Settings.of(Material.PISTON).strength(1.5F).solidBlock(Extended_redstone::never));
    }
}
