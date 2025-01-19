package com.solegendary.reignofnether.sandbox;

import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.research.ResearchClientboundPacket;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class SandboxServerboundPacket {

    public SandboxAction sandboxAction;
    public String playerName;
    public int unitId;
    public BlockPos blockPos;

    public static void spawnUnit(SandboxAction sandboxAction, String playerName, BlockPos blockPos) {
        PacketHandler.INSTANCE.sendToServer(new SandboxServerboundPacket(sandboxAction, playerName, 0, blockPos));
    }

    public SandboxServerboundPacket(SandboxAction sandboxAction, String playerName, int unitId, BlockPos blockPos) {
        this.sandboxAction = sandboxAction;
        this.playerName = playerName;
        this.unitId = unitId;
        this.blockPos = blockPos;
    }

    public SandboxServerboundPacket(FriendlyByteBuf buffer) {
        this.sandboxAction = buffer.readEnum(SandboxAction.class);
        this.playerName = buffer.readUtf();
        this.unitId = buffer.readInt();
        this.blockPos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.sandboxAction);
        buffer.writeUtf(this.playerName);
        buffer.writeInt(this.unitId);
        buffer.writeBlockPos(this.blockPos);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            if (sandboxAction.name().toLowerCase().contains("spawn_")) {
                SandboxServer.spawnUnit(this.sandboxAction, this.playerName, this.blockPos);
            }
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
