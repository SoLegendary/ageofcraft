package com.solegendary.reignofnether.unit;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.unit.goals.GatherResourcesGoal;
import com.solegendary.reignofnether.unit.goals.ReturnResourcesGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.ConvertableUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.lwjgl.system.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class UnitActionItem {
    private final String ownerName;
    private final UnitAction action;
    private final int unitId; // preselected unit (usually the target)
    private final int[] unitIds; // selected unit(s)
    private final BlockPos preselectedBlockPos;
    private final BlockPos selectedBuildingPos;

    private final List<UnitAction> nonAbilityActions = List.of(
        UnitAction.STOP,
        UnitAction.HOLD,
        UnitAction.GARRISON,
        UnitAction.UNGARRISON,
        UnitAction.MOVE,
        UnitAction.ATTACK_MOVE,
        UnitAction.ATTACK,
        UnitAction.ATTACK_BUILDING,
        UnitAction.FOLLOW,
        UnitAction.BUILD_REPAIR,
        UnitAction.FARM,
        UnitAction.RETURN_RESOURCES,
        UnitAction.RETURN_RESOURCES_TO_CLOSEST,
        UnitAction.DELETE,
        UnitAction.DISCARD
    );

    public UnitActionItem(
            String ownerName,
            UnitAction action,
            int unitId,
            int[] unitIds,
            BlockPos preselectedBlockPos,
            BlockPos selectedBuildingPos) {

        this.ownerName = ownerName;
        this.action = action;
        this.unitId = unitId;
        this.unitIds = unitIds;
        this.preselectedBlockPos = preselectedBlockPos;
        this.selectedBuildingPos = selectedBuildingPos;
    }

    public void resetBehaviours(Unit unit) {
        unit.getCheckpoints().clear();
        unit.setEntityCheckpointId(-1);
        unit.resetBehaviours();
        Unit.resetBehaviours(unit);
        if (unit instanceof WorkerUnit workerUnit)
            WorkerUnit.resetBehaviours(workerUnit);
        if (unit instanceof AttackerUnit attackerUnit)
            AttackerUnit.resetBehaviours(attackerUnit);
    }

    // can be done server or clientside - but only serverside will have an effect on the world
    // clientside actions are purely for tracking data
    public void action(Level level) {
        // filter out unowned units and non-unit entities
        ArrayList<Unit> actionableUnits = new ArrayList<>();
        for (int id : unitIds) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Unit unit && unit.getOwnerName().equals(this.ownerName))
                actionableUnits.add(unit);
        }

        for (Unit unit : actionableUnits) {

            // have to do this before resetBehaviours so we can assign the correct resourceName first
            if (action == UnitAction.TOGGLE_GATHER_TARGET) {
                if (unit instanceof WorkerUnit workerUnit) {
                    GatherResourcesGoal goal = workerUnit.getGatherResourceGoal();
                    ResourceName targetResourceName = goal.getTargetResourceName();
                    resetBehaviours(unit);
                    switch (targetResourceName) {
                        case NONE -> goal.setTargetResourceName(ResourceName.FOOD);
                        case FOOD -> goal.setTargetResourceName(ResourceName.WOOD);
                        case WOOD -> goal.setTargetResourceName(ResourceName.ORE);
                        case ORE -> goal.setTargetResourceName(ResourceName.NONE);
                    }
                }
            }
            else {
                // if we are issuing a redundant unit attack command then don't resetBehaviours or else the unit will pause unnecessarily
                // also don't reset if the action is an ability and the ability wasn't found on this unit
                if ((action != UnitAction.ATTACK || unit.getTargetGoal().getTarget() == null ||
                    unit.getTargetGoal().getTarget().getId() != unitId)) {

                    boolean foundAbility = false;
                    boolean shouldResetBehaviours = true;
                    for (Ability ability : unit.getAbilities()) {
                        if (ability.action == action) {
                            foundAbility = true;
                            shouldResetBehaviours = ability.shouldResetBehaviours();
                            break;
                        }
                    }
                    if (shouldResetBehaviours && (nonAbilityActions.contains(action) || foundAbility))
                        resetBehaviours(unit);
                }
            }
            switch (action) {
                case STOP -> {
                    Entity passenger = ((Entity) unit).getFirstPassenger();
                    if (passenger instanceof Unit unitPassenger)
                        resetBehaviours(unitPassenger);
                }
                case HOLD -> {
                    unit.setHoldPosition(true);
                }
                case GARRISON -> {
                    if (unit.canGarrison())
                        unit.getGarrisonGoal().setBuildingTarget(preselectedBlockPos);
                }
                case UNGARRISON -> {
                    GarrisonableBuilding garr = GarrisonableBuilding.getGarrison(unit);
                    if (garr != null) {
                        Building building = (Building) garr;
                        BlockPos bp = building.originPos.offset(garr.getExitPosition());
                        ((LivingEntity) unit).teleportTo(bp.getX() + 0.5f, bp.getY() + 0.5f, bp.getZ() + 0.5f);
                    }
                }
                case MOVE -> {
                    ResourceName resName = ResourceSources.getBlockResourceName(preselectedBlockPos, level);
                    Building buildingAtPos = BuildingUtils.findBuilding(((Entity) unit).level.isClientSide(), preselectedBlockPos);

                    if (unit instanceof WorkerUnit workerUnit && resName != ResourceName.NONE && buildingAtPos == null) {
                        GatherResourcesGoal goal = workerUnit.getGatherResourceGoal();
                        goal.setTargetResourceName(resName);
                        goal.setMoveTarget(preselectedBlockPos);
                        if (Unit.atMaxResources((Unit) workerUnit)) {
                            if (level.isClientSide())
                                HudClientEvents.showTemporaryMessage("Worker inventory full, dropping off first...");
                            goal.saveAndReturnResources();
                        }
                    } else if (buildingAtPos instanceof Portal portal &&
                            portal.portalType == Portal.PortalType.TRANSPORT &&
                            unit.canUsePortal()) {
                        unit.getUsePortalGoal().setBuildingTarget(preselectedBlockPos);
                    }
                    else
                        unit.setMoveTarget(preselectedBlockPos);
                }
                case ATTACK_MOVE -> {
                    // if the unit can't actually attack just treat this as a move action
                    if (unit instanceof AttackerUnit attackerUnit) {
                        MiscUtil.addUnitCheckpoint(unit, preselectedBlockPos);
                        unit.setIsCheckpointGreen(false);
                        attackerUnit.setAttackMoveTarget(preselectedBlockPos);
                    }
                    else
                        unit.setMoveTarget(preselectedBlockPos);
                }
                case ATTACK -> {
                    // if the unit can't actually attack just treat this as a follow action
                    if (unit instanceof AttackerUnit attackerUnit)
                        attackerUnit.setUnitAttackTarget((LivingEntity) level.getEntity(unitId));
                    else {
                        LivingEntity livingEntity = (LivingEntity) level.getEntity(unitId);
                        if (livingEntity != null) {
                            MiscUtil.addUnitCheckpoint(unit, unitId);
                            unit.setIsCheckpointGreen(true);
                        }
                        unit.setFollowTarget(livingEntity);
                    }
                }
                case ATTACK_BUILDING -> {
                    // if the unit can't actually attack just treat this as a move action
                    if (unit instanceof AttackerUnit attackerUnit)
                        attackerUnit.setAttackBuildingTarget(preselectedBlockPos);
                    else
                        unit.setMoveTarget(preselectedBlockPos);
                }
                case FOLLOW -> {
                    LivingEntity livingEntity = (LivingEntity) level.getEntity(unitId);
                    if (livingEntity != null) {
                        MiscUtil.addUnitCheckpoint(unit, unitId);
                        unit.setIsCheckpointGreen(true);
                    }
                    unit.setFollowTarget(livingEntity);
                }
                case BUILD_REPAIR -> {
                    // if the unit can't actually build/repair just treat this as a move action
                    if (unit instanceof WorkerUnit workerUnit) {
                        Building building = BuildingUtils.findBuilding(level.isClientSide(), preselectedBlockPos);
                        if (building != null)
                            workerUnit.getBuildRepairGoal().setBuildingTarget(building);
                    }
                    else
                        unit.setMoveTarget(preselectedBlockPos);
                }
                case FARM -> {
                    if (unit instanceof WorkerUnit workerUnit) {
                        GatherResourcesGoal goal = workerUnit.getGatherResourceGoal();
                        if (goal != null) {
                            goal.setTargetResourceName(ResourceName.FOOD);
                            goal.setMoveTarget(preselectedBlockPos);
                            Building building = BuildingUtils.findBuilding(level.isClientSide(), preselectedBlockPos);
                            if (building != null && building.name.contains(" Farm")) {
                                goal.setTargetFarm(building);
                                if (Unit.atMaxResources((Unit) workerUnit)) {
                                    if (level.isClientSide())
                                        HudClientEvents.showTemporaryMessage("Worker inventory full, dropping off first...");
                                    goal.saveAndReturnResources();
                                }
                            }
                        }
                    }
                }
                case RETURN_RESOURCES -> {
                    if (unit instanceof WorkerUnit workerUnit) { // if we manually did this, ignore automated return to gather
                        GatherResourcesGoal goal = workerUnit.getGatherResourceGoal();
                        if (goal != null)
                            goal.deleteSavedState();
                    }
                    ReturnResourcesGoal returnResourcesGoal = unit.getReturnResourcesGoal();
                    Building building = BuildingUtils.findBuilding(false, preselectedBlockPos);
                    if (returnResourcesGoal != null && building != null)
                        returnResourcesGoal.setBuildingTarget(building);
                }
                case RETURN_RESOURCES_TO_CLOSEST -> {
                    if (unit instanceof WorkerUnit workerUnit) { // if we manually did this, ignore automated return to gather
                        GatherResourcesGoal goal = workerUnit.getGatherResourceGoal();
                        if (goal != null)
                            goal.deleteSavedState();
                    }
                    ReturnResourcesGoal returnResourcesGoal = unit.getReturnResourcesGoal();
                    if (returnResourcesGoal != null)
                        returnResourcesGoal.returnToClosestBuilding();
                }
                case DELETE -> {
                    ((LivingEntity) unit).kill();
                }
                case DISCARD -> {
                    if (unit instanceof ConvertableUnit cUnit)
                        cUnit.setShouldDiscard(true);
                }

                // any other Ability not explicitly defined here
                default -> {
                    for (Ability ability : unit.getAbilities()) {
                        if (ability.action == action && (ability.isOffCooldown() || ability.canBypassCooldown())) {
                            if (ability.canTargetEntities && this.unitId > 0)
                                ability.use(level, unit, (LivingEntity) level.getEntity(unitId));
                            else
                                ability.use(level, unit, preselectedBlockPos);
                        }
                    }
                }
            }
        }
        if (this.selectedBuildingPos.equals(new BlockPos(0, 0, 0)))
            return;

        Building actionableBuilding = BuildingUtils.findBuilding(level.isClientSide(), this.selectedBuildingPos);

        if (actionableBuilding != null) {
            for (Ability ability : actionableBuilding.getAbilities()) {
                if (ability.action == action && (ability.isOffCooldown() || ability.canBypassCooldown())) {
                    if (ability.canTargetEntities && this.unitId > 0)
                        ability.use(level, actionableBuilding, (LivingEntity) level.getEntity(unitId));
                    else
                        ability.use(level, actionableBuilding, preselectedBlockPos);
                }
            }
        }
    }
}