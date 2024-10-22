package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class MoveToTargetBlockGoal extends Goal {

    protected final Mob mob;
    protected BlockPos moveTarget = null;
    protected boolean persistent;
    protected int moveReachRange = 0;
    private boolean climbingLadder = false; // Track if the mob is currently climbing
    private boolean foundLadder = false; // Track if a ladder is found
    private Vec3 storedLadderPos = null; // Field to store the found ladder position
    private BlockPos lastFailedTarget = null;  // Track the last failed target
    private int repathAttempts = 0;  // Limit repath attempts
    private static final int MAX_REPATH_ATTEMPTS = 3;  // Limit the number of repaths

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
        if (climbingLadder) {
            // Continue climbing until reaching the target Y level
            return true;
        }

        if (this.mob.getNavigation().isDone() && moveTarget != null &&
                this.mob.getOnPos().distSqr(moveTarget) > 1) {
            this.start();
            return true;
        } else if (moveTarget == null)
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
            System.out.println("Starting move towards target: " + moveTarget);

            // First, try to find a safe path
            Path safePath = findSafePathTowardsTarget(moveTarget);
            if (safePath != null) {
                this.mob.getNavigation().moveTo(safePath, Unit.getSpeedModifier((Unit) this.mob));
                return;  // Use the safe path if it exists
            }

            // Only search for a ladder if no safe path was found
            Vec3 ladderPos = findClimbableLadder();
            if (ladderPos != null) {
                System.out.println("Found a climbable ladder, moving towards: " + ladderPos);

                storedLadderPos = ladderPos;  // Store the ladder position
                if (!isCloseToLadder(ladderPos)) {
                    this.mob.getNavigation().moveTo(ladderPos.x, ladderPos.y, ladderPos.z, Unit.getSpeedModifier((Unit) this.mob));
                    return;  // Wait until mob reaches the ladder
                }

                // Start climbing if we're close enough
                startClimbingLadder(ladderPos);
                return;
            }

            // No path and no ladder, stop movement
            this.mob.getNavigation().stop();
            System.out.println("No safe path or ladder found, stopping movement.");
        } else {
            this.mob.getNavigation().stop();
            System.out.println("No target set, stopping movement.");
        }
    }


    private boolean isCloseToLadder(Vec3 ladderPos) {
        double distanceSquared = mob.blockPosition().distSqr(new BlockPos(ladderPos.x, mob.getY(), ladderPos.z));
        System.out.println("Distance to ladder: " + distanceSquared);
        return distanceSquared < 2.25;  // 1.5^2 is 2.25
    }

    public void setMoveTarget(@Nullable BlockPos bp) {
        if (bp != null) {
            MiscUtil.addUnitCheckpoint((Unit) mob, bp);
            ((Unit) mob).setIsCheckpointGreen(true);

            System.out.println("Setting new move target: " + bp);

            if (isLavaOrFireNearBlock(bp)) {
                System.out.println("Lava or fire detected near the block, finding safer path...");
            }
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
        System.out.println("Stopping movement.");

        if (this.mob.isVehicle() && this.mob.getPassengers().get(0) instanceof Unit unit)
            unit.getMoveGoal().stopMoving();
    }

    private Vec3 findClimbableLadder() {
        Vec3 closestLadderPos = null;  // Variable to store the position of the closest ladder
        double closestDistance = Double.MAX_VALUE;  // Initialize with a very large number

        for (int i = 0; i < 100; i++) {
            int randX = mob.getBlockX() + Mth.nextInt(mob.getRandom(), -16, 16);
            int randY = mob.getBlockY() + Mth.nextInt(mob.getRandom(), -16, 16);
            int randZ = mob.getBlockZ() + Mth.nextInt(mob.getRandom(), -16, 16);
            BlockPos pos = new BlockPos(randX, randY, randZ);

            if (mob.level.getBlockState(pos).isLadder(mob.level, pos, mob)) {
                double distance = mob.distanceToSqr(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestLadderPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                }
            }
        }

        return closestLadderPos;
    }

    private void startClimbingLadder(Vec3 ladderPos) {
        climbingLadder = true;
        mob.getNavigation().stop();  // Stop pathfinding while climbing

        // Force mob to snap to the ladder and start climbing
        mob.teleportTo(ladderPos.x, mob.getY(), ladderPos.z);  // Snap to the ladder's X and Z coordinates
        System.out.println("Snapped to ladder at: " + ladderPos);

        double targetY = moveTarget.getY();
        double currentY = mob.getY();

        // Adjust delta movement for climbing
        if (currentY > targetY) {
            mob.setDeltaMovement(0, -0.3, 0);  // Force downward movement
            System.out.println("Climbing down ladder. Delta movement: " + mob.getDeltaMovement());
        } else if (currentY < targetY) {
            mob.setDeltaMovement(0, 0.3, 0);  // Force upward movement
            System.out.println("Climbing up ladder. Delta movement: " + mob.getDeltaMovement());
        }

        // Stop climbing when the mob reaches the target Y level
        if (Math.abs(currentY - targetY) < 0.5) {
            climbingLadder = false;
            System.out.println("Reached target Y level: " + currentY + ", stopping ladder climbing.");
            start();  // Resume pathfinding to the destination
        }
    }

    private boolean isLavaOrFireNearBlock(BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (isLavaOrFireBlock(checkPos)) {
                        System.out.println("Lava or fire found at: " + checkPos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isLavaOrFireBlock(BlockPos pos) {
        BlockState blockState = mob.level.getBlockState(pos);

        // Check if the block is lava, fire, powder snow, or water
        if (blockState.getBlock() == Blocks.LAVA ||
                blockState.getBlock() == Blocks.FIRE ||
                blockState.getBlock() == Blocks.POWDER_SNOW) {

            // If it's water, check the depth of the water
            if (blockState.getBlock() == Blocks.WATER) {
                // Check if there is water below this block (to measure depth)
                int waterDepth = 0;
                BlockPos belowPos = pos.below();

                // Keep checking below the current position to see if there are water blocks
                while (mob.level.getBlockState(belowPos).getBlock() == Blocks.WATER) {
                    waterDepth++;
                    belowPos = belowPos.below();

                    // If the depth is greater than or equal to 2 blocks, return true (indicates danger)
                    if (waterDepth >= 2) {
                        return true;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private boolean isLavaOrFireNearPath(Path path) {
        for (int i = 0; i < path.getNodeCount(); i++) {
            Node node = path.getNode(i);
            BlockPos pathPos = new BlockPos(node.x, node.y, node.z);
            if (isLavaOrFireNearBlock(pathPos)) {
                System.out.println("Hazard detected at path point: " + pathPos);
                return true;
            }
        }
        return false;
    }



    private Path findSafePathTowardsTarget(BlockPos target) {
        System.out.println("Attempting to find safe path towards target: " + target);

        // Check if we're trying the same failed path again
        if (lastFailedTarget != null && lastFailedTarget.equals(target)) {
            repathAttempts++;
            if (repathAttempts > MAX_REPATH_ATTEMPTS) {
                System.out.println("Exceeded maximum repath attempts. Stopping movement.");
                return null;  // Stop if we've exceeded repath attempts
            }
        } else {
            repathAttempts = 0;  // Reset attempts if we have a new target
        }

        // Try to find a path normally
        Path path = mob.getNavigation().createPath(target, moveReachRange);

        if (path == null) {
            System.out.println("No path found, returning null.");
            return null;
        }

        // Check for hazards on the path
        if (isLavaOrFireNearPath(path)) {
            System.out.println("Hazard detected on path, attempting to find a safer path.");
            lastFailedTarget = target;  // Track this target as failed
            return findAlternateSafePath(target);  // Try to find a safer alternative
        }

        // Clear the failed target if we found a safe path
        lastFailedTarget = null;
        return path;
    }

    private Path findAlternateSafePath(BlockPos target) {
        // Radius around the original target to search for alternative safe positions
        int radius = 5;

        // Iterate through nearby positions around the target
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos newTarget = target.offset(dx, 0, dz);
                Path newPath = mob.getNavigation().createPath(newTarget, moveReachRange);

                // If the new path is free of hazards, use it
                if (newPath != null && !isLavaOrFireNearPath(newPath)) {
                    System.out.println("Found alternate safe path: " + newTarget);
                    return newPath;
                }
            }
        }

        // No alternate path found, return null
        System.out.println("No alternate safe path found.");
        return null;
    }

}