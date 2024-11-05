package com.solegendary.reignofnether.unit;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AllianceResetPacket {

    // Constructor for sending the packet
   /* public AllianceResetPacket() {}

    // Handle method to process the packet on the appropriate side
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Only reset alliances if the packet is processed on the server side
            if (context.getDirection().getReceptionSide().isServer()) {
                AllianceSystem.resetAlliances();
            } else {
                System.out.println("Packet received on client side; skipping resetAlliances.");
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    */
}
