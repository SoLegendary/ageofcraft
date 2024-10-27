package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class GarrisonGoal extends MoveToTargetBlockGoal {

    private Building buildingTarget;

    public GarrisonGoal(Mob mob) {
        super(mob, true, 0);
    }

    public void tick() {
        if (buildingTarget instanceof GarrisonableBuilding garrisonableBuilding) {
            calcMoveTarget();
            if (buildingTarget.getBlocksPlaced() <= 0) {
                stopGarrisoning();
            }
            if (moveTarget != null && this.mob.distanceToSqr(new Vec3(
                    moveTarget.getX() + 0.5f,
                    moveTarget.getY() + 0.5f,
                    moveTarget.getZ() + 0.5f)) <= 3f) {

                // teleport to garrison entry pos
                if (!garrisonableBuilding.isFull() && buildingTarget.isBuilt) {
                    BlockPos bp = buildingTarget.originPos.offset(garrisonableBuilding.getEntryPosition());
                    this.mob.teleportTo(bp.getX() + 0.5f, bp.getY() + 0.5f, bp.getZ() + 0.5f);
                }
                this.stopGarrisoning();
            }
        }
        else
            this.moveTarget = null;
    }

    private void calcMoveTarget() {
        if (this.buildingTarget instanceof GarrisonableBuilding)
            this.moveTarget = this.buildingTarget.getClosestGroundPos(mob.getOnPos(), 1);
    }

    public void setBuildingTarget(BlockPos blockPos) {
        if (blockPos != null) {
            if (this.mob.level.isClientSide()) {
                this.buildingTarget = BuildingUtils.findBuilding(true, blockPos);
                if (this.buildingTarget instanceof GarrisonableBuilding garrisonableBuilding &&
                        buildingTarget.ownerName.equals(((Unit) mob).getOwnerName())) {

                    if (!garrisonableBuilding.isFull()) {
                        MiscUtil.addUnitCheckpoint(((Unit) mob), new BlockPos(
                                buildingTarget.centrePos.getX(),
                                buildingTarget.originPos.getY() + 1,
                                buildingTarget.centrePos.getZ())
                        );
                        ((Unit) mob).setIsCheckpointGreen(true);
                    } else {
                        HudClientEvents.showTemporaryMessage("That building is full!");
                    }
                } else if (this.buildingTarget == null) {
                    HudClientEvents.showTemporaryMessage("That building is not garrisonable");
                }
            }
            else
                this.buildingTarget = BuildingUtils.findBuilding(false, blockPos);
            calcMoveTarget();
            this.start();
        }
    }

    public Building getBuildingTarget() { return buildingTarget; }

    // if we override stop() it for some reason is called after start() and we can never begin this goal...
    public void stopGarrisoning() {
        buildingTarget = null;
        super.stopMoving();
    }
}
