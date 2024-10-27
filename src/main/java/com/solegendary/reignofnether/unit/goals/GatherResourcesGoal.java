package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.*;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.solegendary.reignofnether.resources.BlockUtils.isLogBlock;

// Move towards the nearest open resource blocks and start gathering them
// Can be toggled between food, wood and ore, and disabled by clicking

public class GatherResourcesGoal extends MoveToTargetBlockGoal {

    private static final int REACH_RANGE = 5;
    private static final int DEFAULT_MAX_GATHER_TICKS = 600; // ticks to gather blocks - actual ticks may be lower, depending on the ResourceSource targeted
    private int gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;
    private static final int MAX_SEARCH_CD_TICKS = 40; // while idle, worker will look for a new block once every this number of ticks (searching is expensive!)
    private int searchCdTicksLeft = 0;
    private int failedSearches = 0; // number of times we've failed to search for a new block - as this increases slow down or stop searching entirely to prevent lag
    private static final int MAX_FAILED_SEARCHES = 3;
    private static final int TICK_CD = 20; // only tick down gather time once this many ticks to reduce processing requirements
    private int cdTicksLeft = TICK_CD;
    public static final int NO_TARGET_TIMEOUT = 50; // if we reach this time without progressing a gather tick while having navigation done, then switch a new target
    public static final int IDLE_TIMEOUT = 300; // ticks spent without a target to be considered idle
    private int ticksWithoutTarget = 0; // ticks spent without an active gather target (only increments serverside)
    private int ticksIdle = 0; // ticksWithoutTarget but never reset unless we've reacquired a target - used for idle checks
    private BlockPos altSearchPos = null; // block search origin that may be used instead of the mob position


    private final ArrayList<BlockPos> todoGatherTargets = new ArrayList<>();
    private BlockPos gatherTarget = null;
    private ResourceName targetResourceName = ResourceName.NONE; // if !None, will passively target blocks around it
    private ResourceSource targetResourceSource = null;
    private Building targetFarm = null;

    // saved copies of the above so we can later return to
    private final ArrayList<BlockPos> todoGatherTargetsSaved = new ArrayList<>();
    private BlockPos gatherTargetSaved = null;
    private ResourceName targetResourceNameSaved = ResourceName.NONE;
    private ResourceSource targetResourceSourceSaved = null;
    private Building targetFarmSaved = null;

    // whenever we attempt to assign a block as a target it must pass this test
    private final Predicate<BlockPos> BLOCK_CONDITION = bp -> {
        BlockState bs = mob.level.getBlockState(bp);
        BlockState bsAbove = mob.level.getBlockState(bp.above());
        ResourceSource resBlock = ResourceSources.getFromBlockPos(bp, mob.level);

        if (!mob.level.getWorldBorder().isWithinBounds(bp))
            return false;

        // is a valid resource block and meets the target ResourceSource's blockstate condition
        if (resBlock == null || resBlock.resourceName != targetResourceName) // || resBlock.name.equals("Leaves")
            return false;
        if (!resBlock.blockStateTest.test(bs))
            return false;

        // if the worker is farming, stick to only the assigned farm
        if (targetFarm != null && !targetFarm.isPosInsideBuilding(bp))
            return false;

        if (bs.getBlock() == Blocks.FARMLAND || bs.getBlock() == Blocks.SOUL_SAND) {
            if (!bsAbove.isAir() || !canAffordReplant() || !BuildingUtils.isPosInsideAnyBuilding(mob.level.isClientSide(), bp))
                return false;
        }
        // is not part of a building (unless farming)
        else if (targetFarm == null && BuildingUtils.isPosInsideAnyBuilding(mob.level.isClientSide(), bp))
            return false;

        // not covered by solid blocks
        boolean hasClearNeighbour = false;
        for (BlockPos adjBp : List.of(bp.above(), bp.north(), bp.south(), bp.east(), bp.west())) {
            if (ResourceSources.CLEAR_MATERIALS.contains(mob.level.getBlockState(adjBp).getMaterial())) {
                hasClearNeighbour = true;
                break;
            }
        }
        if (!hasClearNeighbour)
            return false;

        // not targeted by another nearby worker
        AABB aabb = AABB.ofSize(this.mob.position(), REACH_RANGE * 2,REACH_RANGE * 2,REACH_RANGE * 2);
        for (LivingEntity entity : this.mob.level.getNearbyEntities(LivingEntity.class, TargetingConditions.forNonCombat(), this.mob, aabb)) {
            if (entity instanceof Unit unit) {
                if (unit instanceof WorkerUnit workerUnit && workerUnit.getGatherResourceGoal() != null && entity.getId() != this.mob.getId()) {
                    BlockPos otherUnitTarget = workerUnit.getGatherResourceGoal().getGatherTarget();
                    if (otherUnitTarget != null && otherUnitTarget.equals(bp)) {
                        altSearchPos = bp;
                        return false;
                    }
                }
            }
        }
        return true;
    };

