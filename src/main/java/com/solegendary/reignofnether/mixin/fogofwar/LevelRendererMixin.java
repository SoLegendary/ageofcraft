package com.solegendary.reignofnether.mixin.fogofwar;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.fogofwar.FrozenChunk;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.util.MiscUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents.*;


@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Final @Shadow private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;
    @Final @Shadow private AtomicReference<LevelRenderer.RenderChunkStorage> renderChunkStorage = new AtomicReference<>();
    @Final @Shadow private Minecraft minecraft;
    @Final @Shadow private RenderBuffers renderBuffers;
    @Final @Shadow private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow private ChunkRenderDispatcher chunkRenderDispatcher;
    @Shadow private ClientLevel level;

    private static final ObjectArrayList<LevelRenderer.RenderChunkInfo> lastRenderChunksInFrustum = new ObjectArrayList<>();

    private List<Pair<BlockPos, Integer>> chunksToReDirty = new ArrayList<>();

    private int distToChunk(ChunkPos chunkPos) {
        if (minecraft.player == null)
            return 0;
        return minecraft.player.chunkPosition().getChessboardDistance(chunkPos);
    }

    @Inject(
            method = "applyFrustum(Lnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyFrustum(Frustum pFrustum, CallbackInfo ci) {
        if (!FogOfWarClientEvents.isEnabled())
            return;

        ci.cancel();

        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        } else {
            this.minecraft.getProfiler().push("apply_frustum");
            this.renderChunksInFrustum.clear();
            Set<ChunkPos> chunksToRefreshDone = new HashSet<>();

            int chunksDirtied = 0;

            for (LevelRenderer.RenderChunkInfo chunkInfo : this.renderChunkStorage.get().renderChunks) {
                if (pFrustum.isVisible(chunkInfo.chunk.getBoundingBox())) {
                    this.renderChunksInFrustum.add(chunkInfo);
                }
                if (minecraft.level != null) {
                    ChunkPos cpos = minecraft.level.getChunk(chunkInfo.chunk.getOrigin()).getPos();
                    if (FogOfWarClientEvents.chunksToRefresh.contains(cpos)) {
                        chunkInfo.chunk.setDirty(true);
                        chunksToRefreshDone.add(cpos);
                        chunksDirtied += 1;
                    }
                }
            }
            // if (chunksDirtied > 0)
            //    System.out.println("set renderChunks dirty: " + chunksDirtied);

            FogOfWarClientEvents.chunksToRefresh.removeAll(chunksToRefreshDone);

            //if (chunksToRefreshDone.size() > 0)
            //    System.out.println("refreshed " + chunksToRefreshDone.size() + " chunks, " + FogOfWarClientEvents.chunksToRefresh.size() + " remaining");

            FogOfWarClientEvents.renderChunksInFrustum.clear();
            FogOfWarClientEvents.renderChunksInFrustum.addAll(renderChunksInFrustum);

            this.minecraft.getProfiler().pop();
        }
    }

    @Inject(
            method = "compileChunks(Lnet/minecraft/client/Camera;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void compileChunks(Camera pCamera, CallbackInfo ci) {

        // hiding leaves around cursor
        if (OrthoviewClientEvents.hideLeavesMethod == OrthoviewClientEvents.LeafHideMethod.AROUND_UNITS_AND_CURSOR &&
                OrthoviewClientEvents.isEnabled()) {
            UnitClientEvents.windowUpdateTicks -= 1;
            if (UnitClientEvents.windowUpdateTicks <= 0) {
                UnitClientEvents.windowUpdateTicks = UnitClientEvents.WINDOW_UPDATE_TICKS_MAX;
                Vec3 centrePos = MiscUtil.getOrthoviewCentreWorldPos(Minecraft.getInstance());
                for(LevelRenderer.RenderChunkInfo chunkInfo : this.renderChunksInFrustum) {
                    BlockPos chunkCentreBp = chunkInfo.chunk.getOrigin().offset(8.5d, 8.5d, 8.5d);

                    List<Pair<BlockPos, Integer>> newChunksToReDirty = new ArrayList<>();

                    // rerender each chunk a second time so we can unhide leaves as they go out of range
                    synchronized (UnitClientEvents.windowPositions) {
                        for (Pair<BlockPos, Integer> pair : chunksToReDirty) {
                            int times = pair.getSecond();
                            if (pair.getFirst().equals(chunkInfo.chunk.getOrigin())) {
                                chunkInfo.chunk.setDirty(true);
                                times -= 1;
                            }
                            if (times > 0)
                                newChunksToReDirty.add(new Pair<>(pair.getFirst(), times));
                        }
                        chunksToReDirty.clear();
                        chunksToReDirty.addAll(newChunksToReDirty);

                        UnitClientEvents.windowPositions.forEach(bp -> {
                            if (chunkCentreBp.distSqr(bp) < 225) {
                                chunkInfo.chunk.setDirty(true);
                                chunksToReDirty.add(new Pair<>(chunkInfo.chunk.getOrigin(), 10));
                            }
                        });
                    }
                }
            }
        }

        if (!isEnabled())
            return;

        ci.cancel();


        // determine which renderChunks are new - enforce frozenChunks on those
        ObjectArrayList<LevelRenderer.RenderChunkInfo> newRenderChunksInFrustum = new ObjectArrayList<>();
        newRenderChunksInFrustum.addAll(renderChunksInFrustum);
        newRenderChunksInFrustum.removeAll(lastRenderChunksInFrustum);

        // load saved blocks into unexplored frozen chunks (don't repeat this for overlapping chunks)
        ArrayList<BlockPos> loadedFcOrigins = new ArrayList<>();
        for (FrozenChunk frozenChunk : frozenChunks.stream().filter(fc -> !fc.hasFakeBlocks).toList()) {
            for (LevelRenderer.RenderChunkInfo newRenderChunk : newRenderChunksInFrustum) {
                if (newRenderChunk.chunk.getOrigin().equals(frozenChunk.origin) &&
                    !isInBrightChunk(frozenChunk.origin) &&
                    !loadedFcOrigins.contains(frozenChunk.origin)) {
                    if (!frozenChunk.unsaved) {
                        //System.out.println("loaded frozen blocks at: " + frozenChunk.origin);
                        frozenChunk.loadBlocks();
                        loadedFcOrigins.add(frozenChunk.origin);
                    }
                }
            }
        }
        // rerun for any faked chunks, only run them if they were not already loaded
        for (FrozenChunk frozenChunk : frozenChunks.stream().filter(fc -> fc.hasFakeBlocks).toList()) {
            for (LevelRenderer.RenderChunkInfo newRenderChunk : newRenderChunksInFrustum) {
                if (newRenderChunk.chunk.getOrigin().equals(frozenChunk.origin) &&
                    !isInBrightChunk(frozenChunk.origin) &&
                    !loadedFcOrigins.contains(frozenChunk.origin)) {
                    if (!frozenChunk.unsaved) {
                        //System.out.println("loaded (faked) frozen blocks at: " + frozenChunk.origin);
                        frozenChunk.loadBlocks();
                    }
                }
            }
        }

        this.minecraft.getProfiler().push("populate_chunks_to_compile");
        RenderRegionCache renderregioncache = new RenderRegionCache();
        BlockPos blockpos = pCamera.getBlockPosition();
        List<ChunkRenderDispatcher.RenderChunk> list = Lists.newArrayList();
        Set<ChunkPos> rerenderChunksToRemove = ConcurrentHashMap.newKeySet();
        Set<ChunkPos> enemyOccupiedChunks = FogOfWarClientEvents.getEnemyOccupiedChunks();

        for(LevelRenderer.RenderChunkInfo chunkInfo : this.renderChunksInFrustum) {

            BlockPos originPos = chunkInfo.chunk.getOrigin();
            ChunkPos chunkPos = new ChunkPos(originPos);

            if (rerenderChunks.contains(chunkPos)) {
                FogOfWarClientEvents.updateChunkLighting(originPos);
                rerenderChunksToRemove.add(chunkPos);
            }
            else if (!isInBrightChunk(originPos)) {
                if (semiFrozenChunks.contains(originPos) ||
                    frozenChunks.stream()
                            .filter(fc -> fc.unsaved)
                            .map(fc -> fc.origin)
                            .toList().contains(originPos))
                    continue;
                else if (OrthoviewClientEvents.isEnabled() || enemyOccupiedChunks.contains(chunkPos))
                    semiFrozenChunks.add(originPos);
            }
            ChunkRenderDispatcher.RenderChunk renderChunk = chunkInfo.chunk;
            ChunkPos chunkpos = new ChunkPos(renderChunk.getOrigin());
            if (renderChunk.isDirty() && this.level.getChunk(chunkpos.x, chunkpos.z).isClientLightReady()) {
                boolean flag = false;
                if (this.minecraft.options.prioritizeChunkUpdates().get() != PrioritizeChunkUpdates.NEARBY) {
                    if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                        flag = renderChunk.isDirtyFromPlayer();
                    }
                } else {
                    BlockPos blockpos1 = renderChunk.getOrigin().offset(8, 8, 8);
                    flag = !net.minecraftforge.common.ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.get() && (blockpos1.distSqr(blockpos) < 768.0D || renderChunk.isDirtyFromPlayer());
                }

                if (flag) {
                    this.minecraft.getProfiler().push("build_near_sync");
                    this.chunkRenderDispatcher.rebuildChunkSync(renderChunk, renderregioncache);
                    renderChunk.setNotDirty();
                    this.minecraft.getProfiler().pop();
                } else {
                    list.add(renderChunk);
                }
            }
        }
        rerenderChunks.removeAll(rerenderChunksToRemove);

        this.minecraft.getProfiler().popPush("upload");
        this.chunkRenderDispatcher.uploadAllPendingUploads();
        this.minecraft.getProfiler().popPush("schedule_async_compile");

        for(ChunkRenderDispatcher.RenderChunk renderChunk1 : list) {
            renderChunk1.rebuildChunkAsync(this.chunkRenderDispatcher, renderregioncache);
            renderChunk1.setNotDirty();
        }
        this.minecraft.getProfiler().pop();

        lastRenderChunksInFrustum.clear();
        lastRenderChunksInFrustum.addAll(renderChunksInFrustum);
    }

    @Shadow @Final private AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

    // always recheck chunks being in frustum - without this normally only checks when the camera moves
    @Inject(
            method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
            at = @At("HEAD")
    )
    private void setupRender(Camera pCamera, Frustum pFrustum, boolean pHasCapturedFrustum, boolean pIsSpectator, CallbackInfo ci) {
        if (!isEnabled())
            return;

        if (!OrthoviewClientEvents.isEnabled())
            return;

        needsFrustumUpdate.set(true);
    }

    // rerun blockDestroyProgress overlays but with range extended to between 32-256 blocks
    @Inject(
            method = "renderLevel",
            at = @At("TAIL")
    )
    private void renderLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime,
                             boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer,
                             LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {

        Vec3 vec3 = pCamera.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();

        ObjectIterator var42 = this.destructionProgress.long2ObjectEntrySet().iterator();

        while (var42.hasNext()) {
            Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry = (Long2ObjectMap.Entry) var42.next();
            BlockPos blockpos2 = BlockPos.of(entry.getLongKey());
            double d3 = (double) blockpos2.getX() - d0;
            double d4 = (double) blockpos2.getY() - d1;
            double d5 = (double) blockpos2.getZ() - d2;
            double distSqr = d3 * d3 + d4 * d4 + d5 * d5;
            if ((distSqr > 1024.0 && distSqr < 65536)) {
                SortedSet<BlockDestructionProgress> sortedset1 = (SortedSet) entry.getValue();
                if (sortedset1 != null && !sortedset1.isEmpty()) {
                    int k1 = (sortedset1.last()).getProgress();
                    pPoseStack.pushPose();
                    pPoseStack.translate((double) blockpos2.getX() - d0, (double) blockpos2.getY() - d1, (double) blockpos2.getZ() - d2);
                    PoseStack.Pose posestack$pose = pPoseStack.last();
                    VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer((RenderType) ModelBakery.DESTROY_TYPES.get(k1)), posestack$pose.pose(), posestack$pose.normal());
                    ModelData modelData = this.level.getModelDataManager().getAt(blockpos2);
                    this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockpos2), blockpos2, this.level, pPoseStack, vertexconsumer1, modelData == null ? ModelData.EMPTY : modelData);
                    pPoseStack.popPose();
                }
            }
        }
    }

    // increase render distance for particles
    @Shadow private ParticleStatus calculateParticleLevel(boolean pDecreased) { return null; }

    @Shadow @Nullable private PostChain entityEffect;

    @Inject(
            method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void addParticleInternal(ParticleOptions pOptions, boolean pForce, boolean pDecreased, double pX, double pY, double pZ,
                                    double pXSpeed, double pYSpeed, double pZSpeed, CallbackInfoReturnable<Particle> cir) {
        if (!OrthoviewClientEvents.isEnabled())
            return;

        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        if (this.minecraft != null && camera.isInitialized() && this.minecraft.particleEngine != null) {
            ParticleStatus particlestatus = this.calculateParticleLevel(pDecreased);
            if (pForce) {
                cir.setReturnValue(this.minecraft.particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed));
            } else if (camera.getPosition().distanceToSqr(pX, pY, pZ) > 4096) {
                cir.setReturnValue(null);
            } else {
                cir.setReturnValue(particlestatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed));
            }
        } else {
            cir.setReturnValue(null);
        }
    }
}
