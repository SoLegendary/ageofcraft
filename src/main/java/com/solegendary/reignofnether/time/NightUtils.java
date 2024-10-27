package com.solegendary.reignofnether.time;

import com.solegendary.reignofnether.building.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NightUtils {

    public static boolean isInRangeOfNightSource(Vec3 pos, boolean clientSide) {
        List<Building> buildings = clientSide ? BuildingClientEvents.getBuildings() : BuildingServerEvents.getBuildings();
        for (Building building : buildings) {
            if (building.isDestroyedServerside)
                continue;
            if (building instanceof NightSource ns) {
                BlockPos centrePos = BuildingUtils.getCentrePos(building.getBlocks());
                Vec2 centrePos2d = new Vec2(centrePos.getX(), centrePos.getZ());
                Vec2 pos2d = new Vec2((float) pos.x, (float) pos.z);
                if (centrePos2d.distanceToSqr(pos2d) < Math.pow(ns.getNightRange(), 2))
                    return true;
            }
        }
        return false;
    }

    // more consistent version of Mob.isSunburnTick()
    public static boolean isSunBurnTick(Mob mob) {
        if (TimeUtils.isDay(mob.level.getDayTime()) && !mob.level.isClientSide) {
            BlockPos blockpos = new BlockPos(mob.getX(), mob.getEyeY(), mob.getZ());
            boolean flag = mob.isInWaterRainOrBubble() || mob.isInPowderSnow || mob.wasInPowderSnow;
            return !mob.isOnFire() && !flag && mob.level.canSeeSky(blockpos) && !NightUtils.isInRangeOfNightSource(mob.getEyePosition(), mob.level.isClientSide);
        }
        return false;
    }
}
