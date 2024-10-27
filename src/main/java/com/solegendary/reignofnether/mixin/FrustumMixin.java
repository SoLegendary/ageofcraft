package com.solegendary.reignofnether.mixin;

import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public class FrustumMixin {
    // I have no idea why this is needed but without it the game freezes and gets stuck inside
    // this function forever a few seconds after activating orthoView
    @Inject(
            method = "cubeCompletelyInFrustum(FFFFFF)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cubeCompletelyInFrustum(
            float f1, float f2, float f3, float f4, float f5, float f6,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (OrthoviewClientEvents.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}