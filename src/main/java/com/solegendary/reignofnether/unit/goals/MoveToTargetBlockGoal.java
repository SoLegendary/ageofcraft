package com.solegendary.reignofnether.unit.goals;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoveToTargetBlockGoal extends Goal {

    private static final Map<Pair<BlockPos, BlockPos>, Path> pathCache = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 200;
    private static final TagKey<Block> BREAKABLE_BLOCKS_TAG =
            BlockTags.create(new ResourceLocation("minecraft", "breakable_blocks"));

    private static final int BLOCK_BREAK_PENALTY = 20; // Penalty for breaking a block
    private static final long BLOCK_BREAK_COOLDOWN = 1000; // 1 second cooldown

    protected final Mob mob;
    protected BlockPos moveTarget = null;
    protected boolean persistent;
    protected int moveReachRange;

    private static final int MAX_RETRIES = 10;
    private int retries = 0;
    private long lastPathTime = 0;
    private static final long PATH_TIMEOUT = 10000;
    private long lastBlockBreakTime = 0;

    private final Set<BlockPos> failedPositions = new HashSet<>();
    private final Map<BlockPos, Integer> dangerZones = new HashMap<>();
    private static final int MAX_SEARCH_RADIUS = 100;
    private int currentSearchRadius = ALTERNATIVE_SEARCH_RADIUS;
    private static final int RADIUS_INCREMENT = 10;
    private static final int ALTERNATIVE_SEARCH_RADIUS = 20;
    private static final int WATER_PENALTY = 50;
    private static final Logger LOGGER = Logger.getLogger(MoveToTargetBlockGoal.class.getName());
    private static final int BYPASS_RADIUS = 3;

    public MoveToTargetBlockGoal(Mob mob, boolean persistent, int reachRange) {
        this.mob = mob;
        this.persistent = persistent;
        this.moveReachRange = reachRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean isAtDestination() {
        return moveTarget == null || mob.getNavigation().isDone();
    }

    @Override
    public boolean canUse() {
        return moveTarget != null;
    }


    @Override
    public boolean canContinueToUse() {
        if (mob.getNavigation().isDone()) {
            if (moveTarget != null && mob.getOnPos().distSqr(moveTarget) > moveReachRange) {
                retryPathfinding();
                return true;
            }
            if (!persistent && !((Unit) mob).getHoldPosition()) {
                moveTarget = null;
            }
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        if (moveTarget != null && !failedPositions.contains(moveTarget)) {
            createPathAndMove();
        } else {
            findAndSetBestAlternativeTarget();
        }
    }

    private void createPathAndMove() {
        if (isPathClearOfWater(mob.getOnPos(), moveTarget)) {
            Path path = getPathFromCacheOrCreate(mob.getOnPos(), moveTarget);
            if (path != null && isPathValid(path)) {
                mob.getNavigation().moveTo(path, Unit.getSpeedModifier((Unit) mob));
                retries = 0;
                lastPathTime = System.currentTimeMillis();
            } else {
                markPositionAsFailed();
            }
        } else {
            logDecision("Water detected along the path. Attempting reroute.");
            findAndSetBestAlternativeTarget();
        }
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
        if (this.mob.isVehicle() && this.mob.getPassengers().get(0) instanceof Unit unit) {
            unit.getMoveGoal().stopMoving();
        }
    }

    private void retryPathfinding() {
        if (retries < MAX_RETRIES && System.currentTimeMillis() - lastPathTime > PATH_TIMEOUT) {
            retries++;
            logDecision("Retrying pathfinding. Attempt #" + retries);
            start();
        } else {
            markPositionAsFailed();
        }
    }
    private boolean isBreakableBlock(BlockPos pos) {
        return mob.level.getBlockState(pos).is(BREAKABLE_BLOCKS_TAG);
    }

    private void breakBlockIfNeeded(BlockPos pos) {
        if (isBreakableBlock(pos) && System.currentTimeMillis() - lastBlockBreakTime >= BLOCK_BREAK_COOLDOWN) {
            mob.level.destroyBlock(pos, true); // Break the block and drop items
            logDecision("Broke block at: " + pos);
            lastBlockBreakTime = System.currentTimeMillis();
        }
    }

    private void markPositionAsFailed() {
        if (moveTarget != null) {
            logDecision("Failed to reach target: " + moveTarget);
            failedPositions.add(moveTarget);
            moveTarget = null;
            mob.getNavigation().stop();
        }
    }

    private void findAndSetBestAlternativeTarget() {
        if (moveTarget == null) return;

        List<BlockPos> alternatives = generateNearbyPositions(moveTarget, currentSearchRadius)
                .filter(pos -> !failedPositions.contains(pos))
                .sorted(Comparator.comparingInt(this::calculateWeightedPathCost))
                .toList();

        if (!alternatives.isEmpty()) {
            BlockPos selected = alternatives.get(0); // Choose the best path
            Path path = getPathFromCacheOrCreate(mob.getOnPos(), selected);

            if (path != null && isPathValid(path)) {
                moveTarget = selected;
                mob.getNavigation().moveTo(path, Unit.getSpeedModifier((Unit) mob));
                retries = 0;
                lastPathTime = System.currentTimeMillis();
            } else {
                markPositionAsFailed();
            }
        }
    }

    private Path getPathFromCacheOrCreate(BlockPos from, BlockPos to) {
        Pair<BlockPos, BlockPos> key = Pair.of(from, to);
        if (pathCache.containsKey(key)) {
            return pathCache.get(key);
        }

        Path path = mob.getNavigation().createPath(to, moveReachRange);
        if (path != null) {
            cachePath(key, path);
        }
        return path;
    }

    private void cachePath(Pair<BlockPos, BlockPos> key, Path path) {
        if (pathCache.size() >= MAX_CACHE_SIZE) {
            pathCache.remove(pathCache.keySet().iterator().next());
        }
        pathCache.put(key, path);
    }

    private boolean isPathValid(Path path) {
        for (int i = 0; i < path.getNodeCount(); i++) {
            Node node = path.getNode(i);
            BlockPos pos = new BlockPos(node.x, node.y, node.z);

            if (isOverWater(pos)) {
                logDecision("Water detected at " + pos);
                if (tryBypassWater(pos)) {
                    logDecision("Bypassed water at: " + pos);
                    return false;  // Detour successfully initiated
                }

                markWaterAsFailed(pos);
                escalateSearchRadius();
                findAndSetBestAlternativeTarget();
                return false;
            }

            if (isBreakableBlock(pos)) {
                breakBlockIfNeeded(pos);
            }
        }
        return true;
    }

    private boolean isPathClearOfWater(BlockPos from, BlockPos to) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (isOverWater(pos)) {
                return false;
            }
        }
        return true;
    }
    private boolean tryBypassWater(BlockPos waterPos) {
        List<BlockPos> detourPositions = generateNearbyPositions(waterPos, BYPASS_RADIUS)
                .filter(pos -> !failedPositions.contains(pos) && isWalkable(pos))
                .sorted(Comparator.comparingInt(this::calculateWeightedPathCost))
                .toList();

        if (!detourPositions.isEmpty()) {
            BlockPos detour = detourPositions.get(0); // Select the best detour
            Path detourPath = getPathFromCacheOrCreate(mob.getOnPos(), detour);

            if (detourPath != null && isPathValid(detourPath)) {
                mob.getNavigation().moveTo(detourPath, Unit.getSpeedModifier((Unit) mob));
                retries = 0;
                lastPathTime = System.currentTimeMillis();
                return true;
            }
        }

        return false;
    }

    private boolean isWalkable(BlockPos pos) {
        return !mob.level.getBlockState(pos).isAir() &&
                !isOverWater(pos) &&
                mob.level.getBlockState(pos).canOcclude();
    }

    private void markWaterAsFailed(BlockPos waterPos) {
        failedPositions.add(waterPos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos neighbor = waterPos.offset(dx, 0, dz);
                if (isOverWater(neighbor)) {
                    failedPositions.add(neighbor);
                    logDecision("Marked neighboring water block as failed: " + neighbor);
                }
            }
        }
    }
    private void escalateSearchRadius() {
        if (currentSearchRadius < MAX_SEARCH_RADIUS) {
            currentSearchRadius += RADIUS_INCREMENT;
            logDecision("Increasing search radius to: " + currentSearchRadius);
        } else {
            logDecision("Max search radius reached. Giving up on further expansion.");
        }
    }

    private int calculateWeightedPathCost(BlockPos pos) {
        int dangerPenalty = dangerZones.getOrDefault(pos, 0);
        int pathCost = pos.distManhattan(moveTarget);

        if (isOverWater(pos)) {
            pathCost += WATER_PENALTY;
        }

        if (isBreakableBlock(pos)) {
            pathCost += BLOCK_BREAK_PENALTY; // Add block break penalty to cost
        }

        return pathCost + dangerPenalty;
    }

    private boolean isOverWater(BlockPos pos) {
        return mob.level.getBlockState(pos.below()).is(Blocks.WATER);
    }

    private boolean isBridgePath(BlockPos pos) {
        return mob.level.getBlockState(pos.below()).is(Blocks.OAK_PLANKS);
    }

    private Stream<BlockPos> generateNearbyPositions(BlockPos center, int radius) {
        return Stream.iterate(-radius, x -> x <= radius, x -> x + 1)
                .flatMap(x -> Stream.iterate(-radius, y -> y <= radius, y -> y + 1)
                        .flatMap(y -> Stream.iterate(-radius, z -> z <= radius, z -> z + 1)
                                .map(z -> center.offset(x, y, z))));
    }


    private void logDecision(String message) {
        LOGGER.info("[MoveToTargetBlockGoal] " + message);
    }
}
