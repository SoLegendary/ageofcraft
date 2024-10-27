package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerClientEvents;
import com.solegendary.reignofnether.sounds.SoundClientEvents;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.RangedAttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.solegendary.reignofnether.fogofwar.FogOfWarServerboundPacket.setServerFog;

public class FogOfWarClientEvents {
    public static final float BRIGHT = 1.0f;
    public static final float DARK = 0.35f;
    public static final float EXTRA_DARK = 0.10f;

    public static final int CHUNK_VIEW_DIST = 1;
    public static final int CHUNK_FAR_VIEW_DIST = 2;
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int UPDATE_TICKS_MAX = 10;
    private static int updateTicksLeft = UPDATE_TICKS_MAX;

    public static final Set<ChunkPos> brightChunks = ConcurrentHashMap.newKeySet();
    public static final Set<ChunkPos> lastBrightChunks = ConcurrentHashMap.newKeySet();
    public static final Set<ChunkPos> rerenderChunks = ConcurrentHashMap.newKeySet();

    // chunks that will not update while we are looking at them
    public static final Set<BlockPos> semiFrozenChunks = ConcurrentHashMap.newKeySet();

    // chunkInfos that should never be updated, even if the client does a reset or moves the camera out of range
    public static final Set<FrozenChunk> frozenChunks = ConcurrentHashMap.newKeySet();

    public static boolean fogEnableWarningSent = false;

    // if false, disables ALL mixins related to fog of war
    private static boolean enabled = false;

    private static final Set<String> revealedPlayerNames = ConcurrentHashMap.newKeySet();

    public static boolean movedToCapitol = false;

    // mark these renderChunks as dirty the next time they're rendered
    // this is so we chunks that were explored while not in frustum have lighting updated correctly
    public static Set<ChunkPos> chunksToRefresh = new HashSet<>();

    public static ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum = new ObjectArrayList<>();



    public static void revealOrHidePlayer(boolean reveal, String playerName) {
        if (reveal)
            revealedPlayerNames.add(playerName);
        else
            revealedPlayerNames.removeIf(p -> p.equals(playerName));
    }

    public static boolean isPlayerRevealed(String name) {
        if (!PlayerClientEvents.isRTSPlayer)
            return true;

        return revealedPlayerNames.contains(name);
    }

    @SubscribeEvent
    // can't use ScreenEvent.KeyboardKeyPressedEvent as that only happens when a screen is up
    public static void onInput(InputEvent.Key evt) {
        if (evt.getAction() == GLFW.GLFW_PRESS) { // prevent repeated key actions
            if (MC.player == null)
                return;
            if (!MC.player.hasPermissions(4))
                return;

            // resetFogChunks
            if (evt.getKey() == Keybindings.getFnum(8).key && isEnabled()) {
                resetFogChunks();
            }
        }
    }

    // reload chunks like player pressed F3 + A
    public static void resetFogChunks() {
        MC.levelRenderer.allChanged();
        semiFrozenChunks.clear();
    }

