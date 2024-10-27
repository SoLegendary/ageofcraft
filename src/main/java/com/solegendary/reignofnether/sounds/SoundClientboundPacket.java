package com.solegendary.reignofnether.sounds;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class SoundClientboundPacket {

    SoundAction soundAction;
    BlockPos bp;

    public static void playSoundOnClient(SoundAction soundAction, BlockPos bp) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new SoundClientboundPacket(soundAction, bp));
    }

    public SoundClientboundPacket(SoundAction soundAction, BlockPos bp) {
        this.soundAction = soundAction;
        this.bp = bp;
    }

    public SoundClientboundPacket(FriendlyByteBuf buffer) {
        this.soundAction = buffer.readEnum(SoundAction.class);
        this.bp = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.soundAction);
        buffer.writeBlockPos(this.bp);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> {
                        SoundClientEvents.playSoundForAction(soundAction, bp);
                        success.set(true);
                    });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
