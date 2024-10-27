package com.solegendary.reignofnether.unit.units.modelling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;

public class PoisonSpiderUnitRenderer extends SpiderRenderer<CaveSpider> {

    private static final ResourceLocation CAVE_SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/cave_spider.png");

    public PoisonSpiderUnitRenderer(EntityRendererProvider.Context p_173946_) {
        super(p_173946_, ModelLayers.CAVE_SPIDER);
    }

    protected void scale(CaveSpider pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        pMatrixStack.scale(1.0f, 1.0f, 1.0f);
    }

    public ResourceLocation getTextureLocation(CaveSpider pEntity) {
        return CAVE_SPIDER_LOCATION;
    }
}
