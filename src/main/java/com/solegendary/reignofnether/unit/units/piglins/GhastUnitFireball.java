package com.solegendary.reignofnether.unit.units.piglins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;

public class GhastUnitFireball extends LargeFireball {


    public GhastUnitFireball(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, int pExplosionPower) {
        super(pLevel, pShooter, pOffsetX, pOffsetY, pOffsetZ, pExplosionPower);
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }
}
