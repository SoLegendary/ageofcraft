package com.solegendary.reignofnether.unit.units.monsters;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.ConsumeSlime;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchSlimeConversion;
import com.solegendary.reignofnether.time.NightUtils;
import com.solegendary.reignofnether.unit.Checkpoint;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.controls.SlimeUnitMoveControl;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.MagmaCubeUnit;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SlimeUnit extends Slime implements Unit, AttackerUnit {
    // region
    private final ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    public ArrayList<Checkpoint> getCheckpoints() { return checkpoints; };

    GarrisonGoal garrisonGoal;
    public GarrisonGoal getGarrisonGoal() { return garrisonGoal; }
    public boolean canGarrison() { return getGarrisonGoal() != null; }

    UsePortalGoal usePortalGoal;
    public UsePortalGoal getUsePortalGoal() { return usePortalGoal; }
    public boolean canUsePortal() { return getUsePortalGoal() != null; }

    public Faction getFaction() {return Faction.MONSTERS;}
    public List<AbilityButton> getAbilityButtons() {return abilityButtons;};
    public List<Ability> getAbilities() {return abilities;}
    public List<ItemStack> getItems() {return items;};
    public MoveToTargetBlockGoal getMoveGoal() {return moveGoal;}
    public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() {return targetGoal;}
    public Goal getAttackBuildingGoal() {return attackBuildingGoal;}
    public Goal getAttackGoal() {return attackGoal;}
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    protected MoveToTargetBlockGoal moveGoal;
    protected SelectedTargetGoal<? extends LivingEntity> targetGoal;
    protected ReturnResourcesGoal returnResourcesGoal;

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
            SynchedEntityData.defineId(SlimeUnit.class, EntityDataSerializers.STRING);

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
    public float getAttackRange() { return ((getSize() + 1) * 0.5f); }
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitArmorValue() {return armorValue;}
    public int getPopCost() {
        if (getSize() == 1)
            return 0;
        return getSize();
    }
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // endregion

    final public int STARTING_SIZE = 2;
    final public int MAX_SIZE = 6;

    final static public float attackDamagePerSize = 2.0f;
    final static public float attacksPerSecond = 0.5f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.6f; // needs to be 2x other units
    final static public float aggroRange = 10;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;

    protected boolean forceTiny = false; // prevent split on death temporarily
    public boolean shouldSpawnSlimes = true; // prevent split on death without changing size

    public int maxResources = 0;

    private MeleeAttackSlimeUnitGoal attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public SlimeUnit consumeTarget = null;

    public SlimeUnit(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SlimeUnitMoveControl(this);

        ConsumeSlime ab1 = new ConsumeSlime(this);
        this.abilities.add(ab1);
        if (level.isClientSide())
            this.abilityButtons.add(ab1.getButton(Keybindings.keyQ));
    }

    // big slimes sometimes bounce off of each other midair
    public final int PUSH_ATTACK_CD_MAX = getAttackCooldown();
    public int pushAttackCd = 0;

    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.onClimbable() && isOnGround();
    }

    @Override
    public void push(Entity pEntity) {
        super.push(pEntity);
        if (this.getTargetGoal().getTarget() == pEntity && !onGround &&
            !level.isClientSide() && pushAttackCd <= 0) {
            this.doHurtTarget(pEntity);
            pushAttackCd = PUSH_ATTACK_CD_MAX;
        }
    }

    @Override
    public int getSize() {
        if (forceTiny)
            return 1;
        else
            return super.getSize();
    }

    @Override
    public void kill() {
        shouldSpawnSlimes = false;
        super.kill();
    }

    @Override
    public void remove(RemovalReason pReason) {
        // prevent vanilla split logic
        forceTiny = true;
        super.remove(pReason);
        forceTiny = false;
    }

    @Override
    protected float getDamageAfterMagicAbsorb(DamageSource pSource, float pDamage) {
        pDamage = super.getDamageAfterMagicAbsorb(pSource, pDamage);
        if (pSource.isMagic())
            pDamage *= 0.5F;
        return pDamage;
    }

    protected void spawnTinySlime() {
        float f = (float) getSize() / 4.0F;
        float f1 = -0.5F * f;
        Slime slime = this.getType().create(this.level);
        if (slime != null) {
            if (this.isPersistenceRequired())
                slime.setPersistenceRequired();
            slime.setCustomName(this.getCustomName());
            slime.setNoAi(this.isNoAi());
            slime.setInvulnerable(this.isInvulnerable());
            slime.setSize(1, true);
            slime.moveTo(this.getX() + (double)f1, this.getY() + 0.5, this.getZ() + (double)f1, this.random.nextFloat() * 360.0F, 0.0F);
            if (slime instanceof Unit unit)
                unit.setOwnerName(getOwnerName());
            this.level.addFreshEntity(slime);
        }
    }

    public boolean autocastingConsume() {
        for (Ability ability : abilities)
            if (ability instanceof ConsumeSlime consume)
                return consume.autocast;
        return false;
    }

    public float getUnitAttackDamage() {
        return attackDamagePerSize * getSize();
    }
    public float getUnitMaxHealth() { return getMaxHealthForSize(getSize()); }
    public float getKnockbackResistance() {
        return getSize() * (1.0f / 6);
    }

    @Override
    public void resetBehaviours() {
        consumeTarget = null;
        for (Ability ability : abilities)
            if (ability instanceof ConsumeSlime consume)
                consume.autocast = false;
    }

    @Override
    public void setSize(int pSize, boolean pResetHealth) {
        int i = Mth.clamp(pSize, 1, MAX_SIZE);
        this.entityData.set(ID_SIZE, i);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getUnitMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getMovementSpeed());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getUnitAttackDamage());
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(getKnockbackResistance());

        if (pResetHealth)
            this.setHealth(this.getMaxHealth());
    }

    protected int getSizeForHealth(float health) {
        if (health >= getMaxHealthForSize(5))
            return 6;
        else if (health > getMaxHealthForSize(4))
            return 5;
        else if (health > getMaxHealthForSize(3))
            return 4;
        else if (health > getMaxHealthForSize(2))
            return 3;
        else if (health > getMaxHealthForSize(1))
            return 2;
        else
            return 1;
    }

    protected int getMaxHealthForSize(int size) {
        if (size >= 6)
            return 180;
        else if (size == 5)
            return 145;
        else if (size == 4)
            return 110;
        else if (size == 3)
            return 75;
        else if (size == 2)
            return 40;
        else
            return 15;
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    protected boolean isDealsDamage() {
        return true;
    }

    @Override
    protected int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    @Override
    protected void decreaseSquish() {
        this.targetSquish *= 0.9F;
    }

    @Override
    public void jumpFromGround() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, (double)(this.getJumpPower() + (float)this.getSize() * 0.1F), vec3.z);
        this.hasImpulse = true;
        ForgeHooks.onLivingJump(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, SlimeUnit.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, SlimeUnit.attackDamagePerSize)
                .add(Attributes.ARMOR, SlimeUnit.armorValue)
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.FOLLOW_RANGE, Unit.getFollowRange());
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);

        if (pushAttackCd > 0)
            pushAttackCd -= 1;

        if (autocastingConsume() && getSize() < MAX_SIZE && getTargetGoal().getTarget() == null) {

            Vector3d unitPosition = new Vector3d(position().x, position().y, position().z);
            List<SlimeUnit> nearbyEntities = MiscUtil.getEntitiesWithinRange(unitPosition, aggroRange, SlimeUnit.class, level);

            double closestDist = aggroRange;
            SlimeUnit closestTarget = null;

            for (SlimeUnit slime : nearbyEntities) {
                if (slime.getOwnerName().equals(getOwnerName()) && slime != this && slime.getSize() <= getSize() && !slime.isTiny()) {
                    double dist = position().distanceTo(slime.position());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestTarget = slime;
                    }
                }
            }
            if (closestTarget != null) {
                consumeTarget = closestTarget;
                setUnitAttackTarget(closestTarget);
            }
        }
        // apply slowness level 2 during daytime for a short time repeatedly
        if (tickCount % 10 == 0 && !this.level.isClientSide() && this.level.isDay() &&
                !NightUtils.isInRangeOfNightSource(this.getEyePosition(), false) &&
                !ResearchServerEvents.playerHasCheat(getOwnerName(), "slipslopslap"))
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 1));
    }

    // break leaves that are touched
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && getSize() >= 2) {
            if (this.horizontalCollision || this.verticalCollision) {
                boolean flag = false;
                AABB aabb = this.getBoundingBox().inflate(0.2);
                Iterator var = BlockPos.betweenClosed(
                        Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ),
                        Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))
                .iterator();

                label:
                while (true) {
                    BlockPos blockpos;
                    Block block;
                    do {
                        if (!var.hasNext())
                            break label;
                        blockpos = (BlockPos) var.next();
                        BlockState blockstate = this.level.getBlockState(blockpos);
                        block = blockstate.getBlock();
                    } while (!(block instanceof LeavesBlock));

                    flag = this.level.destroyBlock(blockpos, false, this) || flag;
                }
            }
        }
    }

    // stop moving if we overshoot our move target
    private double lastDistToMoveTargetSqr = 9999;
    private BlockPos lastMoveTarget = null;

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        if (!level.isClientSide() && pOnGround && !wasOnGround) {
            attackGoal.landedJump();

            BlockPos moveTarget = getMoveGoal().getMoveTarget();
            if (moveTarget != null) {
                double distToMoveTargetSqr = distanceToSqr(Vec3.atCenterOf(moveTarget));
                if (distToMoveTargetSqr > lastDistToMoveTargetSqr && distToMoveTargetSqr < 9 &&
                    moveTarget.equals(lastMoveTarget)) {
                    getMoveGoal().stopMoving();
                }
                lastDistToMoveTargetSqr = distToMoveTargetSqr;
                lastMoveTarget = moveTarget;
            }
        }
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockSlimeGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.attackGoal = new MeleeAttackSlimeUnitGoal(this, false);
        this.attackBuildingGoal = new MeleeAttackBuildingGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, attackBuildingGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.targetSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setSize(STARTING_SIZE, true);
        return pSpawnData;
    }

    private final static int CONVERT_DEBUFF_DURATION_SECONDS = 10;

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        boolean result = super.doHurtTarget(pEntity);
        if (result && pEntity == consumeTarget) {
            this.setSize(Math.min(MAX_SIZE, getSize() + consumeTarget.getSize() / 2), false);
            if (consumeTarget.getSize() != 1)
                this.heal((consumeTarget.getUnitMaxHealth() / 2) + 15);
            else
                this.heal((consumeTarget.getUnitMaxHealth() / 2));
            pEntity.kill();
            consumeTarget = null;
            return true;
        }
        if (result && getSize() >= 2 && pEntity instanceof LivingEntity && !(this instanceof MagmaCubeUnit) && !this.level.isClientSide())
            if (ResearchServerEvents.playerHasResearch(getOwnerName(), ResearchSlimeConversion.itemName))
                ((LivingEntity)pEntity).addEffect(new MobEffectInstance(MobEffects.CONFUSION, CONVERT_DEBUFF_DURATION_SECONDS * 20, 0), this);
        return result;
    }

    @Override
    protected void dealDamage(LivingEntity pLivingEntity) {
        // use doHurtTarget instead
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean result = super.hurt(pSource, pAmount);

        int newSize = getSizeForHealth(getHealth());
        if (newSize < getSize() && shouldSpawnSlimes) {
            if (getSize() >= 2)
                spawnTinySlime();
            if (getSize() >= 4)
                spawnTinySlime();
            if (getSize() >= 6)
                spawnTinySlime();
        }
        if (newSize != getSize())
            setSize(newSize, false);

        return result;
    }
}
