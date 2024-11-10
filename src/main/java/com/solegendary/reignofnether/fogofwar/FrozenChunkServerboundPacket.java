package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.sounds.SoundClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FrozenChunkServerboundPacket {

    BlockPos renderChunkOrigin;

    public static void syncServerBlocks(BlockPos renderChunkOrigin) {
        Minecraft MC = Minecraft.getInstance();

        if (MC.level != null) {
            Set<Material> targetMaterials = Set.of(
                    Material.PORTAL,
                    Material.PLANT,
                    Material.REPLACEABLE_PLANT,
                    Material.REPLACEABLE_WATER_PLANT,
                    Material.REPLACEABLE_FIREPROOF_PLANT
            );

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos bp = renderChunkOrigin.offset(x, y, z);
                        BlockState bs = MC.level.getBlockState(bp);
                        if (isTargetMaterial(bs, targetMaterials)) {
                            SoundClientEvents.mutedBps.add(bp);
                        }
                    }
                }
            }
        }

        if (MC.player != null) {
            PacketHandler.INSTANCE.sendToServer(new FrozenChunkServerboundPacket(renderChunkOrigin));
        }
    }

    // Helper method to check if the block material is one of the target materials
    private static boolean isTargetMaterial(BlockState blockState, Set<Material> targetMaterials) {
        return targetMaterials.contains(blockState.getMaterial());
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