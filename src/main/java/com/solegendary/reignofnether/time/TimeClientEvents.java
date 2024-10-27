package com.solegendary.reignofnether.time;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.NightSource;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.time.TimeUtils.*;

public class TimeClientEvents {

    private static int xPos = 0;
    private static int yPos = 0;

    private static final Minecraft MC = Minecraft.getInstance();

    // setting this value causes the time of day to smoothly move towards it regardless of the server time
    public static long targetClientTime = 0;
    // actual time on the server
    public static long serverTime = 0;

    public static NightCircleMode nightCircleMode = NightCircleMode.NO_OVERLAPS;

    private static final Button CLOCK_BUTTON = new Button(
        "Clock",
        14,
        null,
        null,
        null,
        () -> false,
        () -> false,
        () -> true,
        () -> {
            if (nightCircleMode == NightCircleMode.ALL)
                nightCircleMode = NightCircleMode.NO_OVERLAPS;
            else if (nightCircleMode == NightCircleMode.NO_OVERLAPS)
                nightCircleMode = NightCircleMode.OFF;
            else if (nightCircleMode == NightCircleMode.OFF)
                nightCircleMode = NightCircleMode.ALL;
            for (Building building : BuildingClientEvents.getBuildings())
                if (building instanceof NightSource ns)
                    ns.updateNightBorderBps();
        },
        null,
        null
    );

