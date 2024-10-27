package com.solegendary.reignofnether.mixin.firedamage;

import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {

    private static final int DAMAGE_DELAY = 20; // higher == damage less often
    private static final int DAMAGE = 3;

    @Inject(
            method = "entityInside",
            at = @At("HEAD"),
            cancellable = true
    )
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, CallbackInfo ci) {
        ci.cancel();
        if (!pEntity.fireImmune()) {
            if (pEntity.getRemainingFireTicks() < (7 * 20) - 1)
                pEntity.setRemainingFireTicks((8 * 20) - 1); // prevent damage from being ON fire from happening every tick
            boolean isDamageTick = pEntity.tickCount % DAMAGE_DELAY == 0;
            if (isDamageTick) {
                pEntity.hurt(DamageSource.IN_FIRE, DAMAGE);
            }
        }
    }
}
