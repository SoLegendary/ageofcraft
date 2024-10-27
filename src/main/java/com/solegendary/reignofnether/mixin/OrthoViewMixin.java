package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(GameRenderer.class)
public class OrthoViewMixin {
    // applies an orthographic projection matrix
    @Inject(
            method = "getProjectionMatrix(D)Lcom/mojang/math/Matrix4f;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getProjectionMatrix(
            double d,
            CallbackInfoReturnable<Matrix4f> cir
    ) {
        if (OrthoviewClientEvents.isEnabled()) {
            cir.setReturnValue(OrthoviewClientEvents.getOrthographicProjection());
        }
    }
}