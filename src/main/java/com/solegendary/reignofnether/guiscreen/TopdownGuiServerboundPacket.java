package com.solegendary.reignofnether.guiscreen;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class TopdownGuiServerboundPacket {
    public boolean topdownGuiOpen = false;
    public int playerId = -1; // to track

    // client-side helper functions
    public static void openTopdownGui(int playerId) {
        PacketHandler.INSTANCE.sendToServer(new TopdownGuiServerboundPacket(true, playerId));
    }
    public static void closeTopdownGui(int playerId) {
        Minecraft.getInstance().popGuiLayer();
        PacketHandler.INSTANCE.sendToServer(new TopdownGuiServerboundPacket(false, playerId));
    }


    // packet-handler functions
    public TopdownGuiServerboundPacket(Boolean pos, int playerId) {
        this.topdownGuiOpen = pos;
        this.playerId = playerId;
    }

    public TopdownGuiServerboundPacket(FriendlyByteBuf buffer) {
        this.topdownGuiOpen = buffer.readBoolean();
        this.playerId = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.topdownGuiOpen);
        buffer.writeInt(this.playerId);
    }


    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            if (this.topdownGuiOpen)
                PlayerServerEvents.openTopdownGui(this.playerId);
            else
                PlayerServerEvents.closeTopdownGui(this.playerId);

            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}