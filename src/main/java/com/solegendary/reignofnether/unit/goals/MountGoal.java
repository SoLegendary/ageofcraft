package com.solegendary.reignofnether.unit.goals;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.PoisonSpiderUnit;
import com.solegendary.reignofnether.unit.units.monsters.SkeletonUnit;
import com.solegendary.reignofnether.unit.units.monsters.SpiderUnit;
import com.solegendary.reignofnether.unit.units.monsters.StrayUnit;
import com.solegendary.reignofnether.unit.units.piglins.HeadhunterUnit;
import com.solegendary.reignofnether.unit.units.piglins.HoglinUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.List;

// allows a unit to be able to mount any target as long as their ability allows them to
public class MountGoal extends MoveToTargetBlockGoal {

    public static final float SEARCH_RANGE = 30;
    public static final float RANGE = 2;
    private LivingEntity targetEntity = null;
    public boolean autofind = false;

    public MountGoal(Mob mob) {
        super(mob, false, 0);
    }

    public void setTarget(LivingEntity entity) {
        this.targetEntity = entity;
    }

    private boolean isMountableUnit(Mob m) {
        if (!(m instanceof Unit unit))
            return false;

        if (m.isVehicle())
            return false;
        else if (!unit.getOwnerName().equals(((Unit) this.mob).getOwnerName()))
            return false;
        else return (this.mob instanceof PillagerUnit pillager && m instanceof RavagerUnit ravager) ||
                    (this.mob instanceof SkeletonUnit skeleton && (m instanceof SpiderUnit || m instanceof PoisonSpiderUnit)) ||
                    (this.mob instanceof StrayUnit stray && (m instanceof SpiderUnit || m instanceof PoisonSpiderUnit)) ||
                    (this.mob instanceof HeadhunterUnit headhunter && m instanceof HoglinUnit);
    }

    public void setNearestTarget() {
        if (this.mob.level.isClientSide())
            return;

        for (LivingEntity entity : UnitServerEvents.getAllUnits()) {
            if (this.mob instanceof Unit) {
                List<Mob> nearbyUnits = MiscUtil.getEntitiesWithinRange(
                        new Vector3d(this.mob.position().x, this.mob.position().y, this.mob.position().z),
                        SEARCH_RANGE, Mob.class, this.mob.level)
                        .stream().filter(this::isMountableUnit).toList();

                // find the closest mob
                double closestDist = 999999;
                Mob closestMob = null;
                for (Mob pfMob : nearbyUnits) {
                    double dist = mob.position().distanceTo(pfMob.position());
                    if (dist < closestDist) {
                        closestDist = mob.position().distanceTo(pfMob.position());
                        closestMob = pfMob;
                    }
                }
                if (closestMob != null)
                    setTarget(closestMob);
                else
                    stop();
            }
        }
    }

    @Override
    public void tick() {
        if (autofind)
            setNearestTarget();

        if (this.targetEntity != null)
            this.setMoveTarget(this.targetEntity.getOnPos());

        if (moveTarget != null &&
            this.mob instanceof Unit unit1 &&
            this.targetEntity instanceof Unit unit2) {
            if (MyMath.distance(
                    this.mob.getX(), this.mob.getZ(),
                    moveTarget.getX(), moveTarget.getZ()) <= RANGE &&
                unit1.getOwnerName().equals(unit2.getOwnerName())) {
                Unit.resetBehaviours(unit1);
                this.mob.startRiding(targetEntity);

                if (this.mob.level.isClientSide())
                    HudClientEvents.removeFromControlGroups(this.mob.getId());

                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        this.autofind = false;
        this.stopMoving();
        this.setTarget(null);
    }
}
