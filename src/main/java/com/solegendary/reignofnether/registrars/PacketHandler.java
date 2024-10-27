package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingClientboundPacket;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.fogofwar.FogOfWarServerboundPacket;
import com.solegendary.reignofnether.fogofwar.FrozenChunkClientboundPacket;
import com.solegendary.reignofnether.fogofwar.FrozenChunkServerboundPacket;
import com.solegendary.reignofnether.guiscreen.TopdownGuiServerboundPacket;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.player.PlayerClientboundPacket;
import com.solegendary.reignofnether.player.PlayerServerboundPacket;
import com.solegendary.reignofnether.research.ResearchClientboundPacket;
import com.solegendary.reignofnether.resources.ResourcesClientboundPacket;
import com.solegendary.reignofnether.sounds.SoundClientboundPacket;
import com.solegendary.reignofnether.tps.TPSClientBoundPacket;
import com.solegendary.reignofnether.ability.AbilityClientboundPacket;
import com.solegendary.reignofnether.tutorial.TutorialClientboundPacket;
import com.solegendary.reignofnether.tutorial.TutorialServerboundPacket;
import com.solegendary.reignofnether.unit.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// Initialises all of the client-server packet-sending classes

public final class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ReignOfNether.MOD_ID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private PacketHandler() { }

    public static void init() {
        int index = 0;

        INSTANCE.messageBuilder(TopdownGuiServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TopdownGuiServerboundPacket::encode).decoder(TopdownGuiServerboundPacket::new)
                .consumer(TopdownGuiServerboundPacket::handle).add();

        INSTANCE.messageBuilder(UnitActionServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UnitActionServerboundPacket::encode).decoder(UnitActionServerboundPacket::new)
                .consumer(UnitActionServerboundPacket::handle).add();

        INSTANCE.messageBuilder(UnitActionClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UnitActionClientboundPacket::encode).decoder(UnitActionClientboundPacket::new)
                .consumer(UnitActionClientboundPacket::handle).add();

        INSTANCE.messageBuilder(UnitConvertClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UnitConvertClientboundPacket::encode).decoder(UnitConvertClientboundPacket::new)
                .consumer(UnitConvertClientboundPacket::handle).add();

        INSTANCE.messageBuilder(UnitSyncClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UnitSyncClientboundPacket::encode).decoder(UnitSyncClientboundPacket::new)
                .consumer(UnitSyncClientboundPacket::handle).add();

        INSTANCE.messageBuilder(UnitSyncWorkerClientBoundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UnitSyncWorkerClientBoundPacket::encode).decoder(UnitSyncWorkerClientBoundPacket::new)
                .consumer(UnitSyncWorkerClientBoundPacket::handle).add();

        INSTANCE.messageBuilder(UnitIdleWorkerClientBoundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UnitIdleWorkerClientBoundPacket::encode).decoder(UnitIdleWorkerClientBoundPacket::new)
                .consumer(UnitIdleWorkerClientBoundPacket::handle).add();

        INSTANCE.messageBuilder(ResearchClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ResearchClientboundPacket::encode).decoder(ResearchClientboundPacket::new)
                .consumer(ResearchClientboundPacket::handle).add();

        INSTANCE.messageBuilder(PlayerServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PlayerServerboundPacket::encode).decoder(PlayerServerboundPacket::new)
                .consumer(PlayerServerboundPacket::handle).add();

        INSTANCE.messageBuilder(PlayerClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerClientboundPacket::encode).decoder(PlayerClientboundPacket::new)
                .consumer(PlayerClientboundPacket::handle).add();

        INSTANCE.messageBuilder(FogOfWarClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FogOfWarClientboundPacket::encode).decoder(FogOfWarClientboundPacket::new)
                .consumer(FogOfWarClientboundPacket::handle).add();

        INSTANCE.messageBuilder(FogOfWarServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FogOfWarServerboundPacket::encode).decoder(FogOfWarServerboundPacket::new)
                .consumer(FogOfWarServerboundPacket::handle).add();

        INSTANCE.messageBuilder(FrozenChunkServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FrozenChunkServerboundPacket::encode).decoder(FrozenChunkServerboundPacket::new)
                .consumer(FrozenChunkServerboundPacket::handle).add();

        INSTANCE.messageBuilder(FrozenChunkClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FrozenChunkClientboundPacket::encode).decoder(FrozenChunkClientboundPacket::new)
                .consumer(FrozenChunkClientboundPacket::handle).add();

        INSTANCE.messageBuilder(BuildingServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BuildingServerboundPacket::encode).decoder(BuildingServerboundPacket::new)
                .consumer(BuildingServerboundPacket::handle).add();

        INSTANCE.messageBuilder(BuildingClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BuildingClientboundPacket::encode).decoder(BuildingClientboundPacket::new)
                .consumer(BuildingClientboundPacket::handle).add();

        INSTANCE.messageBuilder(ResourcesClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ResourcesClientboundPacket::encode).decoder(ResourcesClientboundPacket::new)
                .consumer(ResourcesClientboundPacket::handle).add();

        INSTANCE.messageBuilder(AbilityClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AbilityClientboundPacket::encode).decoder(AbilityClientboundPacket::new)
                .consumer(AbilityClientboundPacket::handle).add();

        INSTANCE.messageBuilder(TPSClientBoundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TPSClientBoundPacket::encode).decoder(TPSClientBoundPacket::new)
                .consumer(TPSClientBoundPacket::handle).add();

        INSTANCE.messageBuilder(AttackWarningClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AttackWarningClientboundPacket::encode).decoder(AttackWarningClientboundPacket::new)
                .consumer(AttackWarningClientboundPacket::handle).add();

        INSTANCE.messageBuilder(SoundClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SoundClientboundPacket::encode).decoder(SoundClientboundPacket::new)
                .consumer(SoundClientboundPacket::handle).add();

        INSTANCE.messageBuilder(TutorialClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TutorialClientboundPacket::encode).decoder(TutorialClientboundPacket::new)
                .consumer(TutorialClientboundPacket::handle).add();

        INSTANCE.messageBuilder(TutorialServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TutorialServerboundPacket::encode).decoder(TutorialServerboundPacket::new)
                .consumer(TutorialServerboundPacket::handle).add();
    }
}
