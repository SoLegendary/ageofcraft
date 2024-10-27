package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.WitherSkeletonUnit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

// based on MeleeAttackGoal
public abstract class AbstractMeleeAttackUnitGoal extends Goal {
    protected final Mob mob;
    private final boolean followingTargetEvenIfNotSeen;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    private final int attackInterval;
    private long lastCanUseCheck;

    public AbstractMeleeAttackUnitGoal(Mob mob, int attackInterval, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.attackInterval = attackInterval;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void tickAttackCooldown() {
        if (ticksUntilNextAttack > 0) // tick down even when not targeting anything
            this.ticksUntilNextAttack -= 1;
    }

    public boolean canUse() {
        long i = this.mob.level.getGameTime();
        if (i - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = i;
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                }
            }
        }
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
            return false;
        } else {
            boolean canContinue = !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
            if (canContinue) {
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                this.mob.getNavigation().moveTo(this.path,  Unit.getSpeedModifier((Unit) this.mob));
            }
            return canContinue;
        }
    }

    public void start() {
        if (!((Unit) this.mob).getHoldPosition())
            this.mob.getNavigation().moveTo(this.path,  Unit.getSpeedModifier((Unit) this.mob));
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
    }

    public void stop() {
        LivingEntity livingentity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.mob.setTarget(null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            double d0 = this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            if (!((Unit) this.mob).getHoldPosition()) {
                if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) &&
                        this.ticksUntilNextPathRecalculation <= 0 &&
                        (this.pathedTargetX == 0.0D &&
                            this.pathedTargetY == 0.0D &&
                            this.pathedTargetZ == 0.0D ||
                            livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D ||
                            this.mob.getRandom().nextFloat() < 0.05F))
                {
                    this.pathedTargetX = livingentity.getX();
                    this.pathedTargetY = livingentity.getY();
                    this.pathedTargetZ = livingentity.getZ();
                }
            }
            this.checkAndPerformAttack(livingentity, d0);
        }
    }

    protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
        double d0 = this.getAttackReachSqr(p_25557_);
        if (p_25558_ <= d0 && this.ticksUntilNextAttack <= 0) {
            this.ticksUntilNextAttack = this.adjustedTickDelay(attackInterval);
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(p_25557_);
        }
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected int getAttackInterval() {
        return this.adjustedTickDelay(attackInterval);
    }

    protected double getAttackReachSqr(LivingEntity p_25556_) {
        return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + p_25556_.getBbWidth());
    }
}
