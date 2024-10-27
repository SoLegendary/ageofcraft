package com.solegendary.reignofnether.mixin.fogofwar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;

// brightness shading for liquids and flat flace blocks (like tall grass)

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    @Shadow @Final private BlockColors blockColors;

    @Inject(
            method = "putQuadData",
            at = @At("HEAD"),
            cancellable = true
    )
    private void putQuadData(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose,
                                     BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3,
                                     int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay, CallbackInfo ci) {
        boolean allBrightnesses1 =
                pBrightness0 == pBrightness1 &&
                pBrightness1 == pBrightness2 &&
                pBrightness2 == pBrightness3 &&
                pBrightness3 == 1.0f;

        if (!allBrightnesses1) //!FogOfWarClientEvents.isEnabled() ||
            return;

        ci.cancel();

        float f;
        float f1;
        float f2;
        if (pQuad.isTinted()) {
            int i = this.blockColors.getColor(pState, pLevel, pPos, pQuad.getTintIndex());
            f = (float)(i >> 16 & 255) / 255.0F;
            f1 = (float)(i >> 8 & 255) / 255.0F;
            f2 = (float)(i & 255) / 255.0F;
        } else {
            f = 1.0F;
            f1 = 1.0F;
            f2 = 1.0F;
        }
        float br = FogOfWarClientEvents.getPosBrightness(pPos);

        pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0 * br, pBrightness1 * br, pBrightness2 * br, pBrightness3 * br},
                f, f1, f2, new int[]{pLightmap0, pLightmap1, pLightmap2, pLightmap3}, pPackedOverlay, true);
    }
}
