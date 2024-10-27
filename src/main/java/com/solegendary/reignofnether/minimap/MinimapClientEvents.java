package com.solegendary.reignofnether.minimap;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerServerboundPacket;
import com.solegendary.reignofnether.time.NightCircleMode;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class MinimapClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    private static int worldRadius = 100; // how many world blocks should be mapped
    private static int mapGuiRadius = 50; // actual size on the screen
    private static int refreshTicks = 0;
    public static final int CORNER_OFFSET = 10;
    public static final int BG_OFFSET = 6;

    private static boolean largeMap = false;
    private static boolean shouldToggleSize = false;

    private static final int UNIT_RADIUS = 3;
    private static final int UNIT_THICKNESS = 1;
    private static final int BUILDING_RADIUS = 7;
    private static final int BUILDING_THICKNESS = 2;

    private static DynamicTexture mapTexture = new DynamicTexture(worldRadius * 2, worldRadius * 2, true);
    private static RenderType mapRenderType = RenderType.textSeeThrough(Minecraft.getInstance()
            .textureManager.register(ReignOfNether.MOD_ID + "_" + "minimap", mapTexture));
    private static int[][] mapColoursTerrain = new int[worldRadius * 2][worldRadius * 2];
    private static int[][] mapColoursOverlays = new int[worldRadius * 2][worldRadius * 2]; // view quad, units, buildings

    private static int terrainPartition = 1;
    private static final int TERRAIN_PARTITIONS_MAX = 10;
    private static int darkTerrainPartition = 1;
    private static final int DARK_TERRAIN_PARTITIONS_MAX = 5; // sub-partitions of terrain_partitions - so there will be 5*10 total
    private static boolean forceUpdateAllPartitions = true;

    private static int xc_world = 0; // world pos x centre, maps to xc
    private static int zc_world = 0; // world pos zcentre, maps to yc
    private static float xl, xc, xr, yt, yc, yb;

    public static final ArrayList<MinimapUnit> minimapUnits = new ArrayList<>();

    private static final float DARK = 0.40f;
    private static final float EXTRA_DARK = 0.10f;

    // objects for tracking serverside Units that don't yet exist on clientside
    private static class MinimapUnit {
        public BlockPos pos;
        public final int id;
        public final String ownerName;

        public MinimapUnit(BlockPos pos, int id, String ownerName) {
            this.pos = pos;
            this.id = id;
            this.ownerName = ownerName;
        }
    }
    public static void removeMinimapUnit(int id) {
        minimapUnits.removeIf(u -> u.id == id);
    }
    public static void syncMinimapUnits(BlockPos pos, int id, String ownerName) {
        for (MinimapUnit unit : minimapUnits) {
            if (unit.id == id) {
                unit.pos = pos;
                return;
            }
        }
        minimapUnits.add(new MinimapUnit(pos, id, ownerName));
    }

    public static void setMapCentre(double x, double z) {
        xc_world = (int) x;
        zc_world = (int) z;
    }

    public static int getMapGuiRadius() {
        return mapGuiRadius;
    }

    private static void toggleMapSize() {
        largeMap = !largeMap;
        if (largeMap) {
            worldRadius = 240;
            mapGuiRadius = 120;
        } else {
            worldRadius = 120;
            mapGuiRadius = 60;
        }
        mapTexture = new DynamicTexture(worldRadius * 2, worldRadius * 2, true);
        mapRenderType = RenderType.textSeeThrough(Minecraft.getInstance()
                .textureManager.register(ReignOfNether.MOD_ID + "_" + "minimap", mapTexture));
        mapColoursTerrain = new int[worldRadius * 2][worldRadius * 2];
        mapColoursOverlays = new int[worldRadius * 2][worldRadius * 2];
        forceUpdateAllPartitions = true;
    }

    public static Button getToggleSizeButton() {
        return new Button(
            largeMap ? "Close" : "Open large map",
            14,
            largeMap ? new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/barrier.png") :
                        new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/map.png"),
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame.png"),
            Keybindings.keyM,
            () -> false,
            () -> !TutorialClientEvents.isAtOrPastStage(TutorialStage.MINIMAP_CLICK),
            () -> true,
            () -> shouldToggleSize = true,
            () -> { },
            List.of(FormattedCharSequence.forward(largeMap ? "Close" : "Open large map", Style.EMPTY))
        );
    }

    public static void updateMapTexture()
    {
        if (MC.player == null)
            return;

        // if camera is off the map, start panning the centre of the map
        double xCam = MC.player.getX();
        double zCam = MC.player.getZ();
        double xDiff1 = xCam - (xc_world + worldRadius);
        if (xDiff1 > 0)
            xc_world += xDiff1;
        double zDiff1 = zCam - (zc_world + worldRadius);
        if (zDiff1 > 0)
            zc_world += zDiff1;
        double xDiff2 = xCam - (xc_world - worldRadius);
        if (xDiff2 < 0)
            xc_world += xDiff2;
        double zDiff2 = zCam - (zc_world - worldRadius);
        if (zDiff2 < 0)
            zc_world += zDiff2;

        NativeImage pixels = mapTexture.getPixels();
        if (pixels != null)
        {
            int i = 0;
            for (int z = 0; z < worldRadius *2; z ++) {
                for (int x = 0; x < worldRadius *2; x ++) {
                    if (mapColoursOverlays[x][z] != 0) {
                        pixels.setPixelRGBA(x, z, mapColoursOverlays[x][z]);
                    } else {
                        pixels.setPixelRGBA(x, z, mapColoursTerrain[x][z]);
                    }
                    i += 1;
                }
            }
            mapTexture.upload();
        }
    }

    private static void updateMapTerrain(int partition, int darkPartition)
    {
        if (MC.level == null || MC.player == null)
            return;

        int zMin = zc_world - worldRadius;
        int zMax = zc_world + worldRadius;
        int xMin = xc_world - worldRadius;
        int xMax = xc_world + worldRadius;

        Set<ChunkPos> brightChunks = FogOfWarClientEvents.brightChunks;

        // draw terrain blocks
        for (int z = zMin; z < zMax; z++)
        {
            boolean skipDarkPartition = false;

            // eg. if z ranges from -500 to 300, that's a range of 800
            // if we have 10 partitions, partition 3 should range from:
            // zPartMin = (800 / 10) * 2 = 160
            // xPartMax = (800 / 10) * 3 = 240
            // so only update this pixel if the z row is between 160 and 240
            if (!forceUpdateAllPartitions) {
                int zMaxN = zMax - zMin; // zMax normalised to 0 -> (worldRadius * 2)
                int zPartMin = (zMaxN / TERRAIN_PARTITIONS_MAX) * (partition - 1);
                int zPartMax = (zMaxN / TERRAIN_PARTITIONS_MAX) * partition;
                int zN = z - zMin;
                if (zN < zPartMin || zN >= zPartMax)
                    continue;

                int zPartMind = (zMaxN / DARK_TERRAIN_PARTITIONS_MAX) * (darkPartition - 1);
                int zPartMaxd = (zMaxN / DARK_TERRAIN_PARTITIONS_MAX) * darkPartition;
                if (zN < zPartMind || zN >= zPartMaxd)
                    skipDarkPartition = true;
            }

            for (int x = xMin; x < xMax; x++) {

                boolean isBright = false;
                if (FogOfWarClientEvents.isEnabled()) {
                    // apply a much slower update rate to dark chunks
                    for (ChunkPos chunkPos : brightChunks) {
                        if (x >= chunkPos.getMinBlockX() && x <= chunkPos.getMaxBlockX() &&
                                z >= chunkPos.getMinBlockZ() && z <= chunkPos.getMaxBlockZ()) {
                            isBright = true;
                            break;
                        }
                    }
                    if (!isBright && skipDarkPartition)
                        continue;
                }

                int y = MC.level.getChunkAt(new BlockPos(x,0,z)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                BlockState bs;
                do {
                    bs = MC.level.getBlockState(new BlockPos(x, y, z));
                    if (bs.getBlock() instanceof SnowLayerBlock) {
                        int layers = bs.getValue(SnowLayerBlock.LAYERS);
                        y += (int) (layers * (1.0F / 8.0F));
                        break;
                    }
                    if (!bs.getMaterial().isSolid() && !bs.getMaterial().isLiquid() && y > 0)
                        y -= 1;
                    else
                        break;
                } while (true);

                int yNorth = MC.level.getChunkAt(new BlockPos(x,0,z-1)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z-1);
                BlockState bsNorth;
                do {
                    bsNorth = MC.level.getBlockState(new BlockPos(x, yNorth, z-1));
                    if (bsNorth.getBlock() instanceof SnowLayerBlock) {
                        int layersNorth = bsNorth.getValue(SnowLayerBlock.LAYERS);
                        yNorth += (int) (layersNorth * (1.0F / 8.0F));
                        break;
                    }
                    if (!bsNorth.getMaterial().isSolid() && !bsNorth.getMaterial().isLiquid() && yNorth > 0)
                        yNorth -= 1;
                    else
                        break;
                } while (true);

                Material mat = MC.level.getBlockState(new BlockPos(x,yNorth,z-1)).getMaterial();
                int rgb = mat.getColor().col;
                if (bs.getBlock() instanceof SnowLayerBlock) {
                    rgb = 0xFFFFFF;
                }

                // shade blocks to give elevation effects, excluding liquids and nonblocking blocks (eg. grass, flowers)
                if (!mat.isLiquid()) {
                    if (yNorth > y)
                        rgb = MiscUtil.shadeHexRGB(rgb, 0.82F);
                    else if (yNorth < y) {
                        rgb = MiscUtil.shadeHexRGB(rgb, 1.16F);
                    }
                }
                else { // shade liquid based on depth
                    int depth = 0;
                    int depthMax = 20;
                    Material matBelow;
                    do {
                        depth += 1;
                        matBelow = MC.level.getBlockState(new BlockPos(x,y-depth,z)).getMaterial();
                    }
                    while(matBelow.isLiquid() && depth < depthMax);

                    // only reduce shade every nth step to have the map look sharper
                    depth = (int) (5*(Math.ceil(Math.abs(depth/5))));

                    rgb = MiscUtil.shadeHexRGB(rgb, 1.2F - (0.025F * depth));
                }

                // normalise xz's to colour array ranges
                int x0 = x - xc_world + worldRadius;
                int z0 = z - zc_world + worldRadius;

                BlockPos pos = new BlockPos(x,0,z);
                if (!MC.level.getWorldBorder().isWithinBounds(pos))
                    rgb = MiscUtil.shadeHexRGB(rgb, EXTRA_DARK);
                else if (!FogOfWarClientEvents.isInBrightChunk(pos))
                    rgb = MiscUtil.shadeHexRGB(rgb, DARK);


                // append 0xFF to include 100% alpha (<< 4 shifts by 1 hex digit)
                mapColoursTerrain[x0][z0] = MiscUtil.reverseHexRGB(rgb) | (0xFF << 24);
            }
        }
        forceUpdateAllPartitions = false;
    }

    private static void updateMapViewQuad() {
        if (MC.level == null || MC.player == null)
            return;

        // get world position of corners of the screen
        Vector3d[] corners = new Vector3d[]{
                MiscUtil.screenPosToWorldPos(MC, 0, 0),
                MiscUtil.screenPosToWorldPos(MC, 0, MC.getWindow().getGuiScaledHeight()),
                MiscUtil.screenPosToWorldPos(MC, MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledHeight()),
                MiscUtil.screenPosToWorldPos(MC, MC.getWindow().getGuiScaledWidth(), 0)
        };
        // adjust corners according to camera angle
        Vector3d lookVector = MiscUtil.getPlayerLookVector(MC);
        corners[0] = MyMath.addVector3d(corners[0], lookVector, 90 - OrthoviewClientEvents.getCamRotY());
        corners[1] = MyMath.addVector3d(corners[1], lookVector, 75 - OrthoviewClientEvents.getCamRotY());
        corners[2] = MyMath.addVector3d(corners[2], lookVector, 75 - OrthoviewClientEvents.getCamRotY());
        corners[3] = MyMath.addVector3d(corners[3], lookVector, 90 - OrthoviewClientEvents.getCamRotY());

        int zMin = zc_world - worldRadius;
        int zMax = zc_world + worldRadius;
        int xMin = xc_world - worldRadius;
        int xMax = xc_world + worldRadius;

        // draw terrain blocks
        for (int z = zMin; z < zMax; z++) {
            xLoop:
            for (int x = xMin; x < xMax; x++) {

                // draw view quad
                for (int i = 0; i < corners.length; i++) {
                    int j = i + 1;
                    if (j >= corners.length) j = 0;

                    if (MyMath.isPointOnLine(
                            new Vec2((float) corners[i].x, (float) corners[i].z),
                            new Vec2((float) corners[j].x, (float) corners[j].z),
                            new Vec2(x, z),
                            OrthoviewClientEvents.getZoom() * 2 // larger = thicker line
                    )) {
                        int x0 = x - xc_world + worldRadius;
                        int z0 = z - zc_world + worldRadius;
                        mapColoursOverlays[x0][z0] = 0xFFFFFFFF;
                        continue xLoop;
                    }
                }
            }
        }
    }

    private static void updateNightCircles() {

        // get list of night source centre:range pairs
        ArrayList<Pair<BlockPos, Integer>> nightSources = new ArrayList<>();

        for (Pair<BlockPos, Integer> ns : TimeClientEvents.nightSourceOrigins) {

            int xc = ns.getFirst().getX() + (BUILDING_RADIUS / 2);
            int zc = ns.getFirst().getZ() + (BUILDING_RADIUS / 2);
            int xN = xc - xc_world + (mapGuiRadius * 2);
            int zN = zc - zc_world + (mapGuiRadius * 2);

            nightSources.add(new Pair<>(new BlockPos(xN, 0, zN), ns.getSecond()));
        }

        for (Pair<BlockPos, Integer> ns : nightSources) {
            Set<BlockPos> nightCircleBps;
            if (TimeClientEvents.nightCircleMode == NightCircleMode.NO_OVERLAPS)
                nightCircleBps = MiscUtil.CircleUtil.getCircleWithCulledOverlaps(ns.getFirst(), ns.getSecond(), nightSources);
            else
                nightCircleBps = MiscUtil.CircleUtil.getCircle(ns.getFirst(), ns.getSecond());

            ArrayList<BlockPos> nightCircleBpsThick = new ArrayList<>();
            // raise thickness
            for (BlockPos bp : nightCircleBps) {
                nightCircleBpsThick.add(bp);
                nightCircleBpsThick.add(bp.offset(-1,0,0));
                nightCircleBpsThick.add(bp.offset(0,0,-1));
            }
            for (BlockPos bp : nightCircleBpsThick) {
                if (bp.getX() > 0 && bp.getX() < mapColoursOverlays.length &&
                    bp.getZ() > 0 && bp.getZ() < mapColoursOverlays[0].length)
                    mapColoursOverlays[bp.getX()][bp.getZ()] = MiscUtil.reverseHexRGB(0x0) | (0xFF << 24);
            }
        }
    }

    private static void updateMapUnitsAndBuildings() {
        // draw buildings
        for (Building building : BuildingClientEvents.getBuildings()) {

            if (!building.isExploredClientside || building instanceof AbstractBridge)
                continue;

            int xc = building.originPos.getX() + (BUILDING_RADIUS / 2);
            int zc = building.originPos.getZ() + (BUILDING_RADIUS / 2);

            for (int x = xc - BUILDING_RADIUS; x < xc + BUILDING_RADIUS; x++) {
                for (int z = zc - BUILDING_RADIUS; z < zc + BUILDING_RADIUS; z++) {
                    if (isWorldXZinsideMap(x,z)) {
                        int x0 = x - xc + BUILDING_RADIUS;
                        int z0 = z - zc + BUILDING_RADIUS;
                        int rgb = 0x000000;

                        // if pixel is on the edge of the square keep it coloured black
                        if (!(x0 < BUILDING_THICKNESS || x0 >= (BUILDING_RADIUS * 2) - BUILDING_THICKNESS ||
                                z0 < BUILDING_THICKNESS || z0 >= (BUILDING_RADIUS * 2) - BUILDING_THICKNESS)) {

                            if (FogOfWarClientEvents.isBuildingInBrightChunk(building)) {
                                switch (BuildingClientEvents.getPlayerToBuildingRelationship(building)) {
                                    case OWNED -> rgb = 0x00FF00;
                                    case FRIENDLY -> rgb = 0x0000FF;
                                    case HOSTILE -> rgb = 0xFF0000;
                                    case NEUTRAL -> rgb = 0xFFFF00;
                                }
                            } else {
                                switch (BuildingClientEvents.getPlayerToBuildingRelationship(building)) {
                                    case OWNED -> rgb = 0x008800;
                                    case FRIENDLY -> rgb = 0x000088;
                                    case HOSTILE -> rgb = 0x880000;
                                    case NEUTRAL -> rgb = 0x888800;
                                }
                            }
                        }
                        int xN = x - xc_world + (mapGuiRadius * 2);
                        int zN = z - zc_world + (mapGuiRadius * 2);

                        mapColoursOverlays[xN][zN] = MiscUtil.reverseHexRGB(rgb) | (0xFF << 24);
                    }
                }
            }
        }

        // draw units
        for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
            if (!FogOfWarClientEvents.isInBrightChunk(entity))
                continue;
            drawUnitOnMap(entity.getOnPos().getX(),
                    entity.getOnPos().getZ(),
                    UnitClientEvents.getPlayerToEntityRelationship(entity)
            );
        }
        for (MinimapUnit minimapUnit : minimapUnits) {
            if (!FogOfWarClientEvents.isInBrightChunk(minimapUnit.pos) || MC.player == null)
                continue;

            Relationship relationship = Relationship.HOSTILE;
            if (MC.player.getName().getString().equals(minimapUnit.ownerName))
                relationship = Relationship.OWNED;
            else if (minimapUnit.ownerName.isBlank())
                relationship = Relationship.NEUTRAL;

            drawUnitOnMap(minimapUnit.pos.getX(),
                    minimapUnit.pos.getZ(),
                    relationship
            );
        }
    }

    private static void drawUnitOnMap(int xc, int zc, Relationship relationship) {
        for (int x = xc - UNIT_RADIUS; x < xc + UNIT_RADIUS; x++) {
            for (int z = zc - UNIT_RADIUS; z < zc + UNIT_RADIUS; z++) {
                if (isWorldXZinsideMap(x,z)) {
                    int x0 = x - xc + UNIT_RADIUS;
                    int z0 = z - zc + UNIT_RADIUS;
                    int rgb = 0x000000;

                    // if pixel is on the edge of the square keep it coloured black
                    if (!(x0 < UNIT_THICKNESS || x0 >= (UNIT_RADIUS * 2) - UNIT_THICKNESS ||
                            z0 < UNIT_THICKNESS || z0 >= (UNIT_RADIUS * 2) - UNIT_THICKNESS)) {
                        switch (relationship) {
                            case OWNED -> rgb = 0x00FF00;
                            case FRIENDLY -> rgb = 0x0000FF;
                            case HOSTILE -> rgb = 0xFF0000;
                            case NEUTRAL -> rgb = 0xFFFF00;
                        }
                    }
                    int xN = x - xc_world + (mapGuiRadius * 2);
                    int zN = z - zc_world + (mapGuiRadius * 2);

                    mapColoursOverlays[xN][zN] = MiscUtil.reverseHexRGB(rgb) | (0xFF << 24);
                }
            }
        }
    }

    // checks whether a given X Z in the world is part of our map
    public static boolean isWorldXZinsideMap(int x, int z) {
        return x >= xc_world - worldRadius && x < xc_world + worldRadius &&
               z >= zc_world - worldRadius && z < zc_world + worldRadius;
    }

    private static void renderMap(PoseStack stack)
    {
        Matrix4f matrix4f = stack.last().pose();

        // place vertices in a diamond shape - left, centre, right, top, centre, bottom
        // map vertex coordinates (left, centre, right, top, centre, bottom)
        xl = MC.getWindow().getGuiScaledWidth() - (mapGuiRadius * 2) - CORNER_OFFSET;
        xc = MC.getWindow().getGuiScaledWidth() - mapGuiRadius - CORNER_OFFSET;
        xr = MC.getWindow().getGuiScaledWidth() - CORNER_OFFSET;
        yt = MC.getWindow().getGuiScaledHeight() - (mapGuiRadius * 2) - CORNER_OFFSET;
        yc = MC.getWindow().getGuiScaledHeight() - mapGuiRadius - CORNER_OFFSET;
        yb = MC.getWindow().getGuiScaledHeight() - CORNER_OFFSET;

        // background vertex coords need to be slightly larger
        float xl_bg = xl - BG_OFFSET;
        float xc_bg = xc;
        float xr_bg = xr + BG_OFFSET;
        float yt_bg = yt - BG_OFFSET;
        float yc_bg = yc;
        float yb_bg = yb + BG_OFFSET;

        // render map background first
        ResourceLocation iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/map_background.png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        // code taken from GuiComponent.blit()
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, xc_bg, yb_bg, 0.0F).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, xr_bg, yc_bg, 0.0F).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, xc_bg, yt_bg, 0.0F).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, xl_bg, yc_bg, 0.0F).uv(0.0F, 0.0F).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        // render map itself
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer consumer = buffer.getBuffer(mapRenderType);
        consumer.vertex(matrix4f, xc, yb, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(255).endVertex();
        consumer.vertex(matrix4f, xr, yc, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(255).endVertex();
        consumer.vertex(matrix4f, xc, yt, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(255).endVertex();
        consumer.vertex(matrix4f, xl, yc, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(255).endVertex();

        buffer.endBatch();
    }

    // https://stackoverflow.com/questions/27022064/detect-click-in-a-diamond
    public static boolean isPointInsideMinimap(double x, double y) {
        double dx = Math.abs(x - xc);
        double dy = Math.abs(y - yc);
        double d = dx / (mapGuiRadius * 2) + dy / (mapGuiRadius * 2);
        return d <= 0.5;
    }

    // given an x and y on the screen that the player clicked, return the world position of that spot
    private static BlockPos getWorldPosOnMinimap(float x, float y, boolean offsetForCamera) {
        if (!isPointInsideMinimap(x,y) || CursorClientEvents.isBoxSelecting() || MC.level == null)
            return null;

        float pixelsToBlocks = (float) worldRadius / (float) mapGuiRadius;

        // offset y up so that user clicks the centre of the view quad instead of bottom border
        if (offsetForCamera)
            y += OrthoviewClientEvents.getZoom() * 0.5F / pixelsToBlocks;

        Vec2 clicked = MyMath.rotateCoords(x - xc, y - yc,45);

        double xWorld = xc_world + clicked.x * pixelsToBlocks * Math.sqrt(2);
        double zWorld = zc_world + clicked.y * pixelsToBlocks * Math.sqrt(2);
        double yWorld = MiscUtil.getHighestNonAirBlock(MC.level, new BlockPos(xWorld, 0, zWorld)).getY();

        return new BlockPos(xWorld, yWorld, zWorld);
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre evt) {
        // when clicking on map move player there
        if (OrthoviewClientEvents.isEnabled() &&
            evt.getMouseButton() == GLFW.GLFW_MOUSE_BUTTON_1 &&
            !Keybindings.shiftMod.isDown() &&
            !OrthoviewClientEvents.isCameraLocked()) {
            BlockPos moveTo = getWorldPosOnMinimap((float) evt.getMouseX(), (float) evt.getMouseY(), true);
            if (MC.player != null && moveTo != null) {
                PlayerServerboundPacket.teleportPlayer((double) moveTo.getX(), MC.player.getY(), (double) moveTo.getZ());
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre evt) {
        if (!OrthoviewClientEvents.isEnabled() || OrthoviewClientEvents.isCameraLocked())
            return;

        // when clicking on map move player there
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
            BlockPos moveTo = getWorldPosOnMinimap((float) evt.getMouseX(), (float) evt.getMouseY(), true);
            if (MC.player != null && moveTo != null) {
                if (Keybindings.shiftMod.isDown()) {
                    setMapCentre(moveTo.getX(), moveTo.getZ());
                    forceUpdateAllPartitions = true;
                    TutorialClientEvents.clickedMinimap = true;
                    PlayerServerboundPacket.teleportPlayer((double) moveTo.getX(), MC.player.getY(), (double) moveTo.getZ());
                } else {
                    TutorialClientEvents.clickedMinimap = true;
                    PlayerServerboundPacket.teleportPlayer((double) moveTo.getX(), MC.player.getY(), (double) moveTo.getZ());
                }
            }
        }
        else if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
            BlockPos moveTo = getWorldPosOnMinimap((float) evt.getMouseX(), (float) evt.getMouseY(), false);
            if (UnitClientEvents.getSelectedUnits().size() > 0 && moveTo != null) {
                UnitClientEvents.sendUnitCommandManual(
                    UnitAction.MOVE, -1,
                    UnitClientEvents.getSelectedUnits().stream().mapToInt(Entity::getId).toArray(),
                    moveTo
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post evt) {
        if (!OrthoviewClientEvents.isEnabled() || MC.isPaused() ||
            !TutorialClientEvents.isAtOrPastStage(TutorialStage.MINIMAP_CLICK))
            return;

        // toggle here to ensure it doesn't happen in the middle of the updates
        if (shouldToggleSize) {
            shouldToggleSize = false;
            toggleMapSize();
        }

        refreshTicks -= 1;
        if (refreshTicks <= 0) {
            updateMapTerrain(terrainPartition, darkTerrainPartition);
            mapColoursOverlays = new int[worldRadius * 2][worldRadius * 2];
            if (TimeClientEvents.nightCircleMode != NightCircleMode.OFF)
                updateNightCircles();
            updateMapUnitsAndBuildings();
            updateMapViewQuad();

            // as the map area increases, decrease refresh rate to maintain FPS
            refreshTicks = (worldRadius * 2) / TERRAIN_PARTITIONS_MAX;
            terrainPartition += 1;
            if (terrainPartition > TERRAIN_PARTITIONS_MAX) {
                terrainPartition = 1;

                darkTerrainPartition += 1;
                if (darkTerrainPartition > DARK_TERRAIN_PARTITIONS_MAX)
                    darkTerrainPartition = 1;
            }

            updateMapTexture();
        }

        renderMap(evt.getPoseStack());

        //MiscUtil.drawDebugStrings(evt.getMatrixStack(), MC.font, new String[] {
        //});
    }
}