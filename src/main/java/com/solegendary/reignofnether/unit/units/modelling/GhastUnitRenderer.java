//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.solegendary.reignofnether.unit.units.modelling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// same as regular GhastRenderer but downscaled
// remember the scaling done here must match that done in EntityRegistrar to ensure the hitbox is consistent

@OnlyIn(Dist.CLIENT)
public class GhastUnitRenderer extends MobRenderer<Ghast, GhastModel<Ghast>> {

    public static final float SCALE_MULT = 0.75f;

    private static final ResourceLocation GHAST_LOCATION = new ResourceLocation("textures/entity/ghast/ghast.png");
    private static final ResourceLocation GHAST_SHOOTING_LOCATION = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

    public GhastUnitRenderer(EntityRendererProvider.Context p_174129_) {
        super(p_174129_, new GhastModel(p_174129_.bakeLayer(ModelLayers.GHAST)), 1.5F);
    }

    public ResourceLocation getTextureLocation(Ghast ghast) {
        if (ghast instanceof GhastUnit ghastUnit)
            return ghastUnit.isShooting() ? GHAST_SHOOTING_LOCATION : GHAST_LOCATION;
        return ghast.isCharging() ? GHAST_SHOOTING_LOCATION : GHAST_LOCATION;
    }

    protected void scale(Ghast pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        pMatrixStack.scale(4.5f * SCALE_MULT, 4.5f * SCALE_MULT, 4.5f * SCALE_MULT);
    }
}
