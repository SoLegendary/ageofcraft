package com.solegendary.reignofnether.unit.interfaces;

import com.solegendary.reignofnether.unit.goals.RangedFlyingAttackGroundGoal;
import net.minecraft.world.entity.LivingEntity;

public interface RangedAttackerUnit {

    int FOG_REVEAL_TICKS_MAX = 60;

    int getFogRevealDuration();
    void setFogRevealDuration(int duration);

    default RangedFlyingAttackGroundGoal<?> getRangedAttackGroundGoal() { return null; }

    default void performUnitRangedAttack(LivingEntity pTarget, float velocity) {
        double x = pTarget.getX();
        double y = pTarget.getY();
        double z = pTarget.getZ();
        performUnitRangedAttack(x, y, z, velocity);
    }

    // attack ground
    default void performUnitRangedAttack(double x, double y, double z, float velocity) { }
}
