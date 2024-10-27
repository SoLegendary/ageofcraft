//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.solegendary.reignofnether.unit.goals;

import java.util.EnumSet;

import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class RandomLookAroundUnitGoal extends Goal {
    private final Mob mob;
    private double relX;
    private double relZ;
    private int lookTime;

    public RandomLookAroundUnitGoal(Mob pMob) {
        this.mob = pMob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (mob instanceof AttackerUnit attackerUnit)
            if (attackerUnit.getAttackBuildingGoal() instanceof MeleeAttackBuildingGoal mabg && mabg.isAttacking())
                return false;

        if (mob instanceof AttackerUnit attackerUnit)
            if (attackerUnit.getAttackBuildingGoal() instanceof MeleeAttackBuildingGoal mabg && mabg.isAttacking())
                return false;

        return this.mob.getRandom().nextFloat() < 0.02F;
    }

    public boolean canContinueToUse() {
        return this.lookTime >= 0;
    }

    public void start() {
        double $$0 = 6.283185307179586 * this.mob.getRandom().nextDouble();
        this.relX = Math.cos($$0);
        this.relZ = Math.sin($$0);
        this.lookTime = 20 + this.mob.getRandom().nextInt(20);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        --this.lookTime;
        this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
    }
}
