//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.solegendary.reignofnether.unit.units.modelling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// based on EvokerRenderer
@OnlyIn(Dist.CLIENT)
public class EvokerUnitRenderer extends AbstractVillagerUnitRenderer<EvokerUnit> {
    private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("reignofnether", "textures/entities/evoker_unit.png");

    public EvokerUnitRenderer(EntityRendererProvider.Context p_174108_) {
        super(p_174108_, new VillagerUnitModel(p_174108_.bakeLayer(ModelLayers.EVOKER)), 0.5F);

        // do not render any held items
        /* this.addLayer(new ItemInHandLayer<EvokerUnit, VillagerUnitModel<EvokerUnit>>(this, p_174108_.getItemInHandRenderer()) {
            public void render(PoseStack p_114569_, MultiBufferSource p_114570_, int p_114571_, EvokerUnit evokerUnit, float p_114573_, float p_114574_, float p_114575_, float p_114576_, float p_114577_, float p_114578_) {
                if (evokerUnit.isCastingSpell()) {
                    super.render(p_114569_, p_114570_, p_114571_, evokerUnit, p_114573_, p_114574_, p_114575_, p_114576_, p_114577_, p_114578_);
                }
            }
        }); */
    }

    public ResourceLocation getTextureLocation(EvokerUnit pEntity) {
        return EVOKER_ILLAGER;
    }
}
