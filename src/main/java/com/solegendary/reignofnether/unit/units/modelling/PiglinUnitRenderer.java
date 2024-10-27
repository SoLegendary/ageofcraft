//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.solegendary.reignofnether.unit.units.modelling;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

import com.solegendary.reignofnether.registrars.EntityRegistrar;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinUnitRenderer extends HumanoidMobRenderer<Mob, PiglinUnitModel<Mob>> {
    private static final Map<EntityType<?>, ResourceLocation> TEXTURES;
    private static final float PIGLIN_CUSTOM_HEAD_SCALE = 1.0019531F;

    // See class EntityRenderers for the original code
    public PiglinUnitRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, createModel(pContext.getModelSet(), ModelLayers.PIGLIN, false), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
        this.addLayer(new HumanoidArmorLayer(this,
                    new HumanoidModel(pContext.bakeLayer(ModelLayers.PIGLIN_INNER_ARMOR)),
                    new HumanoidModel(pContext.bakeLayer(ModelLayers.PIGLIN_OUTER_ARMOR))));
    }

    private static PiglinUnitModel<Mob> createModel(EntityModelSet p_174350_, ModelLayerLocation pLayer, boolean p_174352_) {
        PiglinUnitModel<Mob> $$3 = new PiglinUnitModel(p_174350_.bakeLayer(pLayer));
        if (p_174352_) {
            $$3.rightEar.visible = false;
        }

        return $$3;
    }

    public ResourceLocation getTextureLocation(Mob pEntity) {
        ResourceLocation $$1 = TEXTURES.get(pEntity.getType());
        if ($$1 == null) {
            throw new IllegalArgumentException("I don't know what texture to use for " + pEntity.getType());
        } else {
            return $$1;
        }
    }

    static {
        TEXTURES = ImmutableMap.of(
            EntityRegistrar.GRUNT_UNIT.get(), new ResourceLocation("textures/entity/piglin/piglin.png"),
            EntityRegistrar.BRUTE_UNIT.get(), new ResourceLocation("textures/entity/piglin/piglin_brute.png"),
            EntityRegistrar.HEADHUNTER_UNIT.get(), new ResourceLocation("textures/entity/piglin/piglin_brute.png"),
            EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get(), new ResourceLocation("textures/entity/piglin/zombified_piglin.png")
        );
    }
}
