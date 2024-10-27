package com.solegendary.reignofnether.unit.interfaces;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.goals.FlyingMoveToTargetGoal;
import com.solegendary.reignofnether.unit.goals.MeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.goals.RangedAttackBuildingGoal;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface AttackerUnit {

    public boolean getWillRetaliate();
    public int getAttackCooldown();
    public float getAttacksPerSecond();
    public float getAggroRange();
    public boolean getAggressiveWhenIdle();
    public float getAttackRange();
    public float getUnitAttackDamage();
    public BlockPos getAttackMoveTarget();
    public boolean canAttackBuildings();

    public Goal getAttackGoal(); // not necessarily the same goal, eg. could be melee or ranged
    public Goal getAttackBuildingGoal();

    // chase and attack the target ignoring all else until it is dead or out of sight
    public default void setUnitAttackTarget(@Nullable LivingEntity target) {
        if (target != null) {
            MiscUtil.addUnitCheckpoint(((Unit) this), target.getId());
            ((Unit) this).setIsCheckpointGreen(false);
        }
        ((Unit) this).getTargetGoal().setTarget(target);
    }
    // move to a building and start attacking it
    public default void setAttackBuildingTarget(BlockPos preselectedBlockPos) {
        if (this.canAttackBuildings()) {
            Goal attackBuildingGoal = this.getAttackBuildingGoal();
            if (attackBuildingGoal instanceof RangedAttackBuildingGoal<?> rabg)
                rabg.setBuildingTarget(preselectedBlockPos);
            else if (attackBuildingGoal instanceof MeleeAttackBuildingGoal mabg)
                mabg.setBuildingTarget(preselectedBlockPos);
        } else {
            Level level = ((LivingEntity) this).getLevel();
            Building building = BuildingUtils.findBuilding(level.isClientSide(), preselectedBlockPos);

            if (building != null) {
                BlockPos groundCentrePos = new BlockPos(building.centrePos.getX(), building.originPos.getY() + 1, building.centrePos.getZ());
                BlockPos targetPos = MyMath.getXZRangeLimitedBlockPos(
                        new BlockPos(groundCentrePos),
                        ((LivingEntity) this).getOnPos(),
                        getAttackRange() - 5
                );
                while (!level.getBlockState(targetPos.above()).isAir())
                    targetPos = targetPos.above();

                ((Unit) this).setMoveTarget(targetPos);
                if (((LivingEntity) this).getLevel().isClientSide)
                    MiscUtil.addUnitCheckpoint((Unit) this, groundCentrePos);
            }
        }
    }

    public static void resetBehaviours(AttackerUnit unit) {
        unit.setUnitAttackTarget(null);
        unit.setAttackMoveTarget(null);

        Goal attackBuildingGoal = unit.getAttackBuildingGoal();
        if (attackBuildingGoal instanceof RangedAttackBuildingGoal<?> rabg)
            rabg.stop();
        else if (attackBuildingGoal instanceof MeleeAttackBuildingGoal mabg)
            mabg.stopAttacking();
    }

    // this setter sets a Unit field and so can't be defaulted
    // move to a block but chase/attack a target if there is one close by (for a limited distance)
    public void setAttackMoveTarget(@Nullable BlockPos bp);

    private static boolean isAttackingBuilding(AttackerUnit attackerUnit) {
        boolean isAttackingBuilding = false;
        Goal attackBuildingGoal = attackerUnit.getAttackBuildingGoal();
        if (attackBuildingGoal instanceof RangedAttackBuildingGoal<?> rabg)
            isAttackingBuilding = rabg.getBuildingTarget() != null;
        else if (attackBuildingGoal instanceof MeleeAttackBuildingGoal mabg)
            isAttackingBuilding = mabg.getBuildingTarget() != null;
        return isAttackingBuilding;
    }

    public static void tick(AttackerUnit attackerUnit) {
        Mob unitMob = (Mob) attackerUnit;
        Unit unit = (Unit) attackerUnit;

        if (!unitMob.level.isClientSide) {
            if (attackerUnit.getAttackGoal() instanceof MeleeAttackUnitGoal meleeAttackUnitGoal)
                meleeAttackUnitGoal.tickAttackCooldown();

            if (attackerUnit.getAttackBuildingGoal() != null && attackerUnit.canAttackBuildings())
                attackerUnit.getAttackBuildingGoal().tick();

            boolean isAttackingBuilding = isAttackingBuilding(attackerUnit);

            // enact attack moving
            // prioritises units and will chase them, resuming attack move once dead or out of range/sight
            if (attackerUnit.getAttackMoveTarget() != null && !unit.hasLivingTarget() && !isAttackingBuilding) {
                attackerUnit.attackClosestEnemy((ServerLevel) unitMob.level);

                if (unit.getTargetGoal().getTarget() == null &&
                    unit.getMoveGoal().getMoveTarget() == null &&
                    !isAttackingBuilding(attackerUnit))
                    unit.setMoveTarget(attackerUnit.getAttackMoveTarget());
            }

            // retaliate against a mob that damaged us UNLESS already on another command
            if (unitMob.getLastDamageSource() != null &&
                    attackerUnit.getWillRetaliate() &&
                    !isAttackingBuilding &&
                    unit.getTargetGoal().getTarget() == null &&
                    (unit.getMoveGoal().getMoveTarget() == null || unit.getHoldPosition()) &&
                    unit.getFollowTarget() == null) {

                Entity lastDSEntity = unitMob.getLastDamageSource().getEntity();

                boolean isMeleeAttackedByFlyingOrGarrisoned = false;
                if (lastDSEntity instanceof Unit unitDS &&
                    (unitDS.getMoveGoal() instanceof FlyingMoveToTargetGoal || GarrisonableBuilding.getGarrison(unitDS) != null) &&
                    attackerUnit.getAttackGoal() instanceof MeleeAttackUnitGoal) {
                    isMeleeAttackedByFlyingOrGarrisoned = true;
                }
                Relationship rs = UnitServerEvents.getUnitToEntityRelationship(unit, lastDSEntity);

                if (!isMeleeAttackedByFlyingOrGarrisoned &&
                    lastDSEntity instanceof LivingEntity &&
                    !(lastDSEntity instanceof Player player && player.isCreative()) &&
                    (rs == Relationship.NEUTRAL || rs == Relationship.HOSTILE)) {
                    attackerUnit.setUnitAttackTarget((LivingEntity) lastDSEntity);
                }
            }
            // enact aggression when idle
            if (attackerUnit.isIdle() && !isAttackingBuilding && attackerUnit.getAggressiveWhenIdle())
                attackerUnit.attackClosestEnemy((ServerLevel) unitMob.level);
        }
        else if (unit instanceof RangedAttackerUnit rangedAttackerUnit) {
            int revealDuration = rangedAttackerUnit.getFogRevealDuration();
            if (revealDuration > 0)
                rangedAttackerUnit.setFogRevealDuration(revealDuration - 1);
        }
    }

    public default boolean isIdle() {
        Unit unit = (Unit) this;
        return this.getAttackMoveTarget() == null &&
                !unit.hasLivingTarget() &&
                unit.getMoveGoal().getMoveTarget() == null &&
                unit.getFollowTarget() == null;
    }

    public default void attackClosestEnemy(ServerLevel level) {
        float aggroRange = this.getAggroRange();
        GarrisonableBuilding garr = GarrisonableBuilding.getGarrison((Unit) this);
        if (garr != null)
            aggroRange  = garr.getAttackRange();

        Mob closestMob = MiscUtil.findClosestAttackableUnit((Mob) this, aggroRange, level);
        if (closestMob != null) {
            ((Unit) this).getMoveGoal().stopMoving();
            setUnitAttackTarget(closestMob);
            return;
        }
        if (canAttackBuildings()) {
            Building closestBuilding = MiscUtil.findClosestAttackableBuilding((Mob) this, aggroRange, level);
            if (closestBuilding != null) {
                ((Unit) this).getMoveGoal().stopMoving();
                setAttackBuildingTarget(closestBuilding.originPos);
            }
        }
    }

    public static double getWeaponDamageModifier(AttackerUnit attackerUnit) {
        ItemStack itemStack = ((LivingEntity) attackerUnit).getItemBySlot(EquipmentSlot.MAINHAND);

        if (!itemStack.isEmpty())
            for(AttributeModifier attr : itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))
                if (attr.getOperation() == AttributeModifier.Operation.ADDITION)
                    return attr.getAmount();
        return 0;
    }
}