    // set move goal as range -1, so we aren't slightly out of range
    public GatherResourcesGoal(Mob mob) {
        super(mob, true, REACH_RANGE - 1);
    }

    public void syncFromServer(ResourceName gatherName, BlockPos gatherPos, int gatherTicks) {
        this.targetResourceName = gatherName;
        this.gatherTarget = gatherPos;
        this.gatherTicksLeft = gatherTicks;
        this.targetResourceSource = ResourceSources.getFromBlockPos(gatherTarget, mob.level);
    }

    public void tickClient() {
        if (targetResourceSource != null && this.gatherTarget != null && isGathering() && FogOfWarClientEvents.isInBrightChunk(this.gatherTarget)) {
            gatherTicksLeft = Math.min(gatherTicksLeft, targetResourceSource.ticksToGather);
            gatherTicksLeft -= 1;
            if (gatherTicksLeft <= 0)
                gatherTicksLeft = targetResourceSource.ticksToGather;
            int gatherProgress = Math.round((targetResourceSource.ticksToGather - gatherTicksLeft) / (float) targetResourceSource.ticksToGather * 10);
            this.mob.level.destroyBlockProgress(this.mob.getId(), this.gatherTarget, gatherProgress);
        }
    }

    // move towards the targeted block and start gathering it
    public void tick() {
        if (this.mob.level.isClientSide()) {
            tickClient();
            return;
        }

        cdTicksLeft -= 1;
        if (cdTicksLeft <= 0)
            cdTicksLeft = TICK_CD;
        else
            return;

        if (gatherTarget == null && targetResourceName != ResourceName.NONE) {
            searchCdTicksLeft -= (TICK_CD / 2); // for some this is run twice as fast as we expect

            // prioritise gathering adjacent targets first
            for (BlockPos todoBp : todoGatherTargets)
                if (BLOCK_CONDITION.test(todoBp)) {
                    gatherTarget = todoBp;
                    break;
                }

            if (gatherTarget != null)
                todoGatherTargets.remove(gatherTarget);

            if (gatherTarget == null && searchCdTicksLeft <= 0) {
                if (targetFarm != null) {
                    for (BuildingBlock block : targetFarm.getBlocks()) {
                        if (BLOCK_CONDITION.test(block.getBlockPos())) {
                            gatherTarget = block.getBlockPos();
                            break;
                        }
                    }
                }
                else {
                    Optional<BlockPos> bpOpt;
                    if (altSearchPos != null) {
                        bpOpt = BlockPos.findClosestMatch(
                                altSearchPos, REACH_RANGE/2, REACH_RANGE/2,
                            BLOCK_CONDITION);
                        altSearchPos = null;
                    }
                    else {
                        // increase search range until we've maxed out (to prevent idle workers using up too much CPU)
                        int range = REACH_RANGE * (failedSearches + 1);
                        if (failedSearches == MAX_FAILED_SEARCHES)
                            range = REACH_RANGE;

                        bpOpt = BlockPos.findClosestMatch(
                            new BlockPos(
                                mob.getEyePosition().x,
                                mob.getEyePosition().y,
                                mob.getEyePosition().z
                            ), range, range,
                            BLOCK_CONDITION);
                    }

                    bpOpt.ifPresentOrElse(
                        blockPos -> {
                            gatherTarget = blockPos;
                            failedSearches = 0;
                        },
                        () -> {
                            if (failedSearches < MAX_FAILED_SEARCHES)
                                failedSearches += 1;
                        }
                    );
                }
                searchCdTicksLeft = MAX_SEARCH_CD_TICKS * (failedSearches + 1);
            }
            if (gatherTarget != null)
                targetResourceSource = ResourceSources.getFromBlockPos(gatherTarget, mob.level);
        }

        if (gatherTarget != null) {

            // if the block is no longer valid (destroyed or somehow badly targeted)
            if (!BLOCK_CONDITION.test(this.gatherTarget))
                removeGatherTarget();
            else // keep persistently moving towards the target
                this.setMoveTarget(gatherTarget);

            if (isGathering()) {
                ticksIdle = 0;

                // need to manually set cooldown higher (default is 2) or else we don't have enough time
                // for the mob to turn before behaviour is reset
                mob.getLookControl().setLookAt(gatherTarget.getX(), gatherTarget.getY(), gatherTarget.getZ());
                mob.getLookControl().lookAtCooldown = 20;

                BlockState bsTarget = mob.level.getBlockState(gatherTarget);

                // replant crops on empty farmland
                if (bsTarget.getBlock() == Blocks.FARMLAND || bsTarget.getBlock() == Blocks.SOUL_SAND) {
                    gatherTicksLeft -= (TICK_CD / 2);
                    gatherTicksLeft = Math.min(gatherTicksLeft, ResourceSources.REPLANT_TICKS_MAX);
                    if (gatherTicksLeft <= 0) {
                        gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;

                        if (canAffordReplant()) {
                            ResourcesServerEvents.addSubtractResources(new Resources(((Unit) mob).getOwnerName(), 0, -ResourceCosts.REPLANT_WOOD_COST, 0));
                            mob.level.setBlockAndUpdate(gatherTarget.above(), ((WorkerUnit) mob).getReplantBlockState());
                            removeGatherTarget();
                        }
                    }
                }
                else {
                    if (ResearchServerEvents.playerHasCheat(((Unit) mob).getOwnerName(), "operationcwal"))
                        this.gatherTicksLeft -= (TICK_CD / 2) * 10;
                    else
                        this.gatherTicksLeft -= (TICK_CD / 2);

                    gatherTicksLeft = Math.min(gatherTicksLeft, targetResourceSource.ticksToGather);
                    if (gatherTicksLeft <= 0) {
                        gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;
                        ResourceName resourceName = ResourceSources.getBlockResourceName(this.gatherTarget, mob.level);

                        if (isLogBlock(this.mob.level.getBlockState(gatherTarget)))
                            ResourcesServerEvents.fellAdjacentLogs(gatherTarget, new ArrayList<>(), this.mob.level);

                        if (mob.level.destroyBlock(gatherTarget, false)) {
                            // replace workers' mine ores with cobble to prevent creating potholes
                            if (targetResourceSource.resourceName == ResourceName.ORE) {
                                BlockState replaceBs;
                                if (BuildingUtils.isInNetherRange(mob.level.isClientSide(), gatherTarget))
                                    replaceBs = Blocks.MAGMA_BLOCK.defaultBlockState();
                                else if (bsTarget.getBlock().getName().getString().toLowerCase().contains("deepslate"))
                                    replaceBs = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
                                else
                                    replaceBs = Blocks.COBBLESTONE.defaultBlockState();
                                this.mob.level.setBlockAndUpdate(gatherTarget, replaceBs);
                            }

                            // prioritise gathering adjacent targets first
                            todoGatherTargets.remove(gatherTarget);
                            for (BlockPos pos : MiscUtil.findAdjacentBlocks(gatherTarget, BLOCK_CONDITION))
                                if (!todoGatherTargets.contains(pos))
                                    todoGatherTargets.add(pos);

                            Unit unit = (Unit) mob;
                            unit.getItems().add(new ItemStack(targetResourceSource.items.get(0)));
                            UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);

                            // if at max resources, go to drop off automatically, then return to this gather goal
                            if (Unit.atThresholdResources(unit))
                                saveAndReturnResources();

                            removeGatherTarget();
                        }
                    }
                }
            }
            else {
                // track how long we've been without a target
                // if we have spent too long still then we are stuck andreevaulate our gather target
                if (mob.getNavigation().isDone())
                    ticksWithoutTarget += (TICK_CD / 2);
                if (ticksWithoutTarget >= NO_TARGET_TIMEOUT)
                    this.removeGatherTarget();
            }
        } else {
            ticksIdle += (TICK_CD / 2);
        }
    }

    public void saveAndReturnResources() {
        Unit unit = (Unit) mob;
        if (unit.getReturnResourcesGoal() != null) {
            this.saveState();
            unit.resetBehaviours();
            WorkerUnit.resetBehaviours((WorkerUnit) unit);
            unit.getReturnResourcesGoal().returnToClosestBuilding();
        }
    }

    private void saveState() {
        todoGatherTargetsSaved.clear();
        todoGatherTargetsSaved.addAll(todoGatherTargets);
        gatherTargetSaved = gatherTarget;
        targetResourceNameSaved = targetResourceName;
        targetResourceSourceSaved = targetResourceSource;
        targetFarmSaved = targetFarm;
    }
    public void loadState() {
        todoGatherTargets.clear();
        todoGatherTargets.addAll(todoGatherTargetsSaved);
        gatherTarget = gatherTargetSaved;
        targetResourceName = targetResourceNameSaved;
        targetResourceSource = targetResourceSourceSaved;
        targetFarm = targetFarmSaved;
    }
    public boolean hasSavedData() {
        return todoGatherTargetsSaved.size() > 0 ||
                gatherTargetSaved != null ||
                targetResourceNameSaved != ResourceName.NONE ||
                targetResourceSourceSaved != null ||
                targetFarmSaved != null;
    }
    public void deleteSavedState() {
        todoGatherTargetsSaved.clear();
        gatherTargetSaved = null;
        targetResourceNameSaved = ResourceName.NONE;
        targetResourceSourceSaved = null;
        targetFarmSaved = null;
    }

    private boolean isBlockInRange(BlockPos target) {
        int reachRangeBonus = Math.min(5, ticksWithoutTarget / TICK_CD);
        return target.distToCenterSqr(mob.getX(), mob.getEyeY(), mob.getZ()) <= Math.pow(REACH_RANGE + reachRangeBonus, 2);
    }

    // only count as gathering if in range of the target
    public boolean isGathering() {
        if (!Unit.atMaxResources((Unit) mob) && gatherTarget != null && this.mob.level.isClientSide())
            return isBlockInRange(gatherTarget);

        if (!Unit.atMaxResources((Unit) mob) && this.gatherTarget != null && this.targetResourceSource != null &&
            ResourceSources.getBlockResourceName(this.gatherTarget, mob.level) != ResourceName.NONE)
            return isBlockInRange(gatherTarget);
        return false;
    }

    private boolean canAffordReplant() {
        return ResourcesServerEvents.canAfford(((Unit) mob).getOwnerName(), ResourceName.WOOD, ResourceCosts.REPLANT_WOOD_COST);
    }

    public void setTargetResourceName(ResourceName resourceName) {
        targetResourceName = resourceName;
    }

    public ResourceName getTargetResourceName() {
        return targetResourceName;
    }

    @Override
    public void setMoveTarget(@Nullable BlockPos bp) {
        if (bp != null) {
            MiscUtil.addUnitCheckpoint((Unit) mob, bp);
            ((Unit) mob).setIsCheckpointGreen(true);
        }
        super.setMoveTarget(bp);
        if (BLOCK_CONDITION.test(bp)) {
            this.gatherTarget = bp;
            this.targetResourceSource = ResourceSources.getFromBlockPos(gatherTarget, this.mob.level);
        }
    }

    public boolean isFarming() {
        return this.targetFarm != null;
    }

    // locks the worker to only gather from this specific building
    public void setTargetFarm(Building building) {
        if (building != null) {
            MiscUtil.addUnitCheckpoint((Unit) mob, building.centrePos);
            ((Unit) mob).setIsCheckpointGreen(true);
        }
        this.targetFarm = building;
    }

    // stop attempting to gather the current target but continue searching
    public void removeGatherTarget() {
        gatherTarget = null;
        targetResourceSource = null;
        gatherTicksLeft = DEFAULT_MAX_GATHER_TICKS;
        searchCdTicksLeft = 0;
        ticksWithoutTarget = 0;
    }

    // stop gathering and searching entirely, and remove saved data for
    public void stopGathering() {
        this.mob.level.destroyBlockProgress(this.mob.getId(), new BlockPos(0,0,0), 0);
        todoGatherTargets.clear();
        targetFarm = null;
        removeGatherTarget();
        this.setTargetResourceName(ResourceName.NONE);
        super.stopMoving();
    }

    public BlockPos getGatherTarget() {
        return gatherTarget;
    }

    public int getGatherTicksLeft() {
        return gatherTicksLeft;
    }

    public boolean isIdle() {
        return ticksIdle > IDLE_TIMEOUT;
    }
}
