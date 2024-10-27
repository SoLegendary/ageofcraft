package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.ability.abilities.FirewallShot;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.units.piglins.BlazeUnit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Blaze.class)
public class BlazeMixin extends Monster {

    protected BlazeMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(
            method = "aiStep",
            at = @At("HEAD"),
            cancellable = true
    )
    public void aiStep(CallbackInfo ci) {
        ci.cancel();

        if (!this.onGround && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        if (this.level.isClientSide) {
            if (this.random.nextInt(24) == 0 && !this.isSilent())
                this.level.playLocalSound(this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5, SoundEvents.BLAZE_BURN, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            if (!isBlazeUnitWithFireWallOnCooldown())
                for (int $$0 = 0; $$0 < 2; ++$$0)
                    this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
        }
        super.aiStep();
    }

    private boolean isBlazeUnitWithFireWallOnCooldown() {
        for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
            if (entity.getId() == this.getId() &&
                entity instanceof BlazeUnit blazeUnit &&
                !blazeUnit.getAbilities().isEmpty() &&
                blazeUnit.getAbilities().get(0) instanceof FirewallShot firewallShot &&
                !firewallShot.isOffCooldown())
                return true;
        }
        return false;
    }
}
