package com.solegendary.reignofnether.mixin.fogofwar;

import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// brightness shading for blocks excluding liquids and flat flace blocks (like tall grass)

@Mixin(ModelBlockRenderer.Cache.class)
public abstract class ModelBlockRendererCacheMixin {

    @Shadow private boolean enabled;
    @Final @Shadow private Long2FloatLinkedOpenHashMap brightnessCache;

    @Inject(
            method = "getShadeBrightness",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getShadeBrightness(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Float> cir) {
        //if (!FogOfWarClientEvents.isEnabled())
        //    return;

        cir.cancel();

        float brightnessMulti = FogOfWarClientEvents.getPosBrightness(pPos);

        long i = pPos.asLong();
        if (this.enabled) {
            float f = this.brightnessCache.get(i);
            if (!Float.isNaN(f))
                cir.setReturnValue(f);
        }

        float f1 = pState.getShadeBrightness(pLevel, pPos) * brightnessMulti;
        if (this.enabled) {
            if (this.brightnessCache.size() == 100)
                this.brightnessCache.removeFirstFloat();

            this.brightnessCache.put(i, f1);
        }
        cir.setReturnValue(f1);
    }
}
