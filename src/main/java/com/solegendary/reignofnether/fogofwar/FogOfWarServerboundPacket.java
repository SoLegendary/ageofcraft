package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FogOfWarServerboundPacket {

    boolean enable;

    public static void setServerFog(boolean enable) {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null)
            PacketHandler.INSTANCE.sendToServer(new FogOfWarServerboundPacket(enable));
    }

    // packet-handler functions
    public FogOfWarServerboundPacket(boolean enable) {
        this.enable = enable;
    }

    public FogOfWarServerboundPacket(FriendlyByteBuf buffer) {
        this.enable = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.enable);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            FogOfWarServerEvents.setEnabled(enable);
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}