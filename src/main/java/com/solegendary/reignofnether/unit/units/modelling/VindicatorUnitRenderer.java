package com.solegendary.reignofnether.unit.units.modelling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.units.villagers.VindicatorUnit;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// based on VindicatorRenderer
@OnlyIn(Dist.CLIENT)
public class VindicatorUnitRenderer extends AbstractVillagerUnitRenderer<VindicatorUnit> {
    private static final ResourceLocation VINDICATOR_UNIT = new ResourceLocation("reignofnether", "textures/entities/vindicator_unit.png");

    public VindicatorUnitRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_, new VillagerUnitModel<>(p_174439_.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<VindicatorUnit, VillagerUnitModel<VindicatorUnit>>(this, p_174439_.getItemInHandRenderer()) {
            public void render(PoseStack p_116352_, MultiBufferSource p_116353_, int p_116354_, VindicatorUnit unit, float p_116356_, float p_116357_, float p_116358_, float p_116359_, float p_116360_, float p_116361_) {
                if (unit.getTarget() != null || (unit.getAttackBuildingGoal() instanceof MeleeAttackBuildingGoal mabg && mabg.getBuildingTarget() != null)) {
                    super.render(p_116352_, p_116353_, p_116354_, unit, p_116356_, p_116357_, p_116358_, p_116359_, p_116360_, p_116361_);
                }
            }
        });
    }

    public ResourceLocation getTextureLocation(VindicatorUnit p_116324_) {
        return VINDICATOR_UNIT;
    }
}