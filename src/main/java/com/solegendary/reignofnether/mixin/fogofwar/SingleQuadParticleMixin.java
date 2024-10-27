package com.solegendary.reignofnether.mixin.fogofwar;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// prevent rendering particles in non-bright chunks

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin extends Particle {

    protected SingleQuadParticleMixin(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    // method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks, CallbackInfo ci) {
        if (!FogOfWarClientEvents.isEnabled())
            return;
        BlockPos bp = new BlockPos(x,y,z);
        if (!FogOfWarClientEvents.isInBrightChunk(bp))
            ci.cancel();
    }
}
