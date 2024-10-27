package com.solegendary.reignofnether.unit.units.villagers;

import com.solegendary.reignofnether.ability.abilities.*;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.*;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WitchUnit extends Witch implements Unit {
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
    public ReturnResourcesGoal getReturnResourcesGoal() {return returnResourcesGoal;}
    public int getMaxResources() {return maxResources;}

    private MoveToTargetBlockGoal moveGoal;
    private SelectedTargetGoal<? extends LivingEntity> targetGoal;
    public BuildRepairGoal buildRepairGoal;
    public GatherResourcesGoal gatherResourcesGoal;
    private ReturnResourcesGoal returnResourcesGoal;

    public LivingEntity getFollowTarget() { return followTarget; }
    public boolean getHoldPosition() { return holdPosition; }
    public void setHoldPosition(boolean holdPosition) { this.holdPosition = holdPosition; }

    // if true causes moveGoal and attackGoal to work together to allow attack moving
    // moves to a block but will chase/attack nearby monsters in range up to a certain distance away
    private LivingEntity followTarget = null; // if nonnull, continuously moves to the target
    private boolean holdPosition = false;

    // which player owns this unit? this format ensures its synched to client without having to use packets
    public String getOwnerName() { return this.entityData.get(ownerDataAccessor); }
    public void setOwnerName(String name) { this.entityData.set(ownerDataAccessor, name); }
    public static final EntityDataAccessor<String> ownerDataAccessor =
            SynchedEntityData.defineId(WitchUnit.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ownerDataAccessor, "");
    }

    // combat stats
    public float getMovementSpeed() {return movementSpeed;}
    public float getUnitMaxHealth() {return maxHealth;}
    public float getUnitArmorValue() {return armorValue;}
    public int getPopCost() {return popCost;}

    public void setFollowTarget(@Nullable LivingEntity target) { this.followTarget = target; }

    // endregion

    private ThrowPotionGoal throwPotionGoal;
    public ThrowPotionGoal getThrowPotionGoal() {
        return throwPotionGoal;
    }

    final static public float maxHealth = 40.0f;
    final static public float armorValue = 0.0f;
    final static public float movementSpeed = 0.25f;
    final static public int popCost = ResourceCosts.WITCH.population;
    public int maxResources = 100;

    final static public int LINGERING_POTION_DURATION = 5 * ResourceCost.TICKS_PER_SECOND;
    final static public int LINGERING_POTION_DURATION_EXTENDED = 10 * ResourceCost.TICKS_PER_SECOND;

    private final List<AbilityButton> abilityButtons = new ArrayList<>();
    private final List<Ability> abilities = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public WitchUnit(EntityType<? extends Witch> entityType, Level level) {
        super(entityType, level);

        ThrowLingeringHarmingPotion ab1 = new ThrowLingeringHarmingPotion(this);
        ThrowLingeringRegenPotion ab2 = new ThrowLingeringRegenPotion(this);
        this.abilities.add(ab1);
        this.abilities.add(ab2);

        if (level.isClientSide()) {
            this.abilityButtons.add(ab1.getButton(Keybindings.keyQ));
            this.abilityButtons.add(ab2.getButton(Keybindings.keyW));
        }
    }

    public int getPotionThrowRange() {
        if (GarrisonableBuilding.getGarrison(this) != null)
            return 16;
        return 8;
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    public void throwPotion(Vec3 targetBp, Potion potion) {
        ThrownPotion thrownPotion = new ThrownPotion(this.level, this);

        thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));

        Vec3 dMove = targetBp.subtract(this.getEyePosition())
                .multiply(1,0,1)
                .scale(0.05)
                .add(0,0.5,0);
        thrownPotion.setDeltaMovement(dMove);

        if (!this.isSilent())
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        this.level.addFreshEntity(thrownPotion);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, WitchUnit.movementSpeed)
                .add(Attributes.MAX_HEALTH, WitchUnit.maxHealth)
                .add(Attributes.ARMOR, WitchUnit.armorValue);
    }

    public void tick() {
        this.setCanPickUpLoot(true);
        super.tick();
        Unit.tick(this);
        this.throwPotionGoal.tick();
        PromoteIllager.checkAndApplyBuff(this);
    }

    public void initialiseGoals() {
        this.usePortalGoal = new UsePortalGoal(this);
        this.moveGoal = new MoveToTargetBlockGoal(this, false, 0);
        this.targetGoal = new SelectedTargetGoal<>(this, true, true);
        this.garrisonGoal = new GarrisonGoal(this);
        this.returnResourcesGoal = new ReturnResourcesGoal(this);
        this.throwPotionGoal = new ThrowPotionGoal(this);
    }

    @Override
    public void resetBehaviours() {
        this.throwPotionGoal.stop();
    }

    @Override
    protected void registerGoals() {
        initialiseGoals();
        this.goalSelector.addGoal(2, usePortalGoal);

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, returnResourcesGoal);
        this.targetSelector.addGoal(2, targetGoal);
        this.goalSelector.addGoal(2, throwPotionGoal);
        this.goalSelector.addGoal(2, garrisonGoal);
        this.goalSelector.addGoal(3, moveGoal);
        this.goalSelector.addGoal(4, new RandomLookAroundUnitGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // prevent drinking potions as defined by vanilla code
        if (this.getItemBySlot(EquipmentSlot.MAINHAND) != ItemStack.EMPTY) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setUsingItem(false);
            AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.removeModifier(Witch.SPEED_MODIFIER_DRINKING);
                this.getEntityData().set(Witch.DATA_USING_ITEM, false);
            }
        }
    }
}
