package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FogOfWarClientboundPacket {

    public boolean enable;
    public String playerName;
    public int unitId;

    public static void setEnabled(boolean enable) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new FogOfWarClientboundPacket(enable, "", 0));
    }

    public static void revealOrHidePlayer(boolean reveal, String playerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new FogOfWarClientboundPacket(reveal, playerName, 0));
    }

    // when a ranged unit attacks from within fog, reveal it to the player who is being attacked
    public static void revealRangedUnit(String playerBeingAttacked, int unitId) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new FogOfWarClientboundPacket(true, playerBeingAttacked, unitId));
    }

    public FogOfWarClientboundPacket(boolean enable, String playerName, int unitId) {
        this.enable = enable;
        this.playerName = playerName;
        this.unitId = unitId;
    }

    public FogOfWarClientboundPacket(FriendlyByteBuf buffer) {
        this.enable = buffer.readBoolean();
        this.playerName = buffer.readUtf();
        this.unitId = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.enable);
        buffer.writeUtf(this.playerName);
        buffer.writeInt(this.unitId);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    if (unitId > 0)
                        FogOfWarClientEvents.revealRangedUnit(playerName, unitId);
                    else if (playerName.isEmpty())
                        FogOfWarClientEvents.setEnabled(enable);
                    else
                        FogOfWarClientEvents.revealOrHidePlayer(enable, playerName);
                    success.set(true);
                });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
