package com.solegendary.reignofnether.unit.units.villagers;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.SetFangsCircle;
import com.solegendary.reignofnether.ability.abilities.SetFangsLine;
import com.solegendary.reignofnether.ability.abilities.CastSummonVexes;
import com.solegendary.reignofnether.ability.abilities.PromoteIllager;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.modelling.VillagerUnitModel;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EvokerUnit extends Evoker implements Unit, AttackerUnit, RangedAttackerUnit {
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
    public List<AbilityButton> getAbilityButtons() {return abilityButtons;}
    public List<Ability> getAbilities() {return abilities;}
    public List<ItemStack> getItems() {return items;}
    public MoveToTargetBlockGoal getMoveGoal() {return moveGoal;}
    public SelectedTargetGoal<? extends LivingEntity> getTargetGoal() {return targetGoal;}
    public Goal getAttackGoal() {return attackGoal;}
    public Goal getAttackBuildingGoal() {return attackBuildingGoal;}

    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    public BuildRepairGoal buildRepairGoal;
    public GatherResourcesGoal gatherResourcesGoal;
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
            SynchedEntityData.defineId(EvokerUnit.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
    }

    // combat stats
    public boolean getWillRetaliate() {return willRetaliate;}
    public int getAttackCooldown() {return (int) (20 / attacksPerSecond);}
    public float getAttacksPerSecond() {return 20f / (getAttackCooldown() + 25);}
    public float getAggroRange() {return aggroRange;}
    public boolean getAggressiveWhenIdle() {return aggressiveWhenIdle && !isVehicle();}
    public float getAttackRange() {
        return isUsingLineFangs ? EvokerUnit.FANGS_RANGE_LINE : EvokerUnit.FANGS_RANGE_CIRCLE;
    }
    public float getUnitAttackDamage() {return attackDamage;}
    public boolean canAttackBuildings() {return getAttackBuildingGoal() != null;}

    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    public int getPopCost() {return popCost;}

    public void setAttackMoveTarget(@Nullable BlockPos bp) { this.attackMoveTarget = bp; }
    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // endregion

    private CastFangsGoal castFangsGoal;
    public CastFangsGoal getCastFangsGoal() {
        return castFangsGoal;
    }
    private CastSummonVexesGoal castSummonVexesGoal;
    public CastSummonVexesGoal getCastSummonVexesGoal() {
        return castSummonVexesGoal;
    }

    public static final int FANGS_RANGE_LINE = 10;
    public static final int FANGS_RANGE_CIRCLE = 3;
    public static final float FANGS_DAMAGE = 6f; // can sometimes be doubled or tripled due to overlapping fang hitboxes
    public static final int FANGS_CHANNEL_SECONDS = 2;
    public static final int SUMMON_VEXES_AMOUNT = 3;
    public static final int VEX_TARGET_RANGE = 20;
    public static final int VEX_TARGET_RANGE_GARRISON = 30;

    final static public float attackDamage = FANGS_DAMAGE * 2;
    final static public float attacksPerSecond = 1f / (SetFangsLine.CD_MAX_SECONDS + FANGS_CHANNEL_SECONDS);
    final static public float aggroRange = FANGS_RANGE_LINE;
    final static public boolean willRetaliate = true; // will attack when hurt by an enemy
    final static public boolean aggressiveWhenIdle = true;

    final static public float maxHealth = 40.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.25f;
    final static public int popCost = ResourceCosts.EVOKER.population;

    public boolean isUsingLineFangs = true; // toggle between line and circular fangs

    public int maxResources = 100;

    public int fogRevealDuration = 0; // set > 0 for the client who is attacked by this unit
    public int getFogRevealDuration() { return fogRevealDuration; }
    public void setFogRevealDuration(int duration) { fogRevealDuration = duration; }

    private UnitBowAttackGoal<? extends LivingEntity> attackGoal;
    private MeleeAttackBuildingGoal attackBuildingGoal;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public EvokerUnit(EntityType<? extends Evoker> entityType, Level level) {
        super(entityType, level);

        SetFangsLine ab1 = new SetFangsLine(this);
        SetFangsCircle ab2 = new SetFangsCircle(this);
        CastSummonVexes ab3 = new CastSummonVexes(this);
        this.abilities.add(ab1);
        this.abilities.add(ab2);
        this.abilities.add(ab3);

        if (level.isClientSide()) {
            this.abilityButtons.add(ab1.getButton(Keybindings.keyQ));
            this.abilityButtons.add(ab2.getButton(Keybindings.keyW));
            this.abilityButtons.add(ab3.getButton(Keybindings.keyE));
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, EvokerUnit.movementSpeed)
                .add(Attributes.MAX_HEALTH, EvokerUnit.maxHealth)
                .add(Attributes.ARMOR, EvokerUnit.armorValue);
    }

    @Override
    public void resetBehaviours() {
        this.castFangsGoal.stop();
        this.castSummonVexesGoal.stop();
        if (attackGoal != null && this.abilities.size() > 0 && this.abilities.get(0).isOffCooldown())
            this.attackGoal.resetCooldown();
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        AttackerUnit.tick(this);
        this.castFangsGoal.tick();
        this.castSummonVexesGoal.tick();
        PromoteIllager.checkAndApplyBuff(this);

        // need to do this outside the goal so it ticks down while not attacking
        // only needed for attack goals created by reignofnether like RangedBowAttackUnitGoal
        if (attackGoal != null)
            attackGoal.tickCooldown();
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.attackGoal = new UnitBowAttackGoal<>(this, getAttackCooldown());
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
        this.castFangsGoal = new CastFangsGoal(this, FANGS_CHANNEL_SECONDS * ResourceCost.TICKS_PER_SECOND, FANGS_RANGE_LINE, this::createEvokerFangs);
        this.castSummonVexesGoal = new CastSummonVexesGoal(this);
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    // controls whether the evoker's arms are up or not
    @Override
    public boolean isCastingSpell() {
        if (this.getCastFangsGoal() != null && this.getCastFangsGoal().isCasting())
            return true;
        if (this.getCastSummonVexesGoal() != null && this.getCastSummonVexesGoal().isCasting())
            return true;
        return false;
    }

    // starts channelling the fangs attack
    @Override
    public void performUnitRangedAttack(LivingEntity pTarget, float velocity) {
        if (isUsingLineFangs) {
            this.getCastFangsGoal().startCasting();
            this.getCastFangsGoal().setAbility(this.abilities.get(0));
            this.getCastFangsGoal().setTarget(pTarget);
        } else {
            this.getCastFangsGoal().startCasting();
            this.getCastFangsGoal().setAbility(this.abilities.get(1));
            this.getCastFangsGoal().setTarget(pTarget);
        }
        if (!level.isClientSide() && pTarget instanceof Unit unit)
            FogOfWarClientboundPacket.revealRangedUnit(unit.getOwnerName(), this.getId());
    }

    // actually performs the fangs attack
    public void createEvokerFangs(LivingEntity targetEntity) {
        if (isUsingLineFangs) {
            createEvokerFangsLine(targetEntity);
        } else {
            createEvokerFangsCircle();
        }
    }

    // based on Evoker.EvokerAttackSpellGoal.performSpellCasting
    public void createEvokerFangsLine(LivingEntity targetEntity) {
        double d0 = Math.min(targetEntity.getOnPos().getY(), this.getY());
        double d1 = Math.max(targetEntity.getOnPos().getY(), this.getY()) + 1.0;
        float f = (float)Mth.atan2(targetEntity.getZ() - this.getZ(), targetEntity.getX() - this.getX());
        int k;
        for(k = 0; k < FANGS_RANGE_LINE; ++k) {
            double d2 = 1.25 * (double)(k + 1);
            createEvokerFang(this.getX() + (double)Mth.cos(f) * d2, this.getZ() + (double)Mth.sin(f) * d2, d0, d1, f, k);
        }
    }

    // based on Evoker.EvokerAttackSpellGoal.performSpellCasting
    public void createEvokerFangsCircle() {
        int k;
        float f2;
        for(k = 0; k < 5; ++k) {
            f2 = (float)k * (float) Math.PI * 0.4F;
            createEvokerFang(this.getX() + (double)Mth.cos(f2) * 1.5, this.getZ() + (double)Mth.sin(f2) * 1.5, this.getY(), this.getY() + 1, f2, 0);
        }
        for(k = 0; k < 8; ++k) {
            f2 = (float)k * (float) Math.PI * 2.0F / 8.0F + 1.2566371F;
            createEvokerFang(this.getX() + (double)Mth.cos(f2) * 2.5, this.getZ() + (double)Mth.sin(f2) * 2.5, this.getY(), this.getY() + 1, f2, 3);
        }
    }

    // based on Evoker.EvokerAttackSpellGoal.createSpellEntity
    private void createEvokerFang(double pX, double pZ, double pMinY, double pMaxY, float pYRot, int pWarmupDelay) {
        BlockPos blockpos = new BlockPos(pX, pMaxY, pZ);
        boolean flag = false;
        double d0 = 0.0;

        do {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = this.level.getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(this.level, blockpos1, Direction.UP)) {
                if (!this.level.isEmptyBlock(blockpos)) {
                    BlockState blockstate1 = this.level.getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(this.level, blockpos);
                    if (!voxelshape.isEmpty())
                        d0 = voxelshape.max(Direction.Axis.Y);
                }
                flag = true;
                break;
            }
            blockpos = blockpos.below();
        } while(blockpos.getY() >= Mth.floor(pMinY) - 1);

        if (flag)
            this.level.addFreshEntity(new EvokerFangs(this.level, pX, (double)blockpos.getY() + d0, pZ, pYRot, pWarmupDelay, this));
    }

    public int getVexTargetRange() {
        if (GarrisonableBuilding.getGarrison(this) != null)
            return VEX_TARGET_RANGE_GARRISON;
        return VEX_TARGET_RANGE;
    }

    public void summonVexes() {
        if (this.level.isClientSide())
            return;

        for(int i = 0; i < SUMMON_VEXES_AMOUNT; ++i) {
            BlockPos blockpos = this.blockPosition().offset(-2 + this.random.nextInt(5), 1, -2 + this.random.nextInt(5));
            Vex vex = EntityType.VEX.create(this.level);
            if (vex != null) {
                vex.moveTo(blockpos, 0.0F, 0.0F);
                vex.finalizeSpawn((ServerLevel) this.level, this.level.getCurrentDifficultyAt(blockpos), MobSpawnType.MOB_SUMMONED, null, null);
                vex.setOwner(this);
                vex.setBoundOrigin(blockpos);
                vex.setLimitedLife(CastSummonVexes.VEX_DURATION_SECONDS * ResourceCost.TICKS_PER_SECOND);
                ((ServerLevel) this.level).addFreshEntityWithPassengers(vex);
            }
        }
    }

    // TODO: when a target is autoacquired serverside this is not updated clientside
    public VillagerUnitModel.ArmPose getEvokerArmPose() {
        Entity targetEntity = getTargetGoal().getTarget();
        if (this.isCastingSpell() || (targetEntity != null &&
            this.distanceTo(targetEntity) <= getAttackRange()) &&
            this.getAbilities().get(0).isOffCooldown()) {
            return VillagerUnitModel.ArmPose.SPELLCASTING;
        }
        return VillagerUnitModel.ArmPose.CROSSED;
    }

    @Override
    public void setupEquipmentAndUpgradesClient() {

    }

    @Override
    public void setupEquipmentAndUpgradesServer() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }
}
