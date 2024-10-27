package com.solegendary.reignofnether.mixin.fogofwar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// brightness shading for blocks excluding liquids and flat flace blocks (like tall grass)

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityShadowMixin {

    @Inject(
            method = "renderShadow",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onRenderShadow(PoseStack pMatrixStack, MultiBufferSource pBuffer, Entity pEntity, float pWeight, float pPartialTicks, LevelReader pLevel, float pSize, CallbackInfo ci) {
        if (OrthoviewClientEvents.isEnabled() &&
            pEntity == Minecraft.getInstance().player) {
            ci.cancel();
            return;
        }

        if (!FogOfWarClientEvents.isEnabled())
            return;

        BlockPos bp = pEntity.getOnPos();

        if (!FogOfWarClientEvents.isInBrightChunk(pEntity))
            ci.cancel();
    }
}
