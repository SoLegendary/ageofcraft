package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FrozenChunkClientboundPacket {

    FrozenChunkAction action;
    BlockPos blockPos;

    public static void setBuildingDestroyedServerside(BlockPos buildingOrigin) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new FrozenChunkClientboundPacket(FrozenChunkAction.SET_BUILDING_DESTROYED, buildingOrigin));
    }

    public static void setBuildingBuiltServerside(BlockPos buildingOrigin) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new FrozenChunkClientboundPacket(FrozenChunkAction.SET_BUILDING_BUILT, buildingOrigin));
    }

    public static void unmuteChunks() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new FrozenChunkClientboundPacket(FrozenChunkAction.UNMUTE, new BlockPos(0,0,0)));
    }

    // packet-handler functions
    public FrozenChunkClientboundPacket(FrozenChunkAction action, BlockPos blockPos) {
        this.action = action;
        this.blockPos = blockPos;
    }

    public FrozenChunkClientboundPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(FrozenChunkAction.class);
        this.blockPos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        buffer.writeBlockPos(this.blockPos);
    }

    // client-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> {
                        switch (action) {
                            case SET_BUILDING_DESTROYED -> FogOfWarClientEvents.setBuildingDestroyedServerside(blockPos);
                            case SET_BUILDING_BUILT -> FogOfWarClientEvents.setBuildingBuiltServerside(blockPos);
                            case UNMUTE -> FogOfWarClientEvents.unmuteChunks();
                        }
                        success.set(true);
                    });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}