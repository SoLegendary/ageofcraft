package com.solegendary.reignofnether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
public class ForgeGuiMixin extends Gui {

    public ForgeGuiMixin(Minecraft pMinecraft, ItemRenderer pItemRenderer) {
        super(pMinecraft, pItemRenderer);
    }

    // moves the chat history window up above bottom left hotkeys
    @Inject(
            method = "renderChat",
            at = @At("HEAD"),
            cancellable = true,
            remap=false
    )
    protected void renderChat(int width, int height, PoseStack pStack, CallbackInfo ci) {
        if (!OrthoviewClientEvents.isEnabled())
            return;

        ci.cancel();

        this.minecraft.getProfiler().push("chat");
        CustomizeGuiOverlayEvent.Chat event = new CustomizeGuiOverlayEvent.Chat(this.minecraft.getWindow(), pStack, this.minecraft.getFrameTime(), 0, height - 48);
        MinecraftForge.EVENT_BUS.post(event);
        pStack.pushPose();
        pStack.translate(event.getPosX(), event.getPosY() - 40, 0.0);
        this.chat.render(pStack, this.tickCount);
        pStack.popPose();
        this.minecraft.getProfiler().pop();
    }
}