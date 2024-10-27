package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.sounds.SoundClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FrozenChunkServerboundPacket {

    BlockPos renderChunkOrigin;

    public static void syncServerBlocks(BlockPos renderChunkOrigin) {
        Minecraft MC = Minecraft.getInstance();
        if (MC.level != null) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos bp = renderChunkOrigin.offset(x,y,z);
                        BlockState bs = MC.level.getBlockState(bp);
                        if (bs.getMaterial() == Material.PORTAL ||
                            bs.getMaterial() == Material.PLANT ||
                            bs.getMaterial() == Material.REPLACEABLE_PLANT ||
                            bs.getMaterial() == Material.REPLACEABLE_WATER_PLANT ||
                            bs.getMaterial() == Material.REPLACEABLE_FIREPROOF_PLANT) {
                            SoundClientEvents.mutedBps.add(bp);
                        }
                    }
                }
            }
        }
        if (MC.player != null)
            PacketHandler.INSTANCE.sendToServer(new FrozenChunkServerboundPacket(renderChunkOrigin));
    }

    // packet-handler functions
    public FrozenChunkServerboundPacket(BlockPos renderChunkOrigin) {
        this.renderChunkOrigin = renderChunkOrigin;
    }

    public FrozenChunkServerboundPacket(FriendlyByteBuf buffer) {
        this.renderChunkOrigin = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.renderChunkOrigin);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            FogOfWarServerEvents.syncClientBlocks(this.renderChunkOrigin);
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}