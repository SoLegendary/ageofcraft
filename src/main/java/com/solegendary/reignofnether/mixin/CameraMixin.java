package com.solegendary.reignofnether.mixin;

import com.mojang.math.Vector3f;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Vec3 position;
    @Shadow protected void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset) { }
    @Shadow private double getMaxZoom(double pStartingDistance) { return pStartingDistance; }
    @Shadow protected void setPosition(Vec3 pPos) { }
    @Shadow protected void setPosition(double pX, double pY, double pZ) { }
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow protected void setRotation(float pYRot, float pXRot) { }
    @Shadow private boolean detached;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow private boolean initialized;
    @Shadow private BlockGetter level;
    @Shadow private Entity entity;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;

    // when orthoview is enabled, set the camera to 3rd person and move it further back than normal
    // so that we still render chunks if the orthoview player is inside blocks

    @Inject(
            method = "move",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset, CallbackInfo ci) {
        if (!OrthoviewClientEvents.isEnabled())
            return;
        ci.cancel();
        pDistanceOffset -= 20;
        double d0 = (double)this.forwards.x() * pDistanceOffset + (double)this.up.x() * pVerticalOffset + (double)this.left.x() * pHorizontalOffset;
        double d1 = (double)this.forwards.y() * pDistanceOffset + (double)this.up.y() * pVerticalOffset + (double)this.left.y() * pHorizontalOffset;
        double d2 = (double)this.forwards.z() * pDistanceOffset + (double)this.up.z() * pVerticalOffset + (double)this.left.z() * pHorizontalOffset;
        this.setPosition(new Vec3(this.position.x + d0, this.position.y + d1, this.position.z + d2));
    }

    @Inject(
            method = "setup",
            at = @At("TAIL")
    )
    public void setup(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse,
                      float pPartialTick, CallbackInfo ci) {
        this.initialized = true;
        this.level = pLevel;
        this.entity = pEntity;
        this.detached = pDetached || OrthoviewClientEvents.isEnabled();
        this.setRotation(pEntity.getViewYRot(pPartialTick), pEntity.getViewXRot(pPartialTick));
        this.setPosition(Mth.lerp((double)pPartialTick, pEntity.xo, pEntity.getX()), Mth.lerp((double)pPartialTick, pEntity.yo, pEntity.getY()) + (double)Mth.lerp(pPartialTick, this.eyeHeightOld, this.eyeHeight), Mth.lerp((double)pPartialTick, pEntity.zo, pEntity.getZ()));
        if (pDetached) {
            if (pThirdPersonReverse) {
                this.setRotation(this.yRot + 180.0F, -this.xRot);
            }
            this.move(-this.getMaxZoom(4.0), 0.0, 0.0);
        } else if (pEntity instanceof LivingEntity && ((LivingEntity)pEntity).isSleeping()) {
            Direction direction = ((LivingEntity)pEntity).getBedOrientation();
            this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
            this.move(0.0, 0.3, 0.0);
        }
    }
}
