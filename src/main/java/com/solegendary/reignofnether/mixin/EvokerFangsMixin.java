package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(EvokerFangs.class)
public abstract class EvokerFangsMixin extends Entity {

    public EvokerFangsMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    @Shadow private boolean clientSideAttackStarted;
    @Shadow public LivingEntity getOwner() { return null; }
    @Shadow private int lifeTicks;
    @Shadow private int warmupDelayTicks;
    @Shadow private boolean sentSpikeEvent;
    @Shadow private void dealDamageTo(LivingEntity pTarget) {}

    @Inject(
        method = "dealDamageTo",
        at = @At("HEAD"),
        cancellable=true
    )
    private void dealDamageTo(LivingEntity pTarget, CallbackInfo ci) {
        ci.cancel();
        LivingEntity owner = this.getOwner();

        // prevent friendly fire
        if (this.getOwner() instanceof Unit unit &&
            UnitServerEvents.getUnitToEntityRelationship(unit, pTarget) == Relationship.FRIENDLY)
            return;

        // set to the damage amount we want and set us as the owner to ensure knockback
        if (pTarget.isAlive() && !pTarget.isInvulnerable() && pTarget != this.getOwner())
            pTarget.hurt(DamageSource.indirectMagic(this.getOwner(), this.getOwner()), EvokerUnit.FANGS_DAMAGE);
    }

    // increase AOE size (originally inflate(0.2, 0.0, 0.2))
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable=true
    )
    private void tick(CallbackInfo ci) {
        ci.cancel();
        super.tick();
        if (this.level.isClientSide) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == 14) {
                    for(int $$0 = 0; $$0 < 12; ++$$0) {
                        double $$1 = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double $$2 = this.getY() + 0.05 + this.random.nextDouble();
                        double $$3 = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double $$4 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double $$5 = 0.3 + this.random.nextDouble() * 0.3;
                        double $$6 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.level.addParticle(ParticleTypes.CRIT, $$1, $$2 + 1.0, $$3, $$4, $$5, $$6);
                    }
                }
            }
        } else if (--this.warmupDelayTicks < 0) {
            if (this.warmupDelayTicks == -8) {
                List<LivingEntity> $$7 = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5));
                Iterator var15 = $$7.iterator();

                while(var15.hasNext()) {
                    LivingEntity $$8 = (LivingEntity)var15.next();
                    this.dealDamageTo($$8);
                }
            }

            if (!this.sentSpikeEvent) {
                this.level.broadcastEntityEvent(this, (byte)4);
                this.sentSpikeEvent = true;
            }

            if (--this.lifeTicks < 0) {
                this.discard();
            }
        }

    }
}
