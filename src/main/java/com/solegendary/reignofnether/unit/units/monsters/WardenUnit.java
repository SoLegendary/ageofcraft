package com.solegendary.reignofnether.unit.units.monsters;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.SonicBoom;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.buildings.monsters.SculkCatalyst;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchSculkAmplifiers;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.time.NightUtils;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WardenUnit extends Warden implements Unit, AttackerUnit {
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

    public Faction getFaction() {return Faction.MONSTERS;}
    public List<AbilityButton> getAbilityButtons() {return abilityButtons;};
    public List<Ability> getAbilities() {return abilities;};
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
            SynchedEntityData.defineId(WardenUnit.class, EntityDataSerializers.STRING);

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

    final static public float attackDamage = 8.0f;
    final static public float attacksPerSecond = 0.6f;
    final static public float maxHealth = 150.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.28f;
    final static public float attackRange = 2; // only used by ranged units or melee building attackers
    final static public float aggroRange = 10;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;
    final static public int popCost = ResourceCosts.WARDEN.population;

    public int maxResources = 100;

    private MeleeAttackUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;
    private SonicBoomGoal sonicBoomGoal;

    public SonicBoomGoal getSonicBoomGoal() { return sonicBoomGoal; }

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public static final float SONIC_BOOM_DAMAGE = 75f;
    public static final int SONIC_BOOM_RANGE = 10;
    public static final int SONIC_BOOM_CHANNEL_TICKS = 2 * ResourceCost.TICKS_PER_SECOND;

    public WardenUnit(EntityType<? extends Warden> entityType, Level level) {
        super(entityType, level);

        SonicBoom ab1 = new SonicBoom(this);
        this.abilities.add(ab1);

        if (level.isClientSide())
            this.abilityButtons.add(ab1.getButton(Keybindings.keyQ));
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, WardenUnit.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, WardenUnit.attackDamage)
                .add(Attributes.ARMOR, WardenUnit.armorValue)
                .add(Attributes.MAX_HEALTH, WardenUnit.maxHealth)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5);
    }

    // override new AI types and use unit goals instead
    @Override
    protected void customServerAiStep() { }
    @Override
    public void increaseAngerAt(@Nullable Entity pEntity, int pOffset, boolean pPlayListeningSound) {}
    @Override
    public LivingEntity getTarget() { return this.targetGoal.getTarget(); }
    @Override
    public void setAttackTarget(LivingEntity pAttackTarget) { }

    public void tick() {
        this.setCanPickUpLoot(false);

        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);
        this.sonicBoomGoal.tick();

        // apply slowness level 2 during daytime for a short time repeatedly
        if (!this.level.isClientSide() && this.level.isDay() && !NightUtils.isInRangeOfNightSource(this.getEyePosition(), false))
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 1));
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new MeleeAttackUnitGoal(this, getAttackCooldown(), false);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
        this.sonicBoomGoal = new SonicBoomGoal(this, SONIC_BOOM_CHANNEL_TICKS, SONIC_BOOM_RANGE, this::doEntitySonicBoom, this::doBuildingSonicBoom);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        // movegoal must be lower priority than attacks so that attack-moving works correctly
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, sonicBoomGoal);
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    public void resetBehaviours() {
        this.sonicBoomAnimationState.stop();
        this.sonicBoomGoal.stop();
    }

    public void doEntitySonicBoom(LivingEntity targetEntity) {
        doEntitySonicBoom(targetEntity, this.position().add(0, 1.6, 0));
    }

    public void doEntitySonicBoom(LivingEntity targetEntity, Vec3 startPos) {
        Vec3 targetPos = targetEntity.getEyePosition().subtract(startPos);
        Vec3 normTargetPos = targetPos.normalize();

        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        if (!this.level.isClientSide()) {
            ServerLevel level = (ServerLevel) this.level;
            for(int i = 1; i < Mth.floor(targetPos.length()) + 7; ++i) {
                Vec3 particlePos = startPos.add(normTargetPos.scale(i));
                level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0,0,0,0);
            }
        }
        targetEntity.hurt(DamageSource.sonicBoom(this), SONIC_BOOM_DAMAGE);
        double knockbackY = 0.5 * (1.0 - targetEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        double knockbackXZ = 2.0 * (1.0 - targetEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        targetEntity.push(normTargetPos.x() * knockbackXZ, normTargetPos.y() * knockbackY, normTargetPos.z() * knockbackXZ);
    }

    public void doBuildingSonicBoom(Building targetBuilding) {
        Vec3 startPos = this.position().add(0, 1.6, 0);
        Vec3 targetPos = new Vec3(
                targetBuilding.centrePos.getX() + 0.5f,
                targetBuilding.minCorner.getY() + 1.5f,
                targetBuilding.centrePos.getZ() + 0.5f)
                .subtract(startPos);
        Vec3 normTargetPos = targetPos.normalize();

        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        if (!this.level.isClientSide()) {
            ServerLevel level = (ServerLevel) this.level;
            for(int i = 1; i < Mth.floor(targetPos.length()) + 7; ++i) {
                Vec3 particlePos = startPos.add(normTargetPos.scale(i));
                level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0,0,0,0);
            }
        }
        boolean hasResearch;
        if (this.level.isClientSide())
            hasResearch = ResearchClient.hasResearch(ResearchSculkAmplifiers.itemName);
        else
            hasResearch = ResearchServerEvents.playerHasResearch(getOwnerName(), ResearchSculkAmplifiers.itemName);

        if (hasResearch && targetBuilding instanceof SculkCatalyst) {
            List<Mob> nearbyEnemies = MiscUtil.getEntitiesWithinRange(
                            new Vector3d(targetBuilding.centrePos.getX(), targetBuilding.centrePos.getY(), targetBuilding.centrePos.getZ()),
                            ResearchSculkAmplifiers.SPLIT_BOOM_RANGE, Mob.class, this.level)
                    .stream().filter(mob -> mob instanceof Unit unit &&
                            UnitServerEvents.getUnitToEntityRelationship(this, mob) == Relationship.HOSTILE)
                    .toList();
            if (nearbyEnemies.size() > 0)
                doEntitySonicBoom(nearbyEnemies.get(0), Vec3.atCenterOf(targetBuilding.centrePos));
            if (nearbyEnemies.size() > 1)
                doEntitySonicBoom(nearbyEnemies.get(1), Vec3.atCenterOf(targetBuilding.centrePos));
            if (nearbyEnemies.size() > 2)
                doEntitySonicBoom(nearbyEnemies.get(2), Vec3.atCenterOf(targetBuilding.centrePos));
        }
        else
            targetBuilding.destroyRandomBlocks((int) SONIC_BOOM_DAMAGE / 2);
    }

    public void startSonicBoomAnimation() {
        this.sonicBoomAnimationState.start(this.tickCount);
    }

    public void stopSonicBoomAnimation() {
        this.sonicBoomAnimationState.stop();
    }
}
