package com.solegendary.reignofnether.unit.packets;

import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

// allow the server to force unit actions as though it was sent by the client so it is recorded on both sides
public class UnitActionClientboundPacket {

    private final String ownerName; // player that is issuing this command
    private final UnitAction action;
    private final int unitId;
    private final int[] unitIds; // units to be controlled
    private final BlockPos preselectedBlockPos;
    private final BlockPos selectedBuildingPos; // for building abilities

    public static void reflectUnitAction(String ownerName, UnitAction action, int unitId, int[] unitIds,
                                 BlockPos preselectedBlockPos, BlockPos selectedBuildingPos) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new UnitActionClientboundPacket(
                ownerName, action, unitId, unitIds,
                preselectedBlockPos,
                selectedBuildingPos
            ));
    }

    public static void reflectUnitAction(String ownerName, UnitAction action, int unitId, int[] unitIds) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new UnitActionClientboundPacket(
                ownerName, action, unitId, unitIds,
                new BlockPos(0,0,0),
                new BlockPos(0,0,0)
            ));
    }

    public static void reflectUnitAction(String ownerName, UnitAction action, int[] unitIds) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new UnitActionClientboundPacket(
                ownerName, action, -1, unitIds,
                new BlockPos(0,0,0),
                new BlockPos(0,0,0)
            ));
    }

    // packet-handler functions
    public UnitActionClientboundPacket(
        String ownerName,
        UnitAction action,
        int unitId,
        int[] unitIds,
        BlockPos preselectedBlockPos,
        BlockPos selectedBuildingPos
    ) {
        this.ownerName = ownerName;
        this.action = action;
        this.unitId = unitId;
        this.unitIds = unitIds;
        this.preselectedBlockPos = preselectedBlockPos;
        this.selectedBuildingPos = selectedBuildingPos;
    }

    public UnitActionClientboundPacket(FriendlyByteBuf buffer) {
        this.ownerName = buffer.readUtf();
        this.action = buffer.readEnum(UnitAction.class);
        this.unitId = buffer.readInt();
        this.unitIds = buffer.readVarIntArray();
        this.preselectedBlockPos = buffer.readBlockPos();
        this.selectedBuildingPos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.ownerName);
        buffer.writeEnum(this.action);
        buffer.writeInt(this.unitId);
        buffer.writeVarIntArray(this.unitIds);
        buffer.writeBlockPos(this.preselectedBlockPos);
        buffer.writeBlockPos(this.selectedBuildingPos);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            UnitClientEvents.sendUnitCommandManual(
                this.ownerName,
                this.action,
                this.unitId,
                this.unitIds,
                this.preselectedBlockPos,
                this.selectedBuildingPos
            );
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
