package com.solegendary.reignofnether.unit.packets;

import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class UnitActionServerboundPacket {

    private final String ownerName;
    private final UnitAction action;
    private final int unitId;
    private final int[] unitIds; // units to be controlled
    private final BlockPos preselectedBlockPos;
    private final BlockPos selectedBuildingPos; // for building abilities

    // packet-handler functions
    public UnitActionServerboundPacket(
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

    public UnitActionServerboundPacket(FriendlyByteBuf buffer) {
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
            // Get the authenticated player from the network context
            ServerPlayer player = ctx.get().getSender();

            if (player != null) {
                // Verify that the provided ownerName matches the actual player’s username
                if (player.getGameProfile().getName().equals(this.ownerName)) {
                    // Use verified ownerName for entity assignment or action
                    if (this.action == UnitAction.DEBUG1) {
                        UnitServerEvents.debug1();
                    }
                    if (this.action == UnitAction.DEBUG2) {
                        UnitServerEvents.debug2();
                    }
                    UnitServerEvents.addActionItem(
                            this.ownerName,
                            this.action,
                            this.unitId,
                            this.unitIds,
                            this.preselectedBlockPos,
                            this.selectedBuildingPos
                    );
                    success.set(true);
                } else {
                    // Log or handle the packet rejection due to mismatched ownerName
                    System.out.println("Packet rejected: ownerName mismatch for player " + player.getGameProfile().getName());
                }
            } else {
                // Handle cases where the player is invalid or null
                System.out.println("Packet received from unauthenticated player.");
            }
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}