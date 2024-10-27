//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.solegendary.reignofnether.unit.units.modelling;

import com.solegendary.reignofnether.unit.interfaces.ArmSwingingUnit;
import com.solegendary.reignofnether.unit.units.piglins.BruteUnit;
import com.solegendary.reignofnether.unit.units.piglins.HeadhunterUnit;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PiglinUnitModel<T extends Mob> extends PlayerModel<T> {
    public final ModelPart rightEar;
    private final ModelPart leftEar;
    private final PartPose bodyDefault;
    private final PartPose headDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;

    public PiglinUnitModel(ModelPart p_170810_) {
        super(p_170810_, false);
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.bodyDefault = this.body.storePose();
        this.headDefault = this.head.storePose();
        this.leftArmDefault = this.leftArm.storePose();
        this.rightArmDefault = this.rightArm.storePose();
    }

    public static MeshDefinition createMesh(CubeDeformation pCubeDeformation) {
        MeshDefinition $$1 = PlayerModel.createMesh(pCubeDeformation, false);
        PartDefinition $$2 = $$1.getRoot();
        $$2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.ZERO);
        PartDefinition $$3 = $$2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, pCubeDeformation).texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, pCubeDeformation).texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, pCubeDeformation).texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, pCubeDeformation), PartPose.ZERO);
        $$3.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, pCubeDeformation), PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));
        $$3.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, pCubeDeformation), PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
        $$2.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        return $$1;
    }

    public void setupAnim(T entity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.body.loadPose(this.bodyDefault);
        this.head.loadPose(this.headDefault);
        this.leftArm.loadPose(this.leftArmDefault);
        this.rightArm.loadPose(this.rightArmDefault);
        super.setupAnim(entity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        float $$6 = 0.5235988F;
        float $$7 = pAgeInTicks * 0.1F + pLimbSwing * 0.5F;
        float $$8 = 0.08F + pLimbSwingAmount * 0.4F;
        this.leftEar.zRot = -0.5235988F - Mth.cos($$7 * 1.2F) * $$8;
        this.rightEar.zRot = 0.5235988F + Mth.cos($$7) * $$8;
        if (entity instanceof AbstractPiglin $$9) {
            PiglinArmPose $$10 = $$9.getArmPose();
            if ($$10 == PiglinArmPose.DANCING) {
                float $$11 = pAgeInTicks / 60.0F;
                this.rightEar.zRot = 0.5235988F + 0.017453292F * Mth.sin($$11 * 30.0F) * 10.0F;
                this.leftEar.zRot = -0.5235988F - 0.017453292F * Mth.cos($$11 * 30.0F) * 10.0F;
                this.head.x = Mth.sin($$11 * 10.0F);
                this.head.y = Mth.sin($$11 * 40.0F) + 0.4F;
                this.rightArm.zRot = 0.017453292F * (70.0F + Mth.cos($$11 * 40.0F) * 10.0F);
                this.leftArm.zRot = this.rightArm.zRot * -1.0F;
                this.rightArm.y = Mth.sin($$11 * 40.0F) * 0.5F + 1.5F;
                this.leftArm.y = Mth.sin($$11 * 40.0F) * 0.5F + 1.5F;
                this.body.y = Mth.sin($$11 * 40.0F) * 0.35F;
            } else if ($$10 == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0F) {
                this.holdWeaponHigh(entity);
            } else if ($$10 == PiglinArmPose.CROSSBOW_HOLD) {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !entity.isLeftHanded());
            } else if ($$10 == PiglinArmPose.CROSSBOW_CHARGE) {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, !entity.isLeftHanded());
            } else if ($$10 == PiglinArmPose.ADMIRING_ITEM) {
                this.head.xRot = 0.5F;
                this.head.yRot = 0.0F;
                if (entity.isLeftHanded()) {
                    this.rightArm.yRot = -0.5F;
                    this.rightArm.xRot = -0.9F;
                } else {
                    this.leftArm.yRot = 0.5F;
                    this.leftArm.xRot = -0.9F;
                }
            }
        } else if (entity.getType() == EntityType.ZOMBIFIED_PIGLIN) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, entity.isAggressive(), this.attackTime, pAgeInTicks);
        }

        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        this.hat.copyFrom(this.head);

        if (entity instanceof ArmSwingingUnit armSwinger &&
            (armSwinger.isSwingingArmRepeatedly() ||
             armSwinger.isSwingingArmOnce())) {

            List<Float> armRots = armSwinger.getNextArmRot();
            this.rightArm.xRot = armRots.get(0);
            this.rightArm.yRot = armRots.get(1);
            this.rightArm.zRot = armRots.get(2);

            int swingTime = armSwinger.getSwingTime();
            armSwinger.setSwingTime(swingTime + 1);

            if (swingTime >= ArmSwingingUnit.SWING_TIME_MAX) {
                armSwinger.setSwingTime(0);
                if (armSwinger.isSwingingArmOnce())
                    armSwinger.setSwingingArmOnce(false);
            }
        }

        if (entity instanceof HeadhunterUnit headhunter && headhunter.getTarget() != null) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - 3.1415927F;
            this.rightArm.yRot = 0.0F;
        }

        if (entity instanceof BruteUnit brute &&
            entity.getOffhandItem().getItem() == Items.SHIELD &&
            brute.isHoldingUpShield) {
            this.leftArm.xRot = -1.5f;
        }
    }

    protected void setupAttackAnimation(T pLivingEntity, float pAgeInTicks) {
        if (this.attackTime > 0.0F && pLivingEntity instanceof Piglin && ((Piglin)pLivingEntity).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, pLivingEntity, this.attackTime, pAgeInTicks);
        } else {
            super.setupAttackAnimation(pLivingEntity, pAgeInTicks);
        }
    }

    private void holdWeaponHigh(T pMob) {
        if (pMob.isLeftHanded()) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }
    }
}