    // render directly above the minimap
    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post evt) {
        if (!OrthoviewClientEvents.isEnabled() || MC.isPaused() ||
            !TutorialClientEvents.isAtOrPastStage(TutorialStage.MINIMAP_CLICK))
            return;

        xPos = MC.getWindow().getGuiScaledWidth() - MinimapClientEvents.getMapGuiRadius() - (MinimapClientEvents.CORNER_OFFSET * 2) + 2;
        yPos = MC.getWindow().getGuiScaledHeight() - (MinimapClientEvents.getMapGuiRadius() * 2) - (MinimapClientEvents.CORNER_OFFSET * 2) - 6;

        ItemRenderer itemrenderer = MC.getItemRenderer();

        itemrenderer.renderAndDecorateItem(new ItemStack(Items.CLOCK), xPos, yPos);
    }

    @SubscribeEvent
    public static void onDrawScreen(ScreenEvent.Render.Post evt) {
        if (!OrthoviewClientEvents.isEnabled() || MC.isPaused() ||
            !TutorialClientEvents.isAtOrPastStage(TutorialStage.MINIMAP_CLICK))
            return;

        xPos = MC.getWindow().getGuiScaledWidth() - MinimapClientEvents.getMapGuiRadius() - (MinimapClientEvents.CORNER_OFFSET * 2) + 2;
        yPos = MC.getWindow().getGuiScaledHeight() - (MinimapClientEvents.getMapGuiRadius() * 2) - (MinimapClientEvents.CORNER_OFFSET * 2) - 6;

        CLOCK_BUTTON.render(evt.getPoseStack(), xPos-3, yPos-3, evt.getMouseX(), evt.getMouseY());
    }

    @SubscribeEvent
    public static void onMousePress(ScreenEvent.MouseButtonPressed.Post evt) {
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1)
            CLOCK_BUTTON.checkClicked((int) evt.getMouseX(), (int) evt.getMouseY(), true);
        //else if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_2)
        //    CLOCK_BUTTON.checkClicked((int) evt.getMouseX(), (int) evt.getMouseY(), false);
    }

    @SubscribeEvent
    public static void onDrawScreen(ScreenEvent.Render evt) {
        if (!TutorialClientEvents.isAtOrPastStage(TutorialStage.MINIMAP_CLICK))
            return;

        final int GUI_LENGTH = 16;

        if (evt.getMouseX() > xPos && evt.getMouseX() <= xPos + GUI_LENGTH &&
            evt.getMouseY() > yPos && evt.getMouseY() <= yPos + GUI_LENGTH) {

            // 'day' is when undead start burning, ~500
            // 'night' is when undead stop burning, ~12500
            boolean isDay = isDay(serverTime);
            String dayStr = isDay ? " (day)" : " (night)";
            String timeStr = get12HourTimeStr(serverTime) + dayStr;

            FormattedCharSequence timeUntilStr = FormattedCharSequence.forward(
                    getTimeUntilStr(serverTime, isDay ? DUSK : DAWN) + " until " + (isDay ? "night" : "day"), Style.EMPTY);

            FormattedCharSequence gameLengthStr = FormattedCharSequence.forward("", Style.EMPTY);

            if (PlayerClientEvents.isRTSPlayer)
                gameLengthStr = FormattedCharSequence.forward("Game time: " + getTimeStrFromTicks(PlayerClientEvents.rtsGameTicks), Style.EMPTY);

            List<FormattedCharSequence> tooltip = List.of(
                    FormattedCharSequence.forward("Time: " + timeStr, Style.EMPTY),
                    timeUntilStr,
                    FormattedCharSequence.forward("" + timeStr, Style.EMPTY),
                    gameLengthStr,
                    FormattedCharSequence.forward("Night circles: " + nightCircleMode.name(), Style.EMPTY)
            );
            if (targetClientTime != serverTime)
                tooltip = List.of(
                        FormattedCharSequence.forward("Time is distorted to midnight", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("Real time: " + timeStr, Style.EMPTY),
                        timeUntilStr,
                        gameLengthStr,
                        FormattedCharSequence.forward("Night circles: " + nightCircleMode.name().replace("_"," "), Style.EMPTY)
                );

            MyRenderer.renderTooltip(
                    evt.getPoseStack(),
                    tooltip,
                    evt.getMouseX(),
                    evt.getMouseY()
            );
        }
    }

    // show corners of all frozenChunks
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;
        if (!OrthoviewClientEvents.isEnabled() || nightCircleMode == NightCircleMode.OFF || MC.level == null)
            return;

        // draw night-ranges for monsters
        for (Building building : BuildingClientEvents.getBuildings())
            if (building instanceof NightSource ns)
                for (BlockPos bp : ns.getNightBorderBps()) {
                    if (BuildingClientEvents.getSelectedBuildings().contains(building))
                        MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.UP, bp, 0f, 0.8f, 0f, 0.3f);
                    else
                        MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.UP, bp, 0f, 0f, 0f, 0.6f);
                    /* causes a lot of flickering
                    if (MC.level.getBlockState(bp.north()).isAir())
                        MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.NORTH, bp, 0f, 0f, 0f, 0.5f);
                    if (MC.level.getBlockState(bp.south()).isAir())
                        MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.SOUTH, bp, 0f, 0f, 0f, 0.5f);
                    if (MC.level.getBlockState(bp.east()).isAir())
                        MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.EAST, bp, 0f, 0f, 0f, 0.5f);
                    if (MC.level.getBlockState(bp.west()).isAir())
                        MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.WEST, bp, 0f, 0f, 0f, 0.5f);
                    */
                }
    }

    // maintain a mapping of night sources for easy culling calcs
    private static final int NIGHT_SOURCES_UPDATE_TICKS_MAX = 100;
    private static int nightSourcesUpdateTicks = NIGHT_SOURCES_UPDATE_TICKS_MAX;
    public static ArrayList<Pair<BlockPos, Integer>> nightSourceOrigins = new ArrayList<>();
    public static final int VISIBLE_BORDER_ADJ = 2; // shrink a bit so borderlines themselves are safe to walk on

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent evt) {

        nightSourcesUpdateTicks -= 1;
        if (nightSourcesUpdateTicks <= 0) {
            nightSourcesUpdateTicks = NIGHT_SOURCES_UPDATE_TICKS_MAX;

            nightSourceOrigins.clear();

            // get list of night source centre:range pairs
            for (Building building : BuildingClientEvents.getBuildings()) {
                if (!building.isExploredClientside || !(building instanceof NightSource ns))
                    continue;

                nightSourceOrigins.add(new Pair<>(building.centrePos, ns.getNightRange() - VISIBLE_BORDER_ADJ));
            }
        }
    }
}














