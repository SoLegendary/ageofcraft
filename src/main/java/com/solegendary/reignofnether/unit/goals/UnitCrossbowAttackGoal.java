package com.solegendary.reignofnether.unit.goals;

import java.util.EnumSet;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

// modified version of RangedBowAttackGoal which:
// - has an attack cooldown parameter in the constructor
// - has no pathfinding delay
// - stops when the target is dead

public class UnitCrossbowAttackGoal<T extends Monster & RangedAttackMob & CrossbowAttackMob> extends Goal {
    private final T mob;
    private UnitCrossbowAttackGoal.CrossbowState crossbowState = UnitCrossbowAttackGoal.CrossbowState.UNCHARGED;
    private int seeTime;
    private int attackCooldown;
    private int attackCooldownMax;

    private static final int GARRISON_BONUS_RANGE_TO_GHASTS = 10;

    public UnitCrossbowAttackGoal(T mob, int attackCooldown) {
        this.mob = mob;
        this.attackCooldownMax = attackCooldown;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public void tickAttackCooldown() {
        attackCooldown -= 1;
    }

    public void setToMaxAttackCooldown() {
        this.attackCooldown = this.attackCooldownMax;
    }

    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }

    public boolean canContinueToUse() {
        if (!isValidTarget() || !this.isHoldingCrossbow())
            return false;
        if (!this.canUse() && this.mob.getNavigation().isDone())
            return false;

        return true;
    }

    private boolean isValidTarget() {
        return (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) || getBuildingTarget() != null;
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget((LivingEntity)null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            CrossbowItem.setCharged(this.mob.getUseItem(), false);
        }
    }

    private Building getBuildingTarget() {
        if (this.mob instanceof PillagerUnit pUnit &&
                pUnit.getAttackBuildingGoal() instanceof RangedAttackBuildingGoal<?> rabg) {
            return rabg.getBuildingTarget();
        }
        return null;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.mob.getTarget();
        Building buildTarget = getBuildingTarget();

        if ((target != null && target.isAlive()) || buildTarget != null) {

            GarrisonableBuilding garr = GarrisonableBuilding.getGarrison((Unit) this.mob);
            GarrisonableBuilding targetGarr = null;
            if (target instanceof Unit unit)
                targetGarr = GarrisonableBuilding.getGarrison(unit);

            boolean isGarrisoned = garr != null;
            boolean isTargetGarrisoned = targetGarr != null;

            boolean canSeeTarget = true;
            if (target != null)
                canSeeTarget = this.mob.getSensing().hasLineOfSight(target) || isGarrisoned || isTargetGarrisoned;
            boolean flag = this.seeTime > 0;

            if (canSeeTarget != flag) {
                this.seeTime = 0;
            }
            if (canSeeTarget) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            double distToTarget;
            if (target != null)
                distToTarget = this.mob.distanceTo(target);
            else
                distToTarget = Math.sqrt(this.mob.distanceToSqr(buildTarget.centrePos.getX(), buildTarget.centrePos.getY(), buildTarget.centrePos.getZ()));

            float attackRange = ((AttackerUnit) this.mob).getAttackRange();

            // dont consider garrison range here so the unit still moves towards the edge of the building
            if (!this.mob.isPassenger()) {
                if ((distToTarget > attackRange || !canSeeTarget) &&
                        !((Unit) this.mob).getHoldPosition()) {
                    if (target != null)
                        this.mob.getNavigation().moveTo(target, 1.0f);
                    else
                        this.mob.getNavigation().moveTo(buildTarget.centrePos.getX(), buildTarget.centrePos.getY(), buildTarget.centrePos.getZ(), 1.0f);
                } else {
                    this.mob.getNavigation().stop();
                }
            }
            if (isGarrisoned) {
                attackRange = garr.getAttackRange();
                if (target instanceof GhastUnit ghastUnit)
                    attackRange += GARRISON_BONUS_RANGE_TO_GHASTS;
            }
            else if (isTargetGarrisoned)
                attackRange += targetGarr.getExternalAttackRangeBonus();
            else if (target instanceof GhastUnit ghastUnit)
                attackRange += ghastUnit.getAttackerRangeBonus(this.mob);

            boolean flag2 = (distToTarget > attackRange || this.seeTime < 5) && this.attackCooldown == 0;

            if (target != null)
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            else
                this.mob.getLookControl().setLookAt(buildTarget.centrePos.getX(), buildTarget.centrePos.getY(), buildTarget.centrePos.getZ(), 30.0F, 30.0F);

            if (this.crossbowState == UnitCrossbowAttackGoal.CrossbowState.UNCHARGED) {
                if (!flag2) {
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                    this.crossbowState = UnitCrossbowAttackGoal.CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == UnitCrossbowAttackGoal.CrossbowState.CHARGING) {
                if (!this.mob.isUsingItem()) {
                    this.crossbowState = UnitCrossbowAttackGoal.CrossbowState.UNCHARGED;
                }

                int i = this.mob.getTicksUsingItem();
                ItemStack itemstack = this.mob.getUseItem();
                if (i >= CrossbowItem.getChargeDuration(itemstack)) {
                    this.mob.releaseUsingItem();
                    this.crossbowState = UnitCrossbowAttackGoal.CrossbowState.CHARGED;
                    this.attackCooldown = attackCooldownMax;
                    this.mob.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == UnitCrossbowAttackGoal.CrossbowState.CHARGED) {
                --this.attackCooldown;
                if (this.attackCooldown == 0) {
                    this.crossbowState = UnitCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == UnitCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK && canSeeTarget) {
                this.mob.performCrossbowAttack(this.mob, 1.6F);
                ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                CrossbowItem.setCharged(itemstack1, false);
                this.crossbowState = UnitCrossbowAttackGoal.CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean canRun() {
        return this.crossbowState == UnitCrossbowAttackGoal.CrossbowState.UNCHARGED;
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}
