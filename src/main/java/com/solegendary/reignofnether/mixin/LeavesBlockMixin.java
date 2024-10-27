package com.solegendary.reignofnether.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

    @Inject(
            method = "getDistanceAt",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void getDistanceAt(BlockState pNeighbor, CallbackInfoReturnable<Integer> cir) {
        if (pNeighbor.is(Blocks.CRIMSON_STEM) || pNeighbor.is(Blocks.WARPED_STEM))
            cir.setReturnValue(0);
    }
}
