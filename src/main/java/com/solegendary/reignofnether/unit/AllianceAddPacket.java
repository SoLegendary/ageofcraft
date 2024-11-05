package com.solegendary.reignofnether.unit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AllianceAddPacket {
    private final String owner1;
    private final String owner2;

    public AllianceAddPacket(String owner1, String owner2) {
        this.owner1 = owner1;
        this.owner2 = owner2;
    }

    public AllianceAddPacket(FriendlyByteBuf buf) {
        this.owner1 = buf.readUtf(32767);
        this.owner2 = buf.readUtf(32767);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(owner1);
        buf.writeUtf(owner2);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling of adding an alliance
            AllianceSystem.addAlliance(owner1, owner2);
        });
        return true;
    }
}
