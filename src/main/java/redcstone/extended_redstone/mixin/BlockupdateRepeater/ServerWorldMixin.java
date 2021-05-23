package redcstone.extended_redstone.mixin.BlockupdateRepeater;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redcstone.extended_redstone.Extended_redstone;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow
    private MinecraftServer server;

    public ServerWorldMixin(ObjectLinkedOpenHashSet<BlockEvent> syncedBlockEventQueue) {
        this.syncedBlockEventQueue = syncedBlockEventQueue;
    }

    @Shadow
    private void processSyncedBlockEvents() {

    }
    @Shadow
    private final ObjectLinkedOpenHashSet<BlockEvent> syncedBlockEventQueue;

    private final ObjectLinkedOpenHashSet<BlockEvent> ExtendedRedstone_SlowSyncedBlockEventQueue = new ObjectLinkedOpenHashSet();


    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;processSyncedBlockEvents()V", shift = At.Shift.AFTER))
    void tickMixin(BooleanSupplier shouldKeepTicking, CallbackInfo ci)
    {
        processSlowSyncedBlockEvents();
    }

    @Inject(method = "addSyncedBlockEvent", at = @At(value = "INVOKE"), cancellable = true)
    void addSlowSyncedBlockEventMixin(BlockPos pos, Block block, int type, int data, CallbackInfo ci)
    {
        if (block.is(Extended_redstone.BLOCKUPDATE_REPEATER)) {
            this.ExtendedRedstone_SlowSyncedBlockEventQueue.add(new BlockEvent(pos, block, type, data));
            ci.cancel();
        }
    }

    private void processSlowSyncedBlockEvents() {
        while(!this.ExtendedRedstone_SlowSyncedBlockEventQueue.isEmpty()) {
            BlockEvent blockEvent = (BlockEvent)this.ExtendedRedstone_SlowSyncedBlockEventQueue.removeFirst();
            syncedBlockEventQueue.add(blockEvent);
            processSyncedBlockEvents();
        }
    }
}
