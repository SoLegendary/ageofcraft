
package com.solegendary.reignofnether.attackwarnings;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AttackWarningClientboundPacket {

    private final String attackedPlayerName;
    private final BlockPos attackPos;

    public static void sendWarning(String attackedPlayerName, BlockPos attackPos) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new AttackWarningClientboundPacket(
                attackedPlayerName,
                attackPos
            ));
    }

    // packet-handler functions
    public AttackWarningClientboundPacket(
        String attackedPlayerName,
        BlockPos attackPos
    ) {
        this.attackedPlayerName = attackedPlayerName;
        this.attackPos = attackPos;
    }

    public AttackWarningClientboundPacket(FriendlyByteBuf buffer) {
        this.attackedPlayerName = buffer.readUtf();
        this.attackPos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.attackedPlayerName);
        buffer.writeBlockPos(this.attackPos);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            AttackWarningClientEvents.checkAndTriggerAttackWarning(attackedPlayerName, attackPos);
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
