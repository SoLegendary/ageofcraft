package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.sounds.SoundClientEvents;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.PlayLevelSoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.time.TimeUtils.normaliseTime;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Shadow @Final private Minecraft minecraft;

    private boolean isWardenSound(SoundEvent pSoundEvent) {
        return pSoundEvent.getLocation().getPath().contains("warden");
    }
    private boolean isGhastHurt(SoundEvent pSoundEvent) {
        return pSoundEvent.getLocation().getPath().contains("ghast.hurt");
    }

    @Inject(
            method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void playSeededSound(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSoundSource, float pVolume, float pPitch, long pSeed, CallbackInfo ci) {
        if (!OrthoviewClientEvents.isEnabled())
            return;
        ci.cancel();
        if (pSoundEvent.equals(SoundEvents.WARDEN_HEARTBEAT))
            return;

        float volumeMult = 0.5f;
        if (isWardenSound(pSoundEvent))
            volumeMult = 0.2f;
        else if (isGhastHurt(pSoundEvent))
            volumeMult = 0.1f;

        this.playSoundActual(pX, pY, pZ, pSoundEvent, pSoundSource, pVolume * volumeMult, pPitch, false, pSeed);
    }

    // plays sounds for orthoview players as though they were on the ground near their selected units/buildings
    @Inject(
            method = "playSound",
            at = @At("HEAD"),
            cancellable = true
    )
    private void playSound(double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSoundSource,
                           float pVolume, float pPitch, boolean pDistanceDelay, long pSeed, CallbackInfo ci) {
        if (!OrthoviewClientEvents.isEnabled())
            return;
        ci.cancel();
        if (pSoundEvent.equals(SoundEvents.WARDEN_HEARTBEAT))
            return;

        BlockPos bp = new BlockPos(pX, pY, pZ);
        if (SoundClientEvents.mutedBps.contains(bp)) {
            SoundClientEvents.mutedBps.remove(bp);
            return;
        }

        float volumeMult = 0.5f;
        if (isWardenSound(pSoundEvent))
            volumeMult = 0.2f;
        else if (isGhastHurt(pSoundEvent))
            volumeMult = 0.1f;

        this.playSoundActual(pX, pY, pZ, pSoundEvent, pSoundSource, pVolume * volumeMult, pPitch, false, pSeed);
    }

    // not a mixin, but called by them
    private void playSoundActual(double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSource,
                           float pVolume, float pPitch, boolean pDistanceDelay, long pSeed) {
        if (!FogOfWarClientEvents.isInBrightChunk(new BlockPos(pX + 0.5f, pY + 0.5f, pZ + 0.5f)) &&
                !pSoundEvent.getLocation().getPath().contains("ui.button.click") &&
                !pSoundEvent.getLocation().getNamespace().contains("reignofnether"))
            return;

        Vec3 soundPos = getOrthoviewSoundPos(new Vec3(pX, pY, pZ));

        double d0 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(soundPos.x(), soundPos.y(), soundPos.z());
        SimpleSoundInstance simplesoundinstance = new SimpleSoundInstance(
                pSoundEvent, pSource, pVolume, pPitch, RandomSource.create(pSeed), soundPos.x(), soundPos.y(), soundPos.z()
        );
        if (pDistanceDelay && d0 > 100.0) {
            double d1 = Math.sqrt(d0) / 40.0;
            this.minecraft.getSoundManager().playDelayed(simplesoundinstance, (int)(d1 * 20.0));
        } else {
            this.minecraft.getSoundManager().play(simplesoundinstance);
        }
    }

    // calculates the location of where local world sounds should play for an orthoview player based on the selected
    // units and/or buildings that are currently on the screen
    // eg. if the player has selected a unit at pos (4,6) but their player entity is at (34,52) and a sound plays at
    // (10,10), the new sound position should instead be (10,10) - (4,6) + (34,52) = (40,56)
    // if nothing is selected, then default to the centre of the screen
    private Vec3 getOrthoviewSoundPos(Vec3 originalPos) {
        Player player = this.minecraft.player;
        if (player == null)
            return originalPos;

        ArrayList<Vec3> posList = new ArrayList<>();

        for (LivingEntity entity : UnitClientEvents.getSelectedUnits())
            posList.add(entity.getEyePosition());
        for (Building building : BuildingClientEvents.getSelectedBuildings()) {
            BlockPos bp = BuildingUtils.getCentrePos(building.getBlocks());
            posList.add(new Vec3(bp.getX(), bp.getY(), bp.getZ()));
        }
        // remove any positions that aren't on the screen
        List<Vec3> posListOnScreen = posList.stream().filter(vec3 -> MinimapClientEvents.isWorldXZinsideMap((int) vec3.x, (int) vec3.z)).toList();

        // calculate the average position
        Vec3 newPos = new Vec3(0,0,0);
        if (posListOnScreen.size() > 0) {
            for (Vec3 pos : posListOnScreen)
                newPos = newPos.add(pos);
            double m = 1D/posListOnScreen.size();
            newPos = newPos.multiply(m, m, m);
        } else {
            // do a similar kind of calculation to get the pos at the centre of the screen as in CursorClientEvents
            newPos = MiscUtil.getOrthoviewCentreWorldPos(this.minecraft);
        }
        // get the position for the sound as though the player was at position newPos
        Vec3 diffOriginalToNew = originalPos.add(newPos.multiply(-1,-1,-1));
        return new Vec3(player.getX(), player.getY(), player.getZ()).add(diffOriginalToNew);
    }

    @Shadow public void setGameTime(long pTime) { }
    @Shadow public void setDayTime(long pTime) { }

    // when near a source of night distortion, speed up time towards midnight (in whichever direction is closest)
    @Inject(
            method = "tickTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickTime(CallbackInfo ci) {
        if (minecraft.level == null)
            return;

        ci.cancel();

        long timeNow = minecraft.level.getDayTime();
        long targetTime = TimeClientEvents.targetClientTime;
        long targetTimePlusHalfDay = targetTime + 12000;

        // transition through day faster if in orthoview and we aren't near dawn/dusk since you can't see the sky anyway
        long timeDiff = 100L;
        if (OrthoviewClientEvents.isEnabled() &&
                ((timeNow > 2000 && timeNow <= 10000) || (timeNow > 14000 && timeNow <= 22000)))
            timeDiff = 500L;

        targetTime = normaliseTime(targetTime);
        targetTimePlusHalfDay = normaliseTime(targetTimePlusHalfDay);

        if (targetTime < 12000 && (timeNow > targetTime && timeNow <= targetTimePlusHalfDay))
            timeDiff *= -1;
        else if (targetTime >= 12000 && (timeNow > targetTime || timeNow <= targetTimePlusHalfDay))
            timeDiff *= -1;

        long timeSet;
        if (Math.abs(timeNow - targetTime) < Math.abs(timeDiff))
            timeSet = targetTime;
        else
            timeSet = minecraft.level.getLevelData().getGameTime() + timeDiff;

        timeSet = normaliseTime(timeSet);

        this.setGameTime(timeSet);
        this.setDayTime(timeSet);
    }

    @Inject(
            method = "addDestroyBlockEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddDestroyBlockEffect(BlockPos pPos, BlockState pState, CallbackInfo ci) {
        if (!FogOfWarClientEvents.isInBrightChunk(pPos))
            ci.cancel();
    }
}
