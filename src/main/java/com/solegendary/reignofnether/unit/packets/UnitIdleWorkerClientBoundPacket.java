package com.solegendary.reignofnether.unit.packets;

import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

// send a list of worker unit ids that are idle at this point in time
public class UnitIdleWorkerClientBoundPacket {

    private final int[] oldUnitIds; // units to be controlled

    public static void sendIdleWorkerPacket() {
        int[] idleIds = UnitServerEvents.getAllUnits().stream()
                .filter(u -> u instanceof WorkerUnit wu && WorkerUnit.isIdle(wu))
                .mapToInt(LivingEntity::getId)
                .toArray();
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new UnitIdleWorkerClientBoundPacket( idleIds ));
    }

    // packet-handler functions
    public UnitIdleWorkerClientBoundPacket(
            int[] oldUnitIds
    ) {
        this.oldUnitIds = oldUnitIds;
    }

    public UnitIdleWorkerClientBoundPacket(FriendlyByteBuf buffer) {
        this.oldUnitIds = buffer.readVarIntArray();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarIntArray(this.oldUnitIds);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            UnitClientEvents.syncIdleWorkers(oldUnitIds);
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
