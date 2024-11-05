package com.solegendary.reignofnether.unit.interfaces;

import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.nether.NetherBlocks;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchFireResistance;
import com.solegendary.reignofnether.research.researchItems.ResearchResourceCapacity;
import com.solegendary.reignofnether.resources.*;
import com.solegendary.reignofnether.time.NightUtils;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import com.solegendary.reignofnether.unit.units.piglins.BruteUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// Defines method bodies for Units
// workaround for trying to have units inherit from both their base vanilla Mob class and a Unit class
// Note that we can't write any default methods if they need to use Unit fields without a getter/setter
// (including getters/setters themselves)

public interface Unit {

    static int PIGLIN_HEALING_TICKS = 8 * ResourceCost.TICKS_PER_SECOND;
    static int MONSTER_HEALING_TICKS = 12 * ResourceCost.TICKS_PER_SECOND;

    // list of positions to draw lines between to indicate unit intents - will fade over time unless shift is held
    public ArrayList<BlockPos> getCheckpoints();
    public int getCheckpointTicksLeft();
    public void setCheckpointTicksLeft(int ticks);
    public boolean isCheckpointGreen();
    public void setIsCheckpointGreen(boolean green);
    public int getEntityCheckpointId();
    public void setEntityCheckpointId(int id);

    public GarrisonGoal getGarrisonGoal();
    public boolean canGarrison();

    public UsePortalGoal getUsePortalGoal();
    public boolean canUsePortal();

    public Faction getFaction();
    public List<AbilityButton> getAbilityButtons();
    public List<Ability> getAbilities();
    public List<ItemStack> getItems();
    public int getMaxResources();

    // note that attackGoal is specific to unit types
    public MoveToTargetBlockGoal getMoveGoal();
    public SelectedTargetGoal<?> getTargetGoal();
    public ReturnResourcesGoal getReturnResourcesGoal();

    public float getMovementSpeed();
    public float getUnitMaxHealth();
    public float getUnitArmorValue();
    public int getPopCost();

    public LivingEntity getFollowTarget();
    public boolean getHoldPosition();
    public void setHoldPosition(boolean holdPosition);

    public String getOwnerName();
    public void setOwnerName(String name);

