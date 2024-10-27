package com.solegendary.reignofnether.cursor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.fogofwar.FrozenChunkServerboundPacket;
import com.solegendary.reignofnether.guiscreen.TopdownGui;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.util.Mth.*;
import static net.minecraft.world.level.BlockGetter.traverseBlocks;


import java.util.ArrayList;
import java.util.List;

/**
 * Handler that implements and manages screen-to-world translations of the cursor and block/entity selection
 */
public class CursorClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();

    private static boolean leftClickDown = false;
    private static boolean rightClickDown = false;

    // pos of block moused over or inside a box select
    // currently blocks are not fully selectable
    private static BlockPos preselectedBlockPos = new BlockPos(0,0,0);
    // pos of cursor exactly on: first non-air block, last frame, near to screen, far from screen
    private static Vector3d cursorWorldPos = new Vector3d(0,0,0);
    private static Vector3d cursorWorldPosLast = new Vector3d(0,0,0);
    // pos of cursor on screen for box selections
    private static Vec2 cursorLeftClickDownPos = new Vec2(-1,-1);
    private static Vec2 cursorLeftClickDragPos = new Vec2(-1,-1);
    // attack that is performed on the next left click
    private static UnitAction leftClickAction = null;

    public static Vector3d getCursorWorldPos() {
        return cursorWorldPos;
    }
    public static BlockPos getPreselectedBlockPos() {
        return preselectedBlockPos;
    }
    public static UnitAction getLeftClickAction() {
        return leftClickAction;
    }
    public static void setLeftClickAction(UnitAction actionName) {
        if (actionName != null &&
            List.of(UnitAction.STARTRTS_VILLAGERS,
                UnitAction.STARTRTS_MONSTERS,
                UnitAction.STARTRTS_PIGLINS).contains(actionName))
            leftClickAction = actionName;
        else if (UnitClientEvents.getSelectedUnits().size() > 0 ||
            BuildingClientEvents.getSelectedBuildings().size() > 0)
            leftClickAction = actionName;
        else if (actionName == null)
            leftClickAction = null;
    }

    private static final ResourceLocation TEXTURE_CURSOR = new ResourceLocation("reignofnether", "textures/cursors/customcursor.png");
    private static final ResourceLocation TEXTURE_HAND = new ResourceLocation("reignofnether", "textures/cursors/customcursor_hand.png");
    private static final ResourceLocation TEXTURE_HAND_GRAB = new ResourceLocation("reignofnether", "textures/cursors/customcursor_hand_grab.png");
    private static final ResourceLocation TEXTURE_SWORD = new ResourceLocation("reignofnether", "textures/cursors/customcursor_sword.png");
    private static final ResourceLocation TEXTURE_CROSS = new ResourceLocation("reignofnether", "textures/cursors/customcursor_cross.png");
    private static final ResourceLocation TEXTURE_SHOVEL = new ResourceLocation("reignofnether", "textures/cursors/customcursor_shovel.png");

    @SubscribeEvent
    public static void onDrawScreen(ScreenEvent.Render evt) {
        long window = MC.getWindow().getWindow();

        if (!OrthoviewClientEvents.isEnabled() || !(evt.getScreen() instanceof TopdownGui)) {
            if (GLFW.glfwRawMouseMotionSupported()) // raw mouse increases sensitivity massively for some reason
                GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            return;
        }
        if (MC.player == null || MC.level == null) return;

        // ************************************
        // Manage cursor icons based on actions
        // ************************************

        // hides default cursor and locks it to the window to allow edge panning
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        // raw mouse increases sensitivity massively for some reason
        GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);

        // blitting like this will cause it to be rendered 1 frame behind realtime (this hopefully shouldn't be noticeable...)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // draw at edge of screen even if mouse is off it
        int cursorDrawX = Math.min(evt.getMouseX(), MC.getWindow().getGuiScaledWidth() - 5);
        int cursorDrawY = Math.min(evt.getMouseY(), MC.getWindow().getGuiScaledHeight() - 5);
        cursorDrawX = Math.max(0,cursorDrawX);
        cursorDrawY = Math.max(0,cursorDrawY);

        if (Keybindings.altMod.isDown() && (leftClickDown || rightClickDown))
            RenderSystem.setShaderTexture(0, TEXTURE_HAND_GRAB);
        else if (Keybindings.altMod.isDown())
            RenderSystem.setShaderTexture(0, TEXTURE_HAND);
        else if (leftClickAction != null && leftClickAction.equals(UnitAction.ATTACK))
            RenderSystem.setShaderTexture(0, TEXTURE_SWORD);
        else if (leftClickAction != null && leftClickAction.equals(UnitAction.BUILD_REPAIR))
            RenderSystem.setShaderTexture(0, TEXTURE_SHOVEL);
        else if (leftClickAction != null) {
            RenderSystem.setShaderTexture(0, TEXTURE_CROSS);
            cursorDrawX -= 8;
            cursorDrawY -= 8;
        }
        else
            RenderSystem.setShaderTexture(0, TEXTURE_CURSOR);

        GuiComponent.blit(evt.getPoseStack(),
                cursorDrawX, cursorDrawY,
                16,
                16, 16,
                16, 16,
                16,16
        );

        // ***********************************************
        // Convert cursor on-screen 2d pos to world 3d pos
        // ***********************************************
        cursorWorldPosLast = new Vector3d(
                cursorWorldPos.x,
                cursorWorldPos.y,
                cursorWorldPos.z
        );
        cursorWorldPos = MiscUtil.screenPosToWorldPos(MC, evt.getMouseX(), evt.getMouseY());

        // calc near and far cursorWorldPos to get a cursor line vector
        Vector3d lookVector = MiscUtil.getPlayerLookVector(MC);
        Vector3d cursorWorldPosNear = MyMath.addVector3d(cursorWorldPos, lookVector, -200);
        Vector3d cursorWorldPosFar = MyMath.addVector3d(cursorWorldPos, lookVector, 200);

        // only spend time doing refining calcs for if we actually moved the cursor (or if this is the first time)
        if (cursorWorldPos.x != cursorWorldPosLast.x || cursorWorldPos.y != cursorWorldPosLast.y || cursorWorldPos.z != cursorWorldPosLast.z) {

            Vec3 hitPos = getRefinedCursorWorldPos(cursorWorldPosNear, cursorWorldPosFar);
            cursorWorldPos = new Vector3d(hitPos.x, hitPos.y, hitPos.z);
            preselectedBlockPos = new BlockPos(hitPos);

            boolean usingPosAbove = false;

            // if we clipped a non-solid block (eg. tall grass) search adjacent blocks for a next-best match
            if (!MC.level.getBlockState(preselectedBlockPos).getMaterial().isSolidBlocking()) {
                preselectedBlockPos = getRefinedBlockPos(preselectedBlockPos, cursorWorldPosNear);
                // disallow selecting a block just below a fluid block
                if (MC.level.getBlockState(preselectedBlockPos.above()).getMaterial().isLiquid()) {
                    preselectedBlockPos = preselectedBlockPos.above();
                    usingPosAbove = true;
                }
            }
            if (!usingPosAbove &&
                !BuildingUtils.isPosInsideAnyBuilding(true, preselectedBlockPos) &&
                BuildingUtils.isPosInsideAnyBuilding(true, preselectedBlockPos.above()))
                preselectedBlockPos = preselectedBlockPos.above();
        }

        // ****************************************
        // Find entity moused over and/or selected
        // ****************************************
        List<LivingEntity> nearbyEntities = MiscUtil.getEntitiesWithinRange(cursorWorldPos, 30, LivingEntity.class, MC.level);

        UnitClientEvents.clearPreselectedUnits();

        for (LivingEntity entity : nearbyEntities) {
            // don't let the player select themselves
            if (entity.getId() == MC.player.getId())
                continue;

            // inflate by set amount to improve click accuracy
            AABB entityaabb = entity.getBoundingBox().inflate(0.1);

            if (MyMath.rayIntersectsAABBCustom(cursorWorldPosNear, MiscUtil.getPlayerLookVector(MC), entityaabb)) {
                UnitClientEvents.addPreselectedUnit(entity);
                if (UnitClientEvents.getPreselectedUnits().size() > 0)
                    break; // only allow one moused-over unit at a time
            }
        }

        // *******************************
        // Calculate box-preselected units
        // *******************************
        // weird bug when downPos == dragPos makes random entities get selected by this algorithm
        float dist = cursorLeftClickDownPos.distanceToSqr(cursorLeftClickDragPos);

        if (leftClickDown && dist > 0 && !Keybindings.altMod.isDown()) {

            // can't use AABB here as it's always axis-aligned (ie. no camera-rotation)
            // instead, improvise our own quad
            // https://math.stackexchange.com/questions/1472049/check-if-a-point-is-inside-a-rectangular-shaped-area-3d

            ArrayList<Vec3> uvwp = MyMath.prepIsPointInsideRect3d(MC,
                    (int) cursorLeftClickDownPos.x, (int) cursorLeftClickDownPos.y, // top left
                    (int) cursorLeftClickDownPos.x, (int) cursorLeftClickDragPos.y, // bottom left
                    (int) cursorLeftClickDragPos.x, (int) cursorLeftClickDragPos.y // bottom right
            );
            for (LivingEntity entity : MiscUtil.getEntitiesWithinRange(cursorWorldPos, 100, LivingEntity.class, MC.level)) {
                if (MyMath.isPointInsideRect3d(uvwp, entity.getBoundingBox().getCenter()) &&
                    entity.getId() != MC.player.getId() &&
                    !UnitClientEvents.getPreselectedUnits().contains(entity))
                    UnitClientEvents.addPreselectedUnit(entity);
            }
        }
    }

    public static boolean isBoxSelecting() {
        return cursorLeftClickDownPos.x >= 0 &&
                cursorLeftClickDownPos.y >= 0 &&
                cursorLeftClickDragPos.x >= 0 &&
                cursorLeftClickDragPos.y >= 0;
    }

    // draw box selection rectangle
    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post evt) {

        if (leftClickDown && !Keybindings.altMod.isDown()) {
            GuiComponent.fill(evt.getPoseStack(), // x1,y1, x2,y2,
                    Math.round(cursorLeftClickDownPos.x),
                    Math.round(cursorLeftClickDownPos.y),
                    Math.round(cursorLeftClickDragPos.x),
                    Math.round(cursorLeftClickDragPos.y),
                    0x0341e868); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Post evt) {

        if (!OrthoviewClientEvents.isEnabled() ||
            MinimapClientEvents.isPointInsideMinimap(evt.getMouseX(), evt.getMouseY()) ||
            HudClientEvents.isMouseOverAnyButtonOrHud())
            return;

        // select a moused over entity by left clicking it
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
            cursorLeftClickDownPos = new Vec2(floor(evt.getMouseX()), floor(evt.getMouseY()));
            cursorLeftClickDragPos = new Vec2(floor(evt.getMouseX()), floor(evt.getMouseY()));
            leftClickDown = true;
        }
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
            rightClickDown = true;
        }
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre evt) {
        if (!OrthoviewClientEvents.isEnabled() ||
            (cursorLeftClickDownPos.x < 0 && cursorLeftClickDownPos.y < 0))
            return;

        cursorLeftClickDragPos = new Vec2(floor(evt.getMouseX()), floor(evt.getMouseY()));
    }

    @SubscribeEvent
    public static void onMouseRelease(ScreenEvent.MouseButtonReleased.Post evt) {
        if (!OrthoviewClientEvents.isEnabled()) return;

        // select a moused over entity by left clicking it
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
            leftClickDown = false;

            // enact box selection, excluding non-unit mobs
            // for single-click selection, see UnitClientEvents
            // except if attack-moving or no owned units are preselected (to prevent deselection)
            ArrayList<LivingEntity> preselectedUnit = UnitClientEvents.getPreselectedUnits();
            if (preselectedUnit.size() > 0 && !cursorLeftClickDownPos.equals(cursorLeftClickDragPos)) {

                // only act if there is at least 1 owned entity so we don't deselect things by box selecting only non-owned entities
                int ownedEntities = 0;
                for (LivingEntity unit : preselectedUnit)
                    if (UnitClientEvents.getPlayerToEntityRelationship(unit) == Relationship.OWNED)
                        ownedEntities += 1;

                if (ownedEntities > 0) {
                    ArrayList<LivingEntity> unitsToAdd = new ArrayList<>();
                    for (LivingEntity unit : preselectedUnit)
                        if (UnitClientEvents.getPlayerToEntityRelationship(unit) == Relationship.OWNED)
                            unitsToAdd.add(unit);

                    if (Keybindings.shiftMod.isDown()) {
                        List<Integer> selectedIds = UnitClientEvents.getSelectedUnits().stream().map(Entity::getId).toList();
                        unitsToAdd.removeIf(e -> selectedIds.contains(e.getId()));
                    } else {
                        UnitClientEvents.clearSelectedUnits();
                    }

                    for (LivingEntity unit : unitsToAdd)
                        UnitClientEvents.addSelectedUnit(unit);
                }

            }
            cursorLeftClickDownPos = new Vec2(-1,-1);
            cursorLeftClickDragPos = new Vec2(-1,-1);
        }
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
            rightClickDown = false;
        }
    }

    // prevent moused over blocks being outlined in the usual way (ie. by raytracing from player to block)
    @SubscribeEvent
    public static void onHighlightBlockEvent(RenderHighlightEvent.Block evt) {
        if (MC.level != null && OrthoviewClientEvents.isEnabled())
            evt.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS ||
            HudClientEvents.isMouseOverAnyButtonOrHud())
            return;
        if (MC.level != null && OrthoviewClientEvents.isEnabled()) {

            Building preSelBuilding = BuildingClientEvents.getPreselectedBuilding();
            // don't draw block outline if we've selected a builder unit and are mousing over a building (unless leftClick action is MOVE)
            boolean buildingTargetedByWorker = (HudClientEvents.hudSelectedEntity instanceof WorkerUnit &&
                    preSelBuilding != null &&
                    CursorClientEvents.getLeftClickAction() != UnitAction.MOVE &&
                    (BuildingClientEvents.getPlayerToBuildingRelationship(preSelBuilding) == Relationship.OWNED ||
                    CursorClientEvents.getLeftClickAction() == UnitAction.BUILD_REPAIR));
            // same for attacker
            boolean buildingTargetedByAttacker = (HudClientEvents.hudSelectedEntity instanceof AttackerUnit &&
                    preSelBuilding != null &&
                    CursorClientEvents.getLeftClickAction() != UnitAction.MOVE &&
                    (BuildingClientEvents.getPlayerToBuildingRelationship(preSelBuilding) != Relationship.OWNED ||
                    CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK));

            // do we own any of the selected buildings or entities?
            // will be false if there are none selected in the first place
            boolean ownAnySelected = false;
            ArrayList<Building> selBuildings = BuildingClientEvents.getSelectedBuildings();
            for (Building building : selBuildings)
                if (building != null && BuildingClientEvents.getPlayerToBuildingRelationship(building) == Relationship.OWNED)
                    ownAnySelected = true;
            for (LivingEntity entity : UnitClientEvents.getSelectedUnits()) {
                if (UnitClientEvents.getPlayerToEntityRelationship(entity) == Relationship.OWNED) {
                    ownAnySelected = true;
                    break;
                }
            }
            boolean isLeftClickActionStartRTS = leftClickAction != null &&
                List.of(UnitAction.STARTRTS_VILLAGERS,
                    UnitAction.STARTRTS_MONSTERS,
                    UnitAction.STARTRTS_PIGLINS).contains(leftClickAction);

            if ((!OrthoviewClientEvents.isCameraMovingByMouse() &&
                !leftClickDown && ownAnySelected &&
                UnitClientEvents.getPreselectedUnits().size() == 0 &&
                BuildingClientEvents.getPreselectedBuilding() == null &&
                !buildingTargetedByWorker && !buildingTargetedByAttacker) || isLeftClickActionStartRTS) {

                if (MC.level.getBlockState(getPreselectedBlockPos().offset(0,1,0)).getBlock() instanceof SnowLayerBlock) {
                    AABB aabb = new AABB(preselectedBlockPos);
                    aabb = aabb.setMaxY(aabb.maxY + 0.13f);
                    MyRenderer.drawSolidBox(evt.getPoseStack(), aabb, null, 1, 1, 1, rightClickDown ? 0.3f : 0.15f, new ResourceLocation("forge:textures/white.png"));
                    aabb = new AABB(preselectedBlockPos).move(0,0.13,0);
                    MyRenderer.drawLineBox(evt.getPoseStack(), aabb, 1.0f,1.0f,1.0f, rightClickDown ? 1.0f : 0.5f);
                } else {
                    MyRenderer.drawBox(evt.getPoseStack(), preselectedBlockPos, 1, 1, 1, rightClickDown ? 0.3f : 0.15f);
                    MyRenderer.drawBlockOutline(evt.getPoseStack(), preselectedBlockPos, rightClickDown ? 1.0f : 0.5f);
                }
            }
        }
    }

    // returns the exact spot that the cursorWorldPos ray meets a solid block
    public static Vec3 getRefinedCursorWorldPos(Vector3d cursorWorldPosNear, Vector3d cursorWorldPosFar) {
        Vec3 vectorNear = new Vec3(cursorWorldPosNear.x, cursorWorldPosNear.y, cursorWorldPosNear.z);
        Vec3 vectorFar = new Vec3(cursorWorldPosFar.x, cursorWorldPosFar.y, cursorWorldPosFar.z);

        // clip() returns the point of clip, not the clipped block giving off-by-one errors so move slightly to compensate
        HitResult hitResult = null;
        if (MC.level != null) {
            hitResult = clip(MC.level, new ClipContext(vectorNear, vectorFar, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null));
        }

        if (hitResult != null)
            return hitResult.getLocation().add(new Vec3(-0.001,-0.001,-0.001));
        return new Vec3(0,0,0);
    }

    // from ClientLevel.clip() but added exception for leaf blocks
    private static BlockHitResult clip(ClientLevel level, ClipContext pContext) {
        return traverseBlocks(pContext.getFrom(), pContext.getTo(), pContext, (clipContext, blockPos) -> {
            BlockState blockstate = level.getBlockState(blockPos);
            FluidState fluidstate = level.getFluidState(blockPos);
            Vec3 vec3 = clipContext.getFrom();
            Vec3 vec31 = clipContext.getTo();
            VoxelShape voxelshape = clipContext.getBlockShape(blockstate, level, blockPos);
            BlockHitResult blockhitresult = level.clipWithInteractionOverride(vec3, vec31, blockPos, voxelshape, blockstate);
            VoxelShape voxelshape1 = clipContext.getFluidShape(fluidstate, level, blockPos);
            BlockHitResult blockhitresult1 = voxelshape1.clip(vec3, vec31, blockPos);
            double d0 = blockhitresult == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockhitresult.getLocation());
            double d1 = blockhitresult1 == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockhitresult1.getLocation());
            BlockHitResult result = d0 <= d1 ? blockhitresult : blockhitresult1;

            if (result != null) {
                Block block = level.getBlockState(result.getBlockPos()).getBlock();
                if (OrthoviewClientEvents.shouldHideLeaves() && level.getBlockState(result.getBlockPos()).getBlock() instanceof LeavesBlock)
                    result = null;
                else if (block instanceof SnowLayerBlock)
                    result = null;
            }
            return result;

        }, (p_151372_) -> {
            Vec3 vec3 = p_151372_.getFrom().subtract(p_151372_.getTo());
            return BlockHitResult.miss(p_151372_.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(p_151372_.getTo()));
        });
    }

    private static BlockPos getRefinedBlockPos(BlockPos bp, Vector3d cursorWorldPosNear) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        blocks.add(bp);
        blocks.add(bp.north());
        blocks.add(bp.south());
        blocks.add(bp.east());
        blocks.add(bp.west());
        blocks.add(bp.north().east());
        blocks.add(bp.south().east());
        blocks.add(bp.north().west());
        blocks.add(bp.south().west());

        blocks.add(bp.above());
        blocks.add(bp.above().north());
        blocks.add(bp.above().south());
        blocks.add(bp.above().east());
        blocks.add(bp.above().west());
        blocks.add(bp.above().north().east());
        blocks.add(bp.above().south().east());
        blocks.add(bp.above().north().west());
        blocks.add(bp.above().south().west());

        blocks.add(bp.below());
        blocks.add(bp.below().north());
        blocks.add(bp.below().south());
        blocks.add(bp.below().east());
        blocks.add(bp.below().west());
        blocks.add(bp.below().north().east());
        blocks.add(bp.below().south().east());
        blocks.add(bp.below().north().west());
        blocks.add(bp.below().south().west());

        BlockPos bestBp = bp;
        double smallestDist = 10000;
        Vector3d lookVector = MiscUtil.getPlayerLookVector(MC);

        for (BlockPos block : blocks) {
            double dist = new Vec3(block.getX(), block.getY(), block.getZ())
                    .distanceTo(new Vec3(cursorWorldPosNear.x, cursorWorldPosNear.y, cursorWorldPosNear.z));

            if (MC.level != null) {
                // if we have a worker selected then include resource blocks that would otherwise be ignored like plants
                boolean isBlockSelectableResource = false;
                if (HudClientEvents.hudSelectedEntity instanceof Unit workerUnit)
                    isBlockSelectableResource = ResourceSources.getFromBlockPos(block, MC.level) != null;

                BlockState bs = MC.level.getBlockState(block);
                if ((bs.getMaterial().isSolidBlocking() || isBlockSelectableResource) &&
                    (!(bs.getBlock() instanceof LeavesBlock) || !OrthoviewClientEvents.shouldHideLeaves()) &&
                    !(bs.getBlock() instanceof SnowLayerBlock) &&
                    MyMath.rayIntersectsAABBCustom(cursorWorldPosNear, lookVector, new AABB(block)) &&
                    dist < smallestDist ) {
                    smallestDist = dist;
                    bestBp = block;
                }
            }
        }
        return bestBp;
    }
}


