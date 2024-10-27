package com.solegendary.reignofnether.tps;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class TPSClientBoundPacket {
    public double tickTime;

    public static void updateTickTime(double tickTime) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new TPSClientBoundPacket(tickTime));
    }

    public TPSClientBoundPacket(double tickTime) {
        this.tickTime = tickTime;
    }

    public TPSClientBoundPacket(FriendlyByteBuf buffer) {
        this.tickTime = buffer.readDouble();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(this.tickTime);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> {
                        TPSClientEvents.updateTickTime(this.tickTime);
                        success.set(true);
                    });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
