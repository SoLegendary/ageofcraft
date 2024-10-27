package com.solegendary.reignofnether.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// disable spectator GUI entirely when in orthoview mode as it has cheaty functions like tp to player

@Mixin(Gui.class)
public class TopdownGuiMixin {

    @Inject(
            method = "renderHotbar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHotbarSelected(
            float f, PoseStack ps, CallbackInfo ci
    ) {
        if (OrthoviewClientEvents.isEnabled())
            ci.cancel();
    }

    @Inject(
            method = "renderCrosshair",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHotbarSelected(
            PoseStack ps, CallbackInfo ci
    ) {
        if (OrthoviewClientEvents.isEnabled())
            ci.cancel();
    }
}
