package com.solegendary.reignofnether.unit.goals;

import net.minecraft.world.entity.Mob;

public class MeleeAttackUnitGoal extends AbstractMeleeAttackUnitGoal {
    private final Mob mob;
    private int raiseArmTicks; // for zombies

    public MeleeAttackUnitGoal(Mob mob, int attackInterval, boolean followingTargetEvenIfNotSeen) {
        super(mob, attackInterval, followingTargetEvenIfNotSeen);
        this.mob = mob;
    }

    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
    }

    public void tick() {
        super.tick();
        ++this.raiseArmTicks;
        if (this.raiseArmTicks >= 5 && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2) {
            this.mob.setAggressive(true);
        } else {
            this.mob.setAggressive(false);
        }

    }
}