    public static void setEnabled(boolean value) {
        if (MC.player == null)
            return;

        if (enabled != value) {
            enabled = value;
            resetFogChunks();

            if (enabled) {
                updateFogChunks();
                for (Building building : BuildingClientEvents.getBuildings())
                    building.freezeChunks(MC.player.getName().getString(), false);
            } else {
                for (FrozenChunk frozenChunk : frozenChunks)
                    frozenChunk.unloadBlocks();
                frozenChunks.clear();
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterClientCommandsEvent evt) {
        evt.getDispatcher().register(Commands.literal("rts-fog").then(Commands.literal("enable")
                .executes((command) -> {
                    if (MC.player == null)
                        return -1;
                    if (!MC.player.hasPermissions(4))
                        return -1;
                    //if (TutorialClientEvents.isEnabled()) {
                    //    MC.player.sendSystemMessage(Component.literal("Fog of war is not available in the tutorial."));
                    //    return -1;
                    //}
                    if (!fogEnableWarningSent) {
                        fogEnableWarningSent = true;
                        MC.player.sendSystemMessage(Component.literal(""));
                        MC.player.sendSystemMessage(Component.literal("[WARNING]").withStyle(Style.EMPTY.withBold(true)));
                        MC.player.sendSystemMessage(Component.literal(
                        "You are about to enable fog of war for all players. This is an experimental feature with several issues:"));
                        MC.player.sendSystemMessage(Component.literal(""));
                        MC.player.sendSystemMessage(Component.literal("- ALL PLAYERS WITH OPTIFINE WILL CRASH"));
                        MC.player.sendSystemMessage(Component.literal("- May cause chunk rendering bugs"));
                        MC.player.sendSystemMessage(Component.literal("- Ups CPU usage (lower chunk render distance to help)"));
                        MC.player.sendSystemMessage(Component.literal(""));
                        MC.player.sendSystemMessage(Component.literal("Use /rts-fog enable again to confirm."));
                        MC.player.sendSystemMessage(Component.literal(""));
                    } else {
                        setServerFog(true);
                    }
                    return 1;
                })));
        evt.getDispatcher().register(Commands.literal("rts-fog").then(Commands.literal("disable")
                .executes((command) -> {
                    if (MC.player == null)
                        return -1;
                    if (!MC.player.hasPermissions(4))
                        return -1;
                    setServerFog(false);
                    return 1;
                })));
    }

    // returns the shade modifier that should be applied at a given position based on the fog of war state there
    public static float getPosBrightness(BlockPos pPos) {
        if (MC.level == null) //!isEnabled() ||
            return BRIGHT;

        if (!MC.level.getWorldBorder().isWithinBounds(pPos))
            return EXTRA_DARK;

        // first check if the ChunkPos is already occupied as this is faster
        for (ChunkPos chunkPos : brightChunks)
            if (new ChunkPos(pPos).equals(chunkPos))
                return BRIGHT;

        if (isEnabled())
            return DARK;
        else
            return BRIGHT;
    }

    public static boolean isBuildingInBrightChunk(Building building) {
        if (!isEnabled())
            return true;

        for (BlockPos bp : BuildingUtils.getUniqueChunkBps(building))
            if (isInBrightChunk(bp))
                return true;

        return false;
    }

    public static boolean isInBrightChunk(BlockPos bp) {
        if (!isEnabled() || MC.level == null)
            return true;

        // first check if the ChunkPos is already occupied as this is faster
        for (ChunkPos chunkPos : brightChunks)
            if (new ChunkPos(bp).equals(chunkPos))
                return true;

        return false;
    }

    public static boolean isInBrightChunk(Entity entity) {
        if (!isEnabled() || MC.level == null)
            return true;

        // first check if the ChunkPos is already occupied as this is faster
        for (ChunkPos chunkPos : brightChunks)
            if (new ChunkPos(entity.getOnPos()).equals(chunkPos))
                return true;

        return entity instanceof RangedAttackerUnit rangedAttackerUnit &&
                rangedAttackerUnit.getFogRevealDuration() > 0;
    }

    @SubscribeEvent
    // hudSelectedEntity and portraitRendererUnit should be assigned in the same event to avoid desyncs
    public static void onRenderLivingEntity(RenderLivingEvent.Pre<? extends LivingEntity, ? extends Model> evt) {
        if (MC.level != null && !MC.level.getWorldBorder().isWithinBounds(evt.getEntity().getOnPos())) {
            evt.setCanceled(true);
            return;
        }
        // don't render entities in non-bright chunks or outside of world border
        if (isInBrightChunk(evt.getEntity()))
            return;

        evt.setCanceled(true);
    }

    // returns all chunks that are occupied by an opponent's building and/or unit
    public static Set<ChunkPos> getEnemyOccupiedChunks() {
        Set<ChunkPos> enemyOccupiedChunks = ConcurrentHashMap.newKeySet();

        for (LivingEntity entity : UnitClientEvents.getAllUnits())
            if (UnitClientEvents.getPlayerToEntityRelationship(entity) != Relationship.OWNED)
                enemyOccupiedChunks.add(new ChunkPos(entity.getOnPos()));

        for (Building building : BuildingClientEvents.getBuildings())
            if (BuildingClientEvents.getPlayerToBuildingRelationship(building) != Relationship.OWNED &&
                    !isPlayerRevealed(building.ownerName) && MC.level != null)
                enemyOccupiedChunks.addAll(building.getRenderChunkOrigins(true)
                        .stream().map(bp -> MC.level.getChunk(bp).getPos()).toList());

        return enemyOccupiedChunks;
    }

    public static void updateFogChunks() {
        brightChunks.clear();
        Set<ChunkPos> viewerChunks = ConcurrentHashMap.newKeySet();
        Set<ChunkPos> farViewerChunks = ConcurrentHashMap.newKeySet();

        // get chunks that have units/buildings that can see
        for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
            if (UnitClientEvents.getPlayerToEntityRelationship(entity) == Relationship.OWNED ||
                    (entity instanceof Unit unit && isPlayerRevealed(unit.getOwnerName()))) {
                if (entity instanceof GhastUnit)
                    farViewerChunks.add(new ChunkPos(entity.getOnPos()));
                else
                    viewerChunks.add(new ChunkPos(entity.getOnPos()));
            }
        }
        for (Building building : BuildingClientEvents.getBuildings()) {
            if (BuildingClientEvents.getPlayerToBuildingRelationship(building) == Relationship.OWNED ||
                    isPlayerRevealed(building.ownerName)) {
                if ((building instanceof GarrisonableBuilding && GarrisonableBuilding.getNumOccupants(building) > 0 && building.isBuilt) ||
                        building.isCapitol)
                    farViewerChunks.add(new ChunkPos(building.centrePos));
                else
                    viewerChunks.add(new ChunkPos(building.centrePos));
            }
        }

        for (ChunkPos chunkPos : viewerChunks)
            for (int x = -CHUNK_VIEW_DIST; x <= CHUNK_VIEW_DIST; x++)
                for (int z = -CHUNK_VIEW_DIST; z <= CHUNK_VIEW_DIST; z++)
                    brightChunks.add(new ChunkPos(chunkPos.x + x, chunkPos.z + z));

        for (ChunkPos chunkPos : farViewerChunks)
            for (int x = -CHUNK_FAR_VIEW_DIST; x <= CHUNK_FAR_VIEW_DIST; x++)
                for (int z = -CHUNK_FAR_VIEW_DIST; z <= CHUNK_FAR_VIEW_DIST; z++)
                    brightChunks.add(new ChunkPos(chunkPos.x + x, chunkPos.z + z));

        Set<ChunkPos> newlyDarkChunks = ConcurrentHashMap.newKeySet();
        newlyDarkChunks.addAll(lastBrightChunks);
        newlyDarkChunks.removeAll(brightChunks);

        for (ChunkPos cpos : newlyDarkChunks) {
            onChunkUnexplore(cpos);
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    rerenderChunks.add(new ChunkPos(cpos.x + x, cpos.z + z));
        }
        Set<ChunkPos> newlyBrightChunks = ConcurrentHashMap.newKeySet();
        newlyBrightChunks.addAll(brightChunks);
        newlyBrightChunks.removeAll(lastBrightChunks);

        for (ChunkPos cpos : newlyBrightChunks)
            onChunkExplore(cpos);

        if (OrthoviewClientEvents.isEnabled())
            semiFrozenChunks.removeIf(bp -> bp.offset(8,8,8)
                    .distSqr(MC.player.getOnPos()) > Math.pow(OrthoviewClientEvents.getZoom() * 3, 2));
        else
            semiFrozenChunks.removeIf(bp -> bp.offset(8,8,8)
                    .distSqr(MC.player.getOnPos()) > Math.pow(MC.levelRenderer.getLastViewDistance() * 8, 2));
        semiFrozenChunks.removeIf(bp -> {
            if (isInBrightChunk(bp)) {
                updateChunkLighting(bp);
                return true;
            }
            return false;
        });

        lastBrightChunks.clear();
        lastBrightChunks.addAll(brightChunks);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent evt) {
        if (!isEnabled() || MC.level == null || MC.player == null || evt.phase != TickEvent.Phase.END)
            return;

        if (updateTicksLeft > 0) {
            updateTicksLeft -= 1;
        } else {
            updateTicksLeft = UPDATE_TICKS_MAX;

            updateFogChunks();
        }
    }

    public static void updateChunkLighting(BlockPos originBp) {
        if (MC.level == null)
            return;

        for (int i = 0; i < 4; i++) {
            BlockPos updatePos = originBp.offset(4*i, 0, 4*i);
            for (int y = MC.level.getMaxBuildHeight(); y > MC.level.getMinBuildHeight(); y -= 1) {
                BlockPos bp = new BlockPos(updatePos.getX(), y, updatePos.getZ());
                BlockState bs = MC.level.getBlockState(bp);
                if (!bs.isAir()) {
                    MC.level.setBlockAndUpdate(bp, Blocks.GLOWSTONE.defaultBlockState());
                    MC.level.setBlockAndUpdate(bp, bs);
                    break;
                }
            }
        }
    }

    // triggered when a chunk goes from dark to bright
    public static void onChunkExplore(ChunkPos cpos) {
        if (MC.level == null)
            return;

        Set<ChunkPos> chunksInFrustum = renderChunksInFrustum.stream()
                .map(rci -> MC.level.getChunk(rci.chunk.getOrigin()).getPos())
                .collect(Collectors.toSet());

        if (!chunksInFrustum.contains(cpos)) {
            System.out.println("explored chunk outside of frustum at: " + cpos);
            chunksToRefresh.add(cpos);
        }

        frozenChunks.removeIf(fc -> fc.removeOnExplore && MC.level.getChunk(fc.origin).getPos().equals(cpos));

        for (FrozenChunk frozenChunk : frozenChunks)
            if (MC.level.getChunk(frozenChunk.origin).getPos().equals(cpos))
                frozenChunk.unloadBlocks();

        for (Building building : BuildingClientEvents.getBuildings()) {
            if (building.isExploredClientside)
                continue;
            for (BlockPos bp : building.getRenderChunkOrigins(false))
                if (bp.getX() == cpos.getWorldPosition().getX() &&
                    bp.getZ() == cpos.getWorldPosition().getZ())
                    building.isExploredClientside = true;
        }
    }

    // triggered when a chunk goes from bright to dark
    public static void onChunkUnexplore(ChunkPos cpos) {
        frozenChunks.removeIf(fc -> fc.removeOnExplore && MC.level.getChunk(fc.origin).getPos().equals(cpos));
        for (FrozenChunk frozenChunk : frozenChunks)
            if (MC.level.getChunk(frozenChunk.origin).getPos().equals(cpos) && MC.level.isLoaded(frozenChunk.origin))
                frozenChunk.saveBlocks(); // only save blocks with faked chunks for NEW frozen chunks
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load evt) {
        BlockPos bp = evt.getChunk().getPos().getWorldPosition();
        // save any unsaved frozenChunks
        for (FrozenChunk fc : frozenChunks) {
            if (evt.getLevel().isClientSide() &&
                    bp.getX() == fc.origin.getX() &&
                    bp.getZ() == fc.origin.getZ()) {
                if (fc.unsaved)
                    fc.saveFakeBlocks();
                if (!isInBrightChunk(bp))
                    fc.loadBlocks();
            }
        }
    }

    public static void setBuildingDestroyedServerside(BlockPos buildingOrigin) {
        for (Building building : BuildingClientEvents.getBuildings())
            if (building.originPos.equals(buildingOrigin))
                building.isDestroyedServerside = true;
    }

    public static void setBuildingBuiltServerside(BlockPos buildingOrigin) {
        for (Building building : BuildingClientEvents.getBuildings())
            if (building.originPos.equals(buildingOrigin))
                building.isBuiltServerside = true;
    }

    // show corners of all frozenChunks
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        /*
        for (FrozenChunk frozenChunk : frozenChunks) {
            BlockPos bp = frozenChunk.origin;
            Vec3 vec3 = new Vec3(bp.getX(), bp.getY(), bp.getZ());
            MyRenderer.drawLine(evt.getPoseStack(), vec3, vec3.add(0,15,0), 1, 1, 1, 1);
            MyRenderer.drawLine(evt.getPoseStack(), vec3, vec3.add(15,0,0), 1, 1, 1, 1);
            MyRenderer.drawLine(evt.getPoseStack(), vec3, vec3.add(0,0,15), 1, 1, 1, 1);
        }
         */
    }

    public static void revealRangedUnit(String playerBeingAttacked, int unitId) {
        if (MC.player != null && MC.player.getName().getString().equals(playerBeingAttacked))
            for (LivingEntity entity : UnitClientEvents.getAllUnits())
                if (entity.getId() == unitId && entity instanceof RangedAttackerUnit unit)
                    unit.setFogRevealDuration(RangedAttackerUnit.FOG_REVEAL_TICKS_MAX);
    }

    public static void unmuteChunks() {
        SoundClientEvents.mutedBps.clear();
    }

    /*
    @SubscribeEvent
    public static void onRenderOverLay(RenderGuiOverlayEvent.Pre evt) {
        MiscUtil.drawDebugStrings(evt.getPoseStack(), MC.font, new String[] {
                "semiFrozenChunks: " + semiFrozenChunks.size(),
        });
    }
     */
}
