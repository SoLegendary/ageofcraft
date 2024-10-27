package com.solegendary.reignofnether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.solegendary.reignofnether.unit.units.piglins.BruteUnit;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    @Final @Shadow private ItemInHandRenderer itemInHandRenderer;

    public ItemInHandLayerMixin(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void renderArmWithItem(LivingEntity pLivingEntity, ItemStack pItemStack, ItemTransforms.TransformType pTransformType,
                                     HumanoidArm pArm, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (pLivingEntity instanceof BruteUnit brute &&
                pItemStack.getItem() == Items.SHIELD &&
                brute.isHoldingUpShield) {

            ci.cancel();

            if (!pItemStack.isEmpty()) {
                pPoseStack.pushPose();
                this.getParentModel().translateToHand(pArm, pPoseStack);
                pPoseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                boolean $$7 = pArm == HumanoidArm.LEFT;
                pPoseStack.translate((float) ($$7 ? -1 : 1) / 2.5f, 0.125f, -0.225f);

                this.itemInHandRenderer.renderItem(pLivingEntity, pItemStack, pTransformType, $$7, pPoseStack, pBuffer, pPackedLight);

                pPoseStack.popPose();
            }
        }
    }
}
