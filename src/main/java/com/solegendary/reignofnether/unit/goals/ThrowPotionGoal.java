package com.solegendary.reignofnether.unit.goals;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.AbilityClientboundPacket;
import com.solegendary.reignofnether.unit.units.villagers.WitchUnit;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.phys.Vec3;

public class ThrowPotionGoal extends MoveToTargetBlockGoal {

    private Potion potion = null;
    private LivingEntity targetEntity = null;
    private Ability ability; // used for syncing cooldown with clientside

    public ThrowPotionGoal(Mob mob) {
        super(mob, false, 0);
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
    }
    public void setAbility(Ability ability) {
        this.ability = ability;
    }

    // set the target to throw a potion - the witch will move towards this location until we're within range
    // then throw a potion at it
    // if we set an entity target, on every tick we will follow that target
    // if we set a BlockPos as the target, remove any entity target
    public void setTarget(LivingEntity entity) {
        this.targetEntity = entity;
    }
    public void setTarget(BlockPos bpTarget) {
        this.setMoveTarget(bpTarget);
        this.setTarget((LivingEntity) null);
    }

    @Override
    public void tick() {
        if (this.targetEntity != null)
            this.setMoveTarget(this.targetEntity.getOnPos());

        if (moveTarget != null) {

            WitchUnit witch = (WitchUnit) this.mob;

            if (MyMath.distance(
                this.mob.getX(), this.mob.getZ(),
                moveTarget.getX(), moveTarget.getZ()) <= witch.getPotionThrowRange()) {

                this.mob.getLookControl().setLookAt(moveTarget.getX(), moveTarget.getY(), moveTarget.getZ());
                if (moveTarget != null)
                    witch.throwPotion(new Vec3(moveTarget.getX(), moveTarget.getY(), moveTarget.getZ()), this.potion);
                if (this.ability != null && !this.mob.level.isClientSide()) {
                    AbilityClientboundPacket.sendSetCooldownPacket(this.mob.getId(), this.ability.action, this.ability.cooldownMax);
                }
                this.stop();
            }
        }
    }

    @Override
    public void stop() {
        this.stopMoving();
        this.setTarget((LivingEntity) null);
        this.setPotion(null);
        this.setAbility(null);
    }
}
