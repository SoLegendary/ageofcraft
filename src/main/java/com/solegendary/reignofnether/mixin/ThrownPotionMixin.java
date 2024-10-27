package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchLingeringPotions;
import com.solegendary.reignofnether.unit.units.villagers.WitchUnit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin extends Projectile {

    protected ThrownPotionMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(
            method = "makeAreaOfEffectCloud",
            at = @At("HEAD"),
            cancellable = true
    )
    private void makeAreaOfEffectCloud(ItemStack pStack, Potion pPotion, CallbackInfo ci) {
        ci.cancel();

        AreaEffectCloud aec = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
        if (this.getOwner() instanceof LivingEntity le) {
            aec.setOwner(le);
        }

        aec.setRadius(3.0F);
        aec.setRadiusOnUse(-0.5F);
        aec.setWaitTime(10);

        if (this.getOwner() instanceof WitchUnit witchUnit) {
            aec.setRadius(3.0F);
            aec.setRadiusOnUse(0);
            aec.setDurationOnUse(0);
            aec.setWaitTime(10);

            int duration = WitchUnit.LINGERING_POTION_DURATION;
            if (this.level.isClientSide() && ResearchClient.hasResearch(ResearchLingeringPotions.itemName)) {
                duration = WitchUnit.LINGERING_POTION_DURATION_EXTENDED;
            } else if (!this.level.isClientSide() && ResearchServerEvents.playerHasResearch(witchUnit.getOwnerName(), ResearchLingeringPotions.itemName)) {
                duration = WitchUnit.LINGERING_POTION_DURATION_EXTENDED;
            }
            aec.setDuration(duration);
        }

        aec.setRadiusPerTick(-aec.getRadius() / (float)aec.getDuration());
        aec.setPotion(pPotion);

        for (MobEffectInstance $$4 : PotionUtils.getCustomEffects(pStack)) {
            aec.addEffect(new MobEffectInstance($$4));
        }

        CompoundTag $$5 = pStack.getTag();
        if ($$5 != null && $$5.contains("CustomPotionColor", 99)) {
            aec.setFixedColor($$5.getInt("CustomPotionColor"));
        }

        this.level.addFreshEntity(aec);
    }
}
