package com.solegendary.reignofnether.mixin.fogofwar;

import com.google.common.collect.Lists;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public abstract class CompiledChunkMixin {

    @Shadow @Final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();

    @Inject(
        method = "getRenderableBlockEntities",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getRenderableBlockEntities(CallbackInfoReturnable<List<BlockEntity>> cir) {
        if (!FogOfWarClientEvents.isEnabled())
            return;

        List<BlockEntity> blockEntities = new ArrayList<>();

        for (BlockEntity be : renderableBlockEntities)
            if (FogOfWarClientEvents.isInBrightChunk(be.getBlockPos()))
                blockEntities.add(be);

        cir.setReturnValue(blockEntities);
    }
}
