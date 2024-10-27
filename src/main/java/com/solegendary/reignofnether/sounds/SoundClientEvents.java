package com.solegendary.reignofnether.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;

public class SoundClientEvents {

    // mute the next sound played at each pos in ClientLevelMixin, then remove it from the list
    public static ArrayList<BlockPos> mutedBps = new ArrayList<>();

    public static void playSoundForAction(SoundAction soundAction, BlockPos bp) {
        SoundEvent soundEvent = getSoundEvent(soundAction);
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null)
            level.playSound(null, bp.getX(), bp.getY(), bp.getZ(), soundEvent, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static SoundEvent getSoundEvent(SoundAction soundAction) {
        return switch (soundAction) {
            case USE_PORTAL ->  SoundEvents.ENDERMAN_TELEPORT;
        };
    }
}
