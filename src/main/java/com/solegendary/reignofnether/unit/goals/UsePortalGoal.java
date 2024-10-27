package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.sounds.SoundAction;
import com.solegendary.reignofnether.sounds.SoundClientboundPacket;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class UsePortalGoal extends MoveToTargetBlockGoal {

    private Building buildingTarget;

    public UsePortalGoal(Mob mob) {
        super(mob, true, 0);
    }

    public void tick() {
        if (buildingTarget instanceof Portal portal) {
            calcMoveTarget();
            if (buildingTarget.getBlocksPlaced() <= 0) {
                stopUsingPortal();
            }
            if (this.mob.distanceToSqr(new Vec3(
                    moveTarget.getX() + 0.5f,
                    moveTarget.getY() + 0.5f,
                    moveTarget.getZ() + 0.5f)) <= 3f) {

                // teleport to destination
                if (portal.destination != null && buildingTarget.isBuilt) {
                    BlockPos bp = portal.destination;
                    SoundClientboundPacket.playSoundOnClient(SoundAction.USE_PORTAL, bp);
                    mob.teleportTo(bp.getX() + 0.5f, bp.getY() + 0.5f, bp.getZ() + 0.5f);
                    SoundClientboundPacket.playSoundOnClient(SoundAction.USE_PORTAL, portal.destination);
                }
                this.stopUsingPortal();
            }
        }
        else
            this.moveTarget = null;
    }

    private void calcMoveTarget() {
        if (this.buildingTarget instanceof Portal)
            this.moveTarget = this.buildingTarget.centrePos;
    }

    public void setBuildingTarget(BlockPos blockPos) {
        if (blockPos != null) {
            if (this.mob.level.isClientSide()) {
                this.buildingTarget = BuildingUtils.findBuilding(true, blockPos);
                if (this.buildingTarget instanceof Portal portal &&
                        buildingTarget.ownerName.equals(((Unit) mob).getOwnerName())) {

                    if (portal.destination != null) {
                        MiscUtil.addUnitCheckpoint(((Unit) mob), new BlockPos(
                                buildingTarget.centrePos.getX(),
                                buildingTarget.originPos.getY() + 1,
                                buildingTarget.centrePos.getZ())
                        );
                        ((Unit) mob).setIsCheckpointGreen(true);
                    } else {
                        HudClientEvents.showTemporaryMessage("That portal has no destination!");
                    }
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
    public void stopUsingPortal() {
        buildingTarget = null;
        super.stopMoving();
    }
}
