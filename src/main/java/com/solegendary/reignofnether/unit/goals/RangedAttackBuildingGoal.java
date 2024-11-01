package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;


public class RangedAttackBuildingGoal<T extends net.minecraft.world.entity.Mob> extends Goal {
    private final T mob;
    private BlockPos blockTarget = null;
    private UnitBowAttackGoal<?> bowAttackGoal = null;
    private UnitCrossbowAttackGoal<?> cbowAttackGoal = null;
    private Building buildingTarget = null;

    public RangedAttackBuildingGoal(T mob, UnitBowAttackGoal<?> bowAttackGoal) {
        this.mob = mob;
        this.bowAttackGoal = bowAttackGoal;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public RangedAttackBuildingGoal(T mob, UnitCrossbowAttackGoal<?> cbowAttackGoal) {
        this.mob = mob;
        this.cbowAttackGoal = cbowAttackGoal;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public void setNextBlockTarget() {
        if (this.buildingTarget != null && !buildingTarget.getBlocks().isEmpty()) {
            Random rand = new Random();
            List<BuildingBlock> nonAirBlocks = buildingTarget.getBlocks().stream()
                    .filter(b -> b.isPlaced(this.mob.level)).toList();

            // Check if nonAirBlocks is empty
            if (!nonAirBlocks.isEmpty()) {
                BuildingBlock block = nonAirBlocks.get(rand.nextInt(nonAirBlocks.size()));
                this.blockTarget = block.getBlockPos();
            } else {
                // Handle the case where there are no non-air blocks available
                this.blockTarget = null; // or some other fallback logic
            }
        }
    }


    public void setBuildingTarget(BlockPos blockPos) {
        if (blockPos != null) {
            if (this.mob.level.isClientSide()) {
                this.buildingTarget = BuildingUtils.findBuilding(true, blockPos);
            } else {
                this.buildingTarget = BuildingUtils.findBuilding(false, blockPos);
            }
            // Check if the building target is valid
            if (this.buildingTarget != null && !this.buildingTarget.getBlocks().isEmpty()) {
                setNextBlockTarget();
            } else {
                this.blockTarget = null; // Reset blockTarget if buildingTarget is invalid
            }
            this.start();
        }
    }

    public void setBuildingTarget(Building building) {
        this.buildingTarget = building;
    }

    public Building getBuildingTarget() {
        return buildingTarget;
    }

    public boolean canUse() {
        return this.blockTarget != null;
    }

    public boolean canContinueToUse() {
        if (!this.canUse() && this.isDoneMoving())
            return false;
        return true;
    }

    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    public void stop() {
        super.stop();
        blockTarget = null;
        buildingTarget = null;
        this.mob.setAggressive(false);
    }

    public void tick() {
        if (buildingTarget != null && buildingTarget.getBlocksPlaced() <= 0) {
            stop();
        }
        if (blockTarget != null) {
            float tx = blockTarget.getX() + 0.5f;
            float ty = blockTarget.getY() + 0.5f;
            float tz = blockTarget.getZ() + 0.5f;

            this.mob.getLookControl().setLookAt(tx, ty, tz);

            if (this.mob.level.isClientSide())
                return;

            float attackRange = ((AttackerUnit) this.mob).getAttackRange();

            double distToTarget = Math.sqrt(this.mob.distanceToSqr(tx, ty, tz));

            if ((distToTarget > attackRange - 1) &&
                !((Unit) this.mob).getHoldPosition()) {
                this.moveTo(this.blockTarget);
            } else {
                this.stopMoving();
            }
            if (distToTarget <= attackRange) { // start drawing bowstring
                if (bowAttackGoal != null) {
                    if (bowAttackGoal.getAttackCooldown() <= 0) {
                        if (mob instanceof RangedAttackerUnit rangedAttackerUnit) {
                            rangedAttackerUnit.performUnitRangedAttack(tx, ty, tz, 20);
                            if (!mob.level.isClientSide() && buildingTarget != null)
                                FogOfWarClientboundPacket.revealRangedUnit(buildingTarget.ownerName, mob.getId());
                        }
                        bowAttackGoal.setToMaxAttackCooldown();
                        setNextBlockTarget();
                    }
                }
            }
            // handle crossbow attacks (ie. pillagers attacking buildings) in UnitCrossbowAttackGoal
            // because we can't directly use mob.performUnitRangedAttack()
        }
    }

    // moveGoal controllers
    private boolean isDoneMoving() {
        Unit unit = (Unit) this.mob;
        if (unit.getMoveGoal() instanceof FlyingMoveToTargetGoal flyingMoveGoal)
            return flyingMoveGoal.isAtDestination();
        else
            return this.mob.getNavigation().isDone();
    }

    private void stopMoving() {
        Unit unit = (Unit) this.mob;
        if (unit.getMoveGoal() instanceof FlyingMoveToTargetGoal flyingMoveGoal)
            flyingMoveGoal.stopMoving();
        else
            this.mob.getNavigation().stop();
    }

    private void moveTo(BlockPos bp) {
        Unit unit = (Unit) this.mob;
        if (unit.getMoveGoal() instanceof FlyingMoveToTargetGoal flyingMoveGoal)
            flyingMoveGoal.setMoveTarget(bp);
        else
            this.mob.getNavigation().moveTo(bp.getX(), bp.getY(), bp.getZ(), 1.0f);
    }
}