    public static void tick(Unit unit) {
        Mob unitMob = (Mob) unit;
        if (!unitMob.level.isClientSide() && unitMob.level instanceof ServerLevel serverLevel) {
            ServerChunkCache chunkProvider = serverLevel.getChunkSource();

            BlockPos unitPos = unitMob.blockPosition();
            ChunkPos currentChunkPos = new ChunkPos(unitPos);

            // Load a 2-chunk radius around the unit
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    ChunkPos chunkPos = new ChunkPos(currentChunkPos.x + dx, currentChunkPos.z + dz);
                    chunkProvider.addRegionTicket(TicketType.FORCED, chunkPos, 2, chunkPos);
                }
            }
        }
        for (Ability ability : unit.getAbilities())
            ability.tickCooldown();

        // ------------- CHECKPOINT LOGIC ------------- //
        if (unitMob.level.isClientSide()) {
            if (Keybindings.shiftMod.isDown()) {
                unit.setCheckpointTicksLeft(UnitClientEvents.CHECKPOINT_TICKS_MAX);
            }
            else if (unit.getCheckpointTicksLeft() > 0) {
                unit.setCheckpointTicksLeft(unit.getCheckpointTicksLeft() - 1);
                if (unit.getCheckpointTicksLeft() <= 0) {
                    unit.getCheckpoints().clear();
                    unit.setEntityCheckpointId(-1);
                } else if (unit.getEntityCheckpointId() > -1 && unit.getCheckpointTicksLeft() > UnitClientEvents.CHECKPOINT_TICKS_FADE) {
                    // remove an entity checkpoint if the given entity no longer exists
                    if (Minecraft.getInstance().level != null) {
                        Entity entity = Minecraft.getInstance().level.getEntity(unit.getEntityCheckpointId());
                        if (entity == null)
                            unit.setCheckpointTicksLeft(UnitClientEvents.CHECKPOINT_TICKS_FADE);
                    }
                }
                // remove any BlockPos checkpoints if we're already close enough to them
                if (unit.getCheckpoints().size() > 1) {
                    unit.getCheckpoints().removeIf(bp -> ((Mob) unit).getOnPos().distToCenterSqr(new Vec3(bp.getX(), bp.getY(), bp.getZ())) < 4f);
                } // if we only have one checkpoint, fade it out instead of removing it
                else if (unit.getCheckpoints().size() == 1 && unit.getCheckpointTicksLeft() > UnitClientEvents.CHECKPOINT_TICKS_FADE) {
                    BlockPos bp = unit.getCheckpoints().get(0);
                    if (((Mob) unit).position().distanceToSqr(new Vec3(bp.getX(), bp.getY(), bp.getZ())) < 4f)
                        unit.setCheckpointTicksLeft(UnitClientEvents.CHECKPOINT_TICKS_FADE);
                }

            }
        } else {
            int totalRes = Resources.getTotalResourcesFromItems(unit.getItems()).getTotalValue();
            if (unitMob.canPickUpLoot()) {
                for (ItemEntity itementity : unitMob.level.getEntitiesOfClass(ItemEntity.class, unitMob.getBoundingBox().inflate(1,0,1))) {
                    if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay() && unitMob.isAlive()) {

                        if (!Unit.atMaxResources(unit)) {
                            ItemStack itemstack = itementity.getItem();
                            ResourceSource resBlock = ResourceSources.getFromItem(itemstack.getItem());
                            if (resBlock != null) {
                                while (!Unit.atMaxResources(unit) && itemstack.getCount() > 0) {
                                    unitMob.onItemPickup(itementity);
                                    unitMob.take(itementity, 1);
                                    unit.getItems().add(new ItemStack(itemstack.getItem(), 1));
                                    itemstack.setCount(itemstack.getCount() - 1);
                                }
                                if (itemstack.getCount() <= 0)
                                    itementity.discard();

                                UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
                            }
                            if (Unit.atThresholdResources(unit) && unit instanceof WorkerUnit workerUnit) {
                                GatherResourcesGoal goal = workerUnit.getGatherResourceGoal();
                                if (goal != null && goal.getTargetResourceName() != ResourceName.NONE)
                                    goal.saveAndReturnResources();
                            }
                        }
                    }
                }
            }

            // sync target variables between goals and Mob
            if (unit.getTargetGoal().getTarget() == null || !unit.getTargetGoal().getTarget().isAlive() ||
                    unitMob.getTarget() == null || !unitMob.getTarget().isAlive()) {
                unitMob.setTarget(null);
                unit.getTargetGoal().setTarget(null);
            }

            // no iframes after being damaged so multiple units can attack at once
            unitMob.invulnerableTime = 0;

            // enact target-following, and stop followTarget being reset
            if (unit.getFollowTarget() != null)
                unit.setMoveTarget(unit.getFollowTarget().blockPosition());

            // remove fire from piglin units if they have research
            boolean hasImmunityResearch = ResearchServerEvents.playerHasResearch(unit.getOwnerName(), ResearchFireResistance.itemName);
            if (hasImmunityResearch && unit.getFaction() == Faction.PIGLINS)
                unitMob.setRemainingFireTicks(0);
        }


        // slow regen for monster and piglin units
        LivingEntity le = (LivingEntity) unit;

        if (!le.level.isClientSide()) {
            if (unit.getFaction() == Faction.MONSTERS &&
                    le.tickCount % MONSTER_HEALING_TICKS == 0 &&
                    (!le.level.isDay())) {
                le.heal(1);
            } else if (unit.getFaction() == Faction.MONSTERS &&
                    (le.tickCount + MONSTER_HEALING_TICKS / 2) % MONSTER_HEALING_TICKS == 0 &&
                    (NightUtils.isInRangeOfNightSource(le.position(), le.level.isClientSide()))) {
                le.heal(1);
            } else if (unit.getFaction() == Faction.PIGLINS &&
                    le.tickCount % PIGLIN_HEALING_TICKS == 0 &&
                    (NetherBlocks.isNetherBlock(le.level, le.getOnPos()) || unit instanceof GhastUnit)) {
                le.heal(1);
            }
        }

        if (le.isInWater() && // stuck in bridge
            BuildingUtils.findBuilding(le.level.isClientSide(), le.getOnPos().above()) instanceof AbstractBridge) {
            le.setDeltaMovement(0,0.2,0);
        }

        if (!le.getLevel().getWorldBorder().isWithinBounds(le.getOnPos()))
            le.kill();
    }

    public static boolean atMaxResources(Unit unit) {
        return Resources.getTotalResourcesFromItems(unit.getItems()).getTotalValue() >= unit.getMaxResources();
    }

    public static boolean atThresholdResources(Unit unit) {
        return Resources.getTotalResourcesFromItems(unit.getItems()).getTotalValue() >= 50;
    }

    public default boolean hasLivingTarget() {
        Mob unitMob = (Mob) this;
        return unitMob.getTarget() != null && unitMob.getTarget().isAlive();
    }

    public static void resetBehaviours(Unit unit) {
        unit.getTargetGoal().setTarget(null);
        unit.getMoveGoal().stopMoving();
        if (unit.getReturnResourcesGoal() != null)
            unit.getReturnResourcesGoal().stopReturning();
        unit.setFollowTarget(null);
        unit.setHoldPosition(false);
        if (unit.canGarrison())
            unit.getGarrisonGoal().stopGarrisoning();
        if (unit.canUsePortal())
            unit.getUsePortalGoal().stopUsingPortal();
    }

    // can be overridden in the Unit's class to do additional logic on a reset
    public default void resetBehaviours() { }

    // this setter sets a Unit field and so can't be defaulted
    // move to a block ignoring all else until reaching it
    public default void setMoveTarget(@Nullable BlockPos bp) {
        this.getMoveGoal().setMoveTarget(bp);
    }

    // continuously move to a target until told to do something else
    public void setFollowTarget(@Nullable LivingEntity target);

    public void initialiseGoals();

    // weapons aren't provided automatically when spawned by custom code
    // also recalculate stats based on upgrades
    default void setupEquipmentAndUpgradesServer() { }

    // equipment only needs to be done serverside, but mod-specific fields need to be done clientside too
    default void setupEquipmentAndUpgradesClient() { }

    public static float getSpeedModifier(Unit unit) {
        if (unit instanceof BruteUnit brute && brute.isHoldingUpShield) {
            return 0.5f;
        }
        return 1.0f;
    }
}
