package com.solegendary.reignofnether.mixin;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(Witch.class)
public class WitchMixin {

    @Shadow private NearestHealableRaiderTargetGoal<Raider> healRaidersGoal;
    @Shadow private NearestAttackableWitchTargetGoal<Player> attackPlayersGoal;

    @Inject(
            method = "aiStep",
            at = @At("HEAD")
    )
    public void aiStep(CallbackInfo ci) {
        if (healRaidersGoal == null)
            healRaidersGoal = new NearestHealableRaiderTargetGoal(((Witch)(Object)this), Raider.class, true, (p_34159_) -> false);
        if (attackPlayersGoal == null)
            attackPlayersGoal = new NearestAttackableWitchTargetGoal(((Witch)(Object)this), Player.class, 10, true, false, (Predicate)null);
    }
}
