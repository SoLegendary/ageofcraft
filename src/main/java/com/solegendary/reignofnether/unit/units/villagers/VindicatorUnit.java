package com.solegendary.reignofnether.unit.units.villagers;

import com.solegendary.reignofnether.ability.abilities.PromoteIllager;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchVindicatorAxes;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VindicatorUnit extends Vindicator implements Unit, AttackerUnit {
    // region
    private final ArrayList<BlockPos> checkpoints = new ArrayList<>();
    private int checkpointTicksLeft = UnitClientEvents.CHECKPOINT_TICKS_MAX;
    public ArrayList<BlockPos> getCheckpoints() { return checkpoints; };
    public int getCheckpointTicksLeft() { return checkpointTicksLeft; }
    public void setCheckpointTicksLeft(int ticks) { checkpointTicksLeft = ticks; }
    private boolean isCheckpointGreen = true;
    public boolean isCheckpointGreen() { return isCheckpointGreen; };
    public void setIsCheckpointGreen(boolean green) { isCheckpointGreen = green; };
    private int entityCheckpointId = -1;
    public int getEntityCheckpointId() { return entityCheckpointId; };
    public void setEntityCheckpointId(int id) { entityCheckpointId = id; };

    GarrisonGoal garrisonGoal;
    public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }
    public boolean canGarrison() { return getGarrisonGoal() != null; }

    UsePortalGoal usePortalGoal;
    public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }
    public boolean canUsePortal() { return getUsePortalGoal() != null; }

    public Faction getFaction() {return Faction.VILLAGERS;}
    public List<AbilityButton> getAbilityButtons() {return abilityButtons;};
    public List<Ability> getAbilities() {return abilities;}
    public List<ItemStack> getItems() {return items;};
    public MoveToTargetBlockGoal getMoveGoal() {return moveGoal;}
    public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() {return targetGoal;}
    public Goal getAttackBuildingGoal() {return attackBuildingGoal;}
    public Goal getAttackGoal() {return attackGoal;}
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    private ReturnResourcesGoal returnResourcesGoal;

    public BlockPos getAttackMoveTarget() { return attackMoveTarget; }
    public LivingEntity getFollowTarget() { return followTarget; }
    public boolean getHoldPosition() { return holdPosition; }
    public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

    // if true causes moveGoal and attackGoal to work together to allow attack moving
    // moves to a block but will chase/attack nearby monsters in range up to a certain distance away
    private BlockPos attackMoveTarget = null;
    private LivingEntity followTarget = null; // if nonnull, continuously moves to the target
    private boolean holdPosition = false;

    // which player owns this unit? this format ensures its synched to client without having to use packets
    public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
    public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
    public static final EntityDataAccessor<String> ownerDataAccessor =
            SynchedEntityData.defineId(VindicatorUnit.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
    }

    // combat stats
    public boolean getWillRetaliate() {return willRetaliate;}
    public int getAttackCooldown() {return (int) (20 / attacksPerSecond);}
    public float getAttacksPerSecond() {return attacksPerSecond;}
    public float getAggroRange() {return aggroRange;}
    public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
    public float getAttackRange() {return attackRange;}
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitAttackDamage() {return attackDamage;}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    public int getPopCost() {return popCost;}
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // endregion

    final static public float attackDamage = 5.0f;
    final static public float attacksPerSecond = 0.5f;
    final static public float maxHealth = 60.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.28f;
    final static public float attackRange = 2; // only used by ranged units or melee building attackers
    final static public float aggroRange = 10;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;
    final static public int popCost = ResourceCosts.VINDICATOR.population;

    public int maxResources = 100;

    private MeleeAttackUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public VindicatorUnit(EntityType<? extends Vindicator> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    // all for animation syncing...
    @Override
    public void setUnitAttackTarget(@Nullable LivingEntity target) {
        AttackerUnit.super.setUnitAttackTarget(target);
        if (!this.level.isClientSide()) {
            if (target != null)
                UnitSyncClientboundPacket.sendSyncAnimationPacket(this, target, true);
            else
                UnitSyncClientboundPacket.sendSyncAnimationPacket(this, false);
        }
    }
    @Override
    public void setAttackBuildingTarget(BlockPos preselectedBlockPos) {
        AttackerUnit.super.setAttackBuildingTarget(preselectedBlockPos);
        if (!this.level.isClientSide())
             UnitSyncClientboundPacket.sendSyncAnimationPacket(this, preselectedBlockPos, true);
    }
    @Override
    public void resetBehaviours() {
        if (!this.level.isClientSide())
            UnitSyncClientboundPacket.sendSyncAnimationPacket(this, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, VindicatorUnit.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, VindicatorUnit.attackDamage)
                .add(Attributes.ARMOR, VindicatorUnit.armorValue)
                .add(Attributes.MAX_HEALTH, VindicatorUnit.maxHealth);
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);
        PromoteIllager.checkAndApplyBuff(this);
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new MeleeAttackUnitGoal(this, getAttackCooldown(), false);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.targetSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    public void setupEquipmentAndUpgradesClient() {
        // weapon is purely visual, damage is based solely on entity attribute ATTACK_DAMAGE
        Item axe = Items.IRON_AXE;
        if (ResearchClient.hasResearch(ResearchVindicatorAxes.itemName))
            axe = Items.DIAMOND_AXE;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(axe));
    }

    @Override
    public void setupEquipmentAndUpgradesServer() {
        // weapon is purely visual, damage is based solely on entity attribute ATTACK_DAMAGE
        Item axe = Items.IRON_AXE;
        int damageMod = 0;
        if (ResearchServerEvents.playerHasResearch(this.getOwnerName(), ResearchVindicatorAxes.itemName)) {
            axe = Items.DIAMOND_AXE;
            damageMod = 2;
        }
        ItemStack axeStack = new ItemStack(axe);
        AttributeModifier mod = new AttributeModifier(UUID.randomUUID().toString(), damageMod, AttributeModifier.Operation.ADDITION);
        axeStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, mod, EquipmentSlot.MAINHAND);

        this.setItemSlot(EquipmentSlot.MAINHAND, axeStack);
    }
}
