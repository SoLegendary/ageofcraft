package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class MoveToTargetBlockGoal extends Goal {

    protected final Mob mob;
    protected BlockPos moveTarget = null;
    protected boolean persistent; // will keep trying to move back to the target if moved externally
    protected int moveReachRange = 0; // how far away from the target block to stop moving (manhattan distance)

    public MoveToTargetBlockGoal(Mob mob, boolean persistent, int reachRange) {
        this.mob = mob;
        this.persistent = persistent;
        this.moveReachRange = reachRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean isAtDestination() {
        if (moveTarget == null)
            return true;
        return mob.getNavigation().isDone();
    }

    public boolean canUse() {
        return moveTarget != null;
    }

    public boolean canContinueToUse() {
        // PathNavigation seems to have a max length so restart it if we haven't actually reached the target yet
        if (this.mob.getNavigation().isDone() && moveTarget != null &&
            this.mob.getOnPos().distSqr(moveTarget) > 1) {
            this.start();
            return true;
        }
        else if (moveTarget == null)
            return false;
        else if (this.mob.getNavigation().isDone()) {
            if (!persistent && !((Unit) this.mob).getHoldPosition())
                moveTarget = null;
            return false;
        }
        return true;
    }

    public void start() {
        if (moveTarget != null) {
            // move to exact goal instead of 1 block away
            Path path = mob.getNavigation().createPath(moveTarget.getX(), moveTarget.getY(), moveTarget.getZ(), moveReachRange);
            this.mob.getNavigation().moveTo(path, Unit.getSpeedModifier((Unit) this.mob));
        }
        else
            this.mob.getNavigation().stop();
    }

    public void setMoveTarget(@Nullable BlockPos bp) {
        if (bp != null) {
            MiscUtil.addUnitCheckpoint((Unit) mob, bp);
            ((Unit) mob).setIsCheckpointGreen(true);
        }
        this.moveTarget = bp;
        this.start();
    }

    public BlockPos getMoveTarget() {
        return this.moveTarget;
    }

    public void stopMoving() {
        this.moveTarget = null;
        this.mob.getNavigation().stop();

        if (this.mob.isVehicle() && this.mob.getPassengers().get(0) instanceof Unit unit)
            unit.getMoveGoal().stopMoving();
    }
}
