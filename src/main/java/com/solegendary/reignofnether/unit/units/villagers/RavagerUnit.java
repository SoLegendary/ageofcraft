package com.solegendary.reignofnether.unit.units.villagers;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.Eject;
import com.solegendary.reignofnether.ability.abilities.Roar;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
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
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class RavagerUnit extends Ravager implements Unit, AttackerUnit {
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
            SynchedEntityData.defineId(RavagerUnit.class, EntityDataSerializers.STRING);

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

    final static public float attackDamage = 7.0f;
    final static public float attacksPerSecond = 0.6f;
    final static public float maxHealth = 250.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.25f;
    final static public float attackRange = 2; // only used by ranged units or melee building attackers
    final static public float aggroRange = 10;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;
    final static public int popCost = ResourceCosts.RAVAGER.population;

    public int maxResources = 100;

    private MeleeAttackUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public final static float ROAR_DAMAGE = 8.0f;
    public final static float ROAR_RANGE = 4.0f;
    public final static float ROAR_KNOCKBACK = 6f;
    public final static int ROAR_SLOW_DURATION = 10 * ResourceCost.TICKS_PER_SECOND;

    private static final Predicate<Entity> NO_RAVAGER_AND_ALIVE = e -> e.isAlive() && !(e instanceof Ravager);

    public RavagerUnit(EntityType<? extends Ravager> entityType, Level level) {
        super(entityType, level);

        Roar ab1 = new Roar(this);
        Eject ab2 = new Eject(this);
        this.abilities.add(ab1);
        this.abilities.add(ab2);
        if (level.isClientSide()) {
            this.abilityButtons.add(ab1.getButton(Keybindings.keyQ));
            this.abilityButtons.add(ab2.getButton(Keybindings.keyW));
        }
    }

    // prevents attack commands applying only to passengers
    @Override
    protected void updateControlFlags() {}

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, RavagerUnit.maxHealth)
                .add(Attributes.MOVEMENT_SPEED, RavagerUnit.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, RavagerUnit.attackDamage)
                .add(Attributes.ARMOR, RavagerUnit.armorValue)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    // prevent shield blocks from stunning and triggering a roar
    @Override
    protected void blockedByShield(LivingEntity pEntity) { }

    public void tick() {
        this.setCanPickUpLoot(false);

        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.attackGoal = new MeleeAttackUnitGoal(this, getAttackCooldown(), false);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        // movegoal must be lower priority than attacks so that attack-moving works correctly
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    private void strongKnockback(Entity pEntity) {
        double d0 = pEntity.getX() - this.getX();
        double d1 = pEntity.getZ() - this.getZ();
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001);
        pEntity.push(d0 / d2 * ROAR_KNOCKBACK, 0.2, d1 / d2 * ROAR_KNOCKBACK);
    }

    public void startToRoar() {
        this.playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 1.0F);
        this.roarTick = 40;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(movementSpeed);
        }
    }

    @Override
    public void roar() {
        if (this.isAlive()) {
            if (!level.isClientSide()) {
                LivingEntity livingentity;

                List<Mob> nearbyMobs = MiscUtil.getEntitiesWithinRange(
                        new Vector3d(this.position().x, this.position().y, this.position().z),
                        ROAR_RANGE,
                        Mob.class,
                        this.level);

                for (Mob mob : nearbyMobs) {
                    if (mob instanceof Unit unit && UnitServerEvents.getUnitToEntityRelationship(this, mob) != Relationship.FRIENDLY) {
                        this.strongKnockback(mob);
                        mob.hurt(DamageSource.mobAttack(this), ROAR_DAMAGE);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ROAR_SLOW_DURATION, 1));
                    }
                }

                Set<Building> affectedBuildings = new HashSet<>();
                for (double x = this.position().x - ROAR_RANGE; x < this.position().x + ROAR_RANGE; x++) {
                    for (double y = this.position().y - ROAR_RANGE; y < this.position().y + ROAR_RANGE; y++) {
                        for (double z = this.position().z - ROAR_RANGE; z < this.position().z + ROAR_RANGE; z++) {
                            BlockPos bp = new BlockPos(x,y,z);
                            Building building = BuildingUtils.findBuilding(false, bp);
                            if (building != null && !building.ownerName.equals(this.getOwnerName()))
                                affectedBuildings.add(building);
                        }
                    }
                }
                for (Building building : affectedBuildings)
                    building.destroyRandomBlocks((int) ROAR_DAMAGE);

                Vec3 vec3 = this.getBoundingBox().getCenter();

                this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
                this.gameEvent(GameEvent.ENTITY_ROAR);
                this.level.explode(null, null, null,
                        vec3.x, vec3.y, vec3.z,
                        2.0f,
                        false,
                        Explosion.BlockInteraction.NONE);
            }
            else {
                Vec3 vec3 = this.getBoundingBox().getCenter();

                for(int i = 0; i < 80; ++i) {
                    double d0 = this.random.nextGaussian() * 0.2;
                    double d1 = this.random.nextGaussian() * 0.2;
                    double d2 = this.random.nextGaussian() * 0.2;
                    this.level.addParticle(ParticleTypes.POOF, vec3.x, vec3.y, vec3.z, d0, d1, d2);
                }
                this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
            }
        }
    }
}
