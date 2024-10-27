package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;

public class CreeperAttackUnitGoal extends AbstractMeleeAttackUnitGoal {
    private final CreeperUnit creeperUnit;

    public CreeperAttackUnitGoal(CreeperUnit creeperUnit, int attackInterval, boolean followingTargetEvenIfNotSeen) {
        super(creeperUnit, attackInterval, followingTargetEvenIfNotSeen);
        this.creeperUnit = creeperUnit;
    }

    public void tick() {
        if (creeperUnit.canExplodeOnTarget())
            creeperUnit.setSwellDir(1);
    }
}
