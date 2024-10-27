package com.solegendary.reignofnether.unit.units.modelling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// based on IllagerRenderer
@OnlyIn(Dist.CLIENT)
public abstract class AbstractVillagerUnitRenderer<T extends AbstractIllager> extends MobRenderer<T, VillagerUnitModel<T>> {
    protected AbstractVillagerUnitRenderer(EntityRendererProvider.Context p_174182_, VillagerUnitModel<T> p_174183_, float p_174184_) {
        super(p_174182_, p_174183_, p_174184_);
        this.addLayer(new CustomHeadLayer<>(this, p_174182_.getModelSet(), p_174182_.getItemInHandRenderer()));
    }

    protected void scale(T p_114919_, PoseStack p_114920_, float p_114921_) {
        float f = 0.9375F;
        p_114920_.scale(0.9375F, 0.9375F, 0.9375F);
    }
}