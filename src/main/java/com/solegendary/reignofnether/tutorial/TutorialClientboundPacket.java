package com.solegendary.reignofnether.tutorial;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class TutorialClientboundPacket {

    private final TutorialAction action;
    private final TutorialStage stage;

    public static void enableTutorial() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new TutorialClientboundPacket(TutorialAction.ENABLE, TutorialStage.INTRO));
    }
    public static void disableTutorial() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new TutorialClientboundPacket(TutorialAction.DISABLE, TutorialStage.INTRO));
    }
    public static void loadTutorialStage(TutorialStage stage) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new TutorialClientboundPacket(TutorialAction.LOAD_STAGE, stage));
    }

    public TutorialClientboundPacket(TutorialAction action, TutorialStage stage) {
        this.action = action;
        this.stage = stage;
    }

    public TutorialClientboundPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(TutorialAction.class);
        this.stage = buffer.readEnum(TutorialStage.class);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        buffer.writeEnum(this.stage);
    }

    // client-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    switch (action) {
                        case ENABLE -> TutorialClientEvents.setEnabled(true);
                        case DISABLE -> TutorialClientEvents.setEnabled(false);
                        case LOAD_STAGE -> TutorialClientEvents.loadStage(stage);
                    }
                });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
