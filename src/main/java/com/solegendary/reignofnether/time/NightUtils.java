package com.solegendary.reignofnether.time;

import com.solegendary.reignofnether.building.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class NightUtils {

    public static boolean isInRangeOfNightSource(Vec3 pos, boolean clientSide) {
        List<Building> buildings = clientSide ? BuildingClientEvents.getBuildings() : BuildingServerEvents.getBuildings();

        // Create a copy to prevent concurrent modification issues
        List<Building> buildingCopy = new ArrayList<>(buildings);

        float posX = (float) pos.x;
        float posZ = (float) pos.z;

        for (Building building : buildingCopy) {
            if (!(building instanceof NightSource ns) || building.isDestroyedServerside) continue;

            BlockPos centrePos = BuildingUtils.getCentrePos(building.getBlocks());
            float dx = centrePos.getX() - posX;
            float dz = centrePos.getZ() - posZ;
            float rangeSquared = ns.getNightRange() * ns.getNightRange();

            if (dx * dx + dz * dz < rangeSquared) {
                return true;
            }
        }
        return false;
    }


    public static boolean isSunBurnTick(Mob mob) {
        if (!TimeUtils.isDay(mob.level.getDayTime()) || mob.level.isClientSide || mob.isOnFire()) {
            return false;
        }

        BlockPos blockPos = new BlockPos(mob.getX(), mob.getEyeY(), mob.getZ());
        boolean inWeatherOrSnow = mob.isInWaterRainOrBubble() || mob.isInPowderSnow || mob.wasInPowderSnow;

        // Only return true if all conditions for sunburn are met
        return !inWeatherOrSnow && mob.level.canSeeSky(blockPos) &&
                !isInRangeOfNightSource(mob.getEyePosition(), mob.level.isClientSide);
    }
}
