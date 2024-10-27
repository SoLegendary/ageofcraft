package com.solegendary.reignofnether.mixin;

import com.google.common.collect.Lists;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {

    protected AbstractArrowMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract boolean isNoPhysics();
    @Shadow private int life;

    private boolean isInsideTopOfBuilding(Building building) {
        BlockPos bp = this.blockPosition();
        boolean isPosInsideBuildingExt =
                bp.getX() <= building.maxCorner.getX() + 1 && bp.getX() >= building.minCorner.getX() - 1 &&
                bp.getY() <= building.maxCorner.getY() + 1 && bp.getY() >= building.minCorner.getY() - 1 &&
                bp.getZ() <= building.maxCorner.getZ() + 1 && bp.getZ() >= building.minCorner.getZ() - 1;

        // only have nophysics at a high Y value so we can still attack enemies at the base of the building
        return building.isPosInsideBuilding(this.blockPosition()) &&
                this.blockPosition().getY() > building.originPos.getY() + 5;
    }

    // prevent arrows from colliding with the building that a garrisoned unit is inside of
    @Inject(
            method = "isNoPhysics",
            at = @At("HEAD"),
            cancellable = true
    )
    public void isNoPhysics(CallbackInfoReturnable<Boolean> cir) {
        if (this.getOwner() instanceof AttackerUnit aUnit) {
            // garrisoned unit -> ground
            if (GarrisonableBuilding.getGarrison((Unit) aUnit) instanceof Building building &&
                isInsideTopOfBuilding(building)) {
                cir.setReturnValue(true);
            }

            // ground -> garrisoned unit - allow through at 1/2 accuracy
            // can't use random() here or the nophysics status will change every tick
            /*
            if (this.getId() % 2 == 0 &&
                ((Unit) aUnit).getTargetGoal().getTarget() instanceof Unit tUnit) {
                if (GarrisonableBuilding.getGarrison(tUnit) instanceof Building building &&
                    isInsideTopOfBuilding(building)) {

                    // nophysics all the time means they wouldn't collide with the actual target
                    if (this.distanceToSqr((Entity) tUnit) > Math.pow(1.0d, 2))
                         cir.setReturnValue(true);
                }
            }
             */
        }
    }

    // correct angle of nophysics arrows
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    public void tick(CallbackInfo ci) {
        if (this.isNoPhysics()) {
            Vec3 vec3 = this.getDeltaMovement();
            double d4 = vec3.horizontalDistance();
            double d6 = vec3.y;
            this.setXRot((float)(Mth.atan2(d6, d4) * 57.2957763671875));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
        }
    }

    // reduce effective life
    @Inject(
            method = "tickDespawn",
            at = @At("TAIL")
    )
    protected void tickDespawn(CallbackInfo ci) {
        if (this.getOwner() instanceof Unit && this.life >= 200)
            this.discard();
    }

    @Shadow public byte getPierceLevel() { return new Byte(""); }
    @Shadow private SoundEvent soundEvent;
    @Shadow private IntOpenHashSet piercingIgnoreEntityIds;
    @Shadow private List<Entity> piercedAndKilledEntities;
    @Shadow private double baseDamage;
    @Shadow public boolean isCritArrow() { return false; }
    @Shadow public AbstractArrow.Pickup pickup;
    @Shadow private int knockback;
    @Shadow protected abstract ItemStack getPickupItem();
    @Shadow protected void doPostHurtEffects(LivingEntity pTarget) { }
    @Shadow public boolean shotFromCrossbow() { return false; }

    // replace bounce logic (on hitting an enemy at the time as another arrow) with pierce logic instead
    @Inject(
            method = "onHitEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void onHitEntity(EntityHitResult pResult, CallbackInfo ci) {
        ci.cancel();

        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();
        float f = (float)this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double)f * this.baseDamage, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }
            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long)this.random.nextInt(i / 2 + 2);
            i = (int)Math.min(j + (long)i, 2147483647L);
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = DamageSource.arrow(new Arrow(this.level, this.xo, this.yo, this.zo), this);//DamageSource.arrow(this, this);
        } else {
            damagesource = DamageSource.arrow(new Arrow(this.level, this.xo, this.yo, this.zo), entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity)entity1).setLastHurtMob(entity);
            }
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.setSecondsOnFire(5);
        }

        if (entity.hurt(damagesource, (float)i)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    double d0 = Math.max(0.0, 1.0 - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double)this.knockback * 0.6 * d0);
                    if (vec3.lengthSqr() > 0.0) {
                        livingentity.push(vec3.x, 0.1, vec3.z);
                    }
                }

                if (!this.level.isClientSide && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level.isClientSide && entity1 instanceof ServerPlayer) {
                    ServerPlayer serverplayer = (ServerPlayer)entity1;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }
            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }
            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.getOwner() instanceof PillagerUnit pUnit &&
            !pUnit.getLevel().isClientSide() && pUnit.isPassenger()) {
            pUnit.getLevel().explode(this, damagesource, null,
                    pResult.getEntity().getEyePosition().x,
                    pResult.getEntity().getEyePosition().y,
                    pResult.getEntity().getEyePosition().z,
                    1f,
                    false,
                    Explosion.BlockInteraction.NONE);
        }
    }

    // replace bounce logic (on hitting an enemy at the time as another arrow) with pierce logic instead
    @Inject(
            method = "onHitBlock",
            at = @At("HEAD")
    )
    protected void onHitBlock(BlockHitResult pResult, CallbackInfo ci) {
        if (this.getOwner() instanceof PillagerUnit pUnit &&
                !pUnit.getLevel().isClientSide() && pUnit.isPassenger()) {
            pUnit.getLevel().explode(this, null, null,
                    pResult.getLocation().x,
                    pResult.getLocation().y,
                    pResult.getLocation().z,
                    1f,
                    false,
                    Explosion.BlockInteraction.NONE);
        }
    }
}

