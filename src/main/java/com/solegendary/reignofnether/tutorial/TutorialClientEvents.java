package com.solegendary.reignofnether.tutorial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.buildings.villagers.*;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerClientEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.MoveToTargetBlockGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.SkeletonUnit;
import com.solegendary.reignofnether.unit.units.monsters.ZombieUnit;
import com.solegendary.reignofnether.unit.units.villagers.*;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.solegendary.reignofnether.registrars.SoundRegistrar.*;
import static com.solegendary.reignofnether.tutorial.TutorialStage.*;

public class TutorialClientEvents {

    private static Minecraft MC = Minecraft.getInstance();
    private static TutorialStage tutorialStage = INTRO;
    private static boolean enabled = false;

    private static int ticksToProgressStage = 0;
    private static int ticksToNextStage = 0;
    private static int ticksOnStage = 0;
    private static int stageProgress = 0; // used to track progress within each TutorialStage
    private static boolean pressSpaceToContinue = false;
    private static boolean blockUpdateStage = false; // prevent updateStage being called when we block it with a delay

    public static boolean pannedUp = false;
    public static boolean pannedDown = false;
    public static boolean pannedLeft = false;
    public static boolean pannedRight = false;
    public static boolean clickedMinimap = false;
    private static int villagersHoldingFood = 0;

    private static final ArrayList<Building> damagedBuildings = new ArrayList<>();

    // all these positions are for camera only, actual spawn locations differ slightly on the serverside
    private static final Vec3i SPAWN_POS = new Vec3i(-2950, 0, -1166);
    public static final Vec3i BUILD_CAM_POS = new Vec3i(-2944, 0, -1200);
    public static final Vec3i BUILD_CAPITOL_POS = new Vec3i(-2936, 67, -1217);
    private static final Vec3i WOOD_POS = new Vec3i(-2919, 0, -1196);
    private static final Vec3i ORE_POS = new Vec3i(-2951, 0, -1224);
    private static final Vec3i FOOD_POS = new Vec3i(-2939, 0, -1173);

    private static final Vec3i MONSTER_CAMERA_POS = new Vec3i(-2983, 64, -1199);
    private static final Vec3i MONSTER_BASE_POS = new Vec3i(-3085, 72, -1277);

    private static final Vec3i BRIDGE_POS = new Vec3i(-2997,0,-1206);
    private static final Vec3i ARMY_POS = new Vec3i(-2963, 64, -1160);

    private static Supplier<Boolean> shouldPauseTicking = () -> false;

    private static int helpButtonClicks = 0;
    private static String helpButtonText = "";
    public static final Button helpButton = new Button(
            "Tutorial Help",
            18,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/help.png"),
            (Keybinding) null,
            () -> false,
            () -> !isEnabled(),
            () -> isEnabled() && !helpButtonText.isEmpty(),
            () ->  {
                helpButtonClicks += 1;
                if (getStage() == INTRO)
                    specialMsg(helpButtonText);
                else
                    msg(helpButtonText, true, CHAT);
            },
            () -> { },
            List.of(FormattedCharSequence.forward("Tutorial Help", Style.EMPTY))
    );

    public static void loadStage(TutorialStage stage) {
        if (stage == null || stage == getStage() || stage == INTRO || stage == COMPLETED)
            return;

        specialMsg("Welcome back... resuming at stage: " + stage.name().replace("_", " "));

        tutorialStage = stage;
        ticksOnStage = 0;
        stageProgress = 0;
        blockUpdateStage = false;
        clearHelpButtonText();
        TutorialRendering.clearButtonName();
        updateStage();
        shouldPauseTicking = () -> false;
    }

    private static void setHelpButtonText(String text) {
        helpButtonClicks = 0;
        helpButtonText = text;
    }

    private static void clearHelpButtonText() {
        helpButtonClicks = 0;
        helpButtonText = "";
    }

    public static int getStageProgress() {
        return stageProgress;
    }

    public static boolean isEnabled() {
        return enabled && MC.hasSingleplayerServer();
    }

    public static void setEnabled(boolean value) {
        if (value && !enabled && MC.player != null) {
            MC.player.sendSystemMessage(Component.literal(""));
            MC.player.sendSystemMessage(Component.literal("Welcome to the Reign of Nether Tutorial!").withStyle(Style.EMPTY.withBold(true)));
            MC.player.sendSystemMessage(Component.literal("Press F12 to get started."));
            MC.player.sendSystemMessage(Component.literal(""));
        }
        enabled = value;
    }

    public static TutorialStage getStage() { return tutorialStage; }

    private static void nextStage() {
        tutorialStage = tutorialStage.next();
        ticksOnStage = 0;
        stageProgress = 0;
        blockUpdateStage = false;
        clearHelpButtonText();
        TutorialRendering.clearButtonName();
        updateStage();
        shouldPauseTicking = () -> false;
        TutorialServerboundPacket.saveStage(tutorialStage);
    }
    private static void prevStage() {
        tutorialStage = tutorialStage.prev();
        ticksOnStage = 0;
        stageProgress = 0;
        blockUpdateStage = false;
        clearHelpButtonText();
        TutorialRendering.clearButtonName();
        updateStage();
        shouldPauseTicking = () -> false;
        TutorialServerboundPacket.saveStage(tutorialStage);
    }

    // check if we need to render an arrow to point at the next button
    public static void checkAndRenderNextAction(PoseStack poseStack, ArrayList<Button> buttons) {
        if (tutorialStage == PAN_CAMERA && MC.screen != null) {
            if (!pannedUp)
                TutorialRendering.pointAtWithArrow(poseStack, MC.screen.width / 2, -Button.iconFrameSize, true);
            if (!pannedDown)
                TutorialRendering.pointAtWithArrow(poseStack, MC.screen.width / 2, MC.screen.height, true);
            if (!pannedLeft)
                TutorialRendering.pointAtWithArrow(poseStack, -Button.iconFrameSize, MC.screen.height / 2, false);
            if (!pannedRight)
                TutorialRendering.pointAtWithArrow(poseStack, MC.screen.width, MC.screen.height / 2, false);
        } else {
            TutorialRendering.highlightNextButton(poseStack, buttons);
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre evt) { updateStage(); }

    @SubscribeEvent
    public static void onKeyRelease(ScreenEvent.KeyReleased.Pre evt) { updateStage(); }

    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyPressed.Pre evt) {
        if (!OrthoviewClientEvents.isEnabled())
            return;

        if (Keybindings.ctrlMod.isDown() && Keybindings.altMod.isDown() && evt.getKeyCode() == GLFW.GLFW_KEY_SPACE) {
            nextStage();
            specialMsg("Skipping tutorial stage... you are now on: " + getStage().name());
        }

        if (pressSpaceToContinue && evt.getKeyCode() == GLFW.GLFW_KEY_SPACE) {
            pressSpaceToContinue = false;
            nextStage();
        }

        if (pressSpaceToContinue && evt.getKeyCode() == GLFW.GLFW_KEY_SPACE) {
            pressSpaceToContinue = false;
            nextStage();
        }
    }

    @SubscribeEvent
    public static void TickEvent(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || MC.isPaused() || !isEnabled() || !OrthoviewClientEvents.isEnabled())
            return;

        if (shouldPauseTicking.get())
            return;

        if (ticksOnStage < Integer.MAX_VALUE) {
            if (ticksOnStage % 20 == 0)
                updateStage();
            ticksOnStage += 1;
        }
        if (ticksToProgressStage > 0) {
            ticksToProgressStage -= 1;
            if (ticksToProgressStage == 0) {
                progressStage();
            }
        }
        if (ticksToNextStage > 0) {
            ticksToNextStage -= 1;
            if (ticksToNextStage == 0)
                nextStage();
        }
    }

    private static void msg(String msg, boolean bold, RegistryObject<SoundEvent> soundEvt) {
        if (MC.player == null)
            return;
        MC.player.playSound(soundEvt.get(), 1.2f, 1.0f);
        MC.player.sendSystemMessage(Component.literal(""));
        if (bold)
            MC.player.sendSystemMessage(Component.literal(msg).withStyle(Style.EMPTY.withBold(true)));
        else
            MC.player.sendSystemMessage(Component.literal(msg));
        MC.player.sendSystemMessage(Component.literal(""));
    }

    private static void msg(String msg) {
        msg(msg, false, CHAT);
    }

    private static void specialMsg(String msg) {
        msg(msg, true, ALLY);
    }


    public static boolean isAtOrPastStage(TutorialStage stage) {
        if (!isEnabled())
            return true;
        return tutorialStage.ordinal() >= stage.ordinal();
    }

    /*
    @SubscribeEvent
    public static void onRenderOverLay(RenderGuiOverlayEvent.Pre evt) {
        MiscUtil.drawDebugStrings(evt.getPoseStack(), MC.font, new String[] {
                "stage: " + getStage(),
                "progress: " + stageProgress,
                "ticks: " + ticksOnStage
        });
    } */



    private static void progressStage() {
        blockUpdateStage = false;
        stageProgress += 1;
        updateStage();
    }

    private static void progressStageAfterDelay(int delay) {
        blockUpdateStage = true;
        ticksToProgressStage = delay;
    }

    private static void nextStageAfterDelay(int delay) {
        blockUpdateStage = true;
        ticksToNextStage = delay;
        clearHelpButtonText();
        TutorialRendering.clearButtonName();
    }

    private static void nextStageAfterSpace() {
        blockUpdateStage = true;
        msg("Press SPACE when you're ready to continue.", true, CHAT);
        setHelpButtonText("Press SPACE when you're ready to continue.");
        TutorialRendering.clearButtonName();
        pressSpaceToContinue = true;
    }

    private static boolean hasUnitSelected(String unitName) {
        return UnitClientEvents.getSelectedUnits().size() > 0 &&
                UnitClientEvents.getSelectedUnits().get(0)
                .getName().getString().toLowerCase().contains(unitName.toLowerCase());
    }

    private static boolean hasBuildingSelected(String buildingName) {
        return BuildingClientEvents.getSelectedBuildings().size() > 0 &&
                BuildingClientEvents.getSelectedBuildings().get(0)
                .name.toLowerCase().equals(buildingName.toLowerCase());
    }

    // Whenever doing anything that could be a tutorial action like enabling orthoview or building your first building,
    // check here to progress the tutorial and give updates to the player.
    // This theoretically should be able to be called at any time but will only actually progress if actions are done
    // This should be called in specific clientside events like
    public static void updateStage() {
        if (!isEnabled() || blockUpdateStage)
            return;

        switch(tutorialStage) {
            case INTRO -> {
                if (stageProgress == 0 && OrthoviewClientEvents.isEnabled()) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    OrthoviewClientEvents.lockCam();
                    msg("Welcome to RTS view. Instead of the usual first-person minecraft camera, " +
                        "here we can view the world from a top-down perspective.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 1) {
                    msg("TIP: If you want to see any of these messages again, open chat with ENTER or the button on the right.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 2) {
                    msg("If at any point you're lost or need a reminder on what to do next, click the button at the " +
                        "top right. Try doing that now to continue.");
                    setHelpButtonText("You needed to click this button, which you just did. Great work!");
                    TutorialRendering.setButtonName(helpButton.name);
                    progressStage();
                }
                else if (stageProgress == 3 && helpButtonClicks > 0) {
                    nextStageAfterDelay(120);
                }
            }
            case PAN_CAMERA -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    OrthoviewClientEvents.unlockCam();
                    msg("To move your camera, move your mouse to the edges of the screen. Try it now.");
                    setHelpButtonText("Move your mouse to each edge of your screen until the camera starts moving, " +
                                        "as shown by the arrows.");
                    progressStage();
                }
                else if (stageProgress == 1 && pannedUp && pannedDown && pannedLeft && pannedRight) {
                    specialMsg("Nicely done.");
                    nextStageAfterDelay(100);
                }
            }
            case CAMERA_TIPS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("TIP: You can also move the camera with arrow keys.");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 1) {
                    msg("TIP: You can zoom the camera with ALT+SCROLL and rotate it with " +
                        "ALT+RIGHT-CLICK.");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 2) {
                    nextStageAfterSpace();
                }
            }
            case MINIMAP_CLICK -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Since you could get lost with a top-down view like this, here's a minimap " +
                        "for you to help navigate.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 1) {
                    clickedMinimap = false;
                    msg("You can move around the world quickly by clicking on a spot on the map. " +
                        "Try doing that now.");
                    setHelpButtonText("Click a spot on the minimap at the bottom right to move the camera there");
                    progressStage();
                }
                else if (stageProgress == 2 && clickedMinimap) {
                    specialMsg("Good work!");
                    nextStageAfterDelay(100);
                }
            }
            case MINIMAP_TIPS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("If you need to move really far away, you can also SHIFT+CLICK " +
                        "to recentre the map on that location.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 1) {
                    msg("TIP: Press M or click the bottom right button to expand the map. " +
                        "The very dark areas are outside the world border.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 2) {
                    nextStageAfterSpace();
                }
            }
            case PLACE_WORKERS_A -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("It's time to start playing with some units.");
                    progressStageAfterDelay(120);
                } else if (stageProgress == 1) {
                    msg("Let's get started by spawning in some villagers here.");
                    OrthoviewClientEvents.forceMoveCam(SPAWN_POS, 50);
                    OrthoviewClientEvents.lockCam();
                    nextStageAfterDelay(100);
                }
            }
            case PLACE_WORKERS_B -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Click the button at the top right and then click on the ground where " +
                        "you want to place them.");
                    setHelpButtonText("Click the button at the top right and then click on the ground where " +
                                        "you want to place your villagers.");
                    TutorialRendering.setButtonName("Villagers");
                    progressStage();
                }
                else if (stageProgress == 1 && PlayerClientEvents.isRTSPlayer) {
                    TutorialRendering.clearButtonName();
                    specialMsg("Excellent.");
                    OrthoviewClientEvents.unlockCam();
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    nextStageAfterDelay(100);
                }
            }
            case SELECT_UNIT -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Villagers are your worker units who can build and gather resources " +
                        "and are essential to starting and maintaining a good base.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 1) {
                    msg("Try selecting one with LEFT-CLICK.");
                    setHelpButtonText("LEFT-CLICK a villager to select it.");
                    UnitClientEvents.clearSelectedUnits();
                    progressStage();
                }
                else if (stageProgress == 2 && hasUnitSelected("villager")) {
                    nextStage();
                }
            }
            case MOVE_UNIT -> {
                if (stageProgress == 0 && hasUnitSelected("villager")) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Now, RIGHT-CLICK where you want to move it.");
                    setHelpButtonText("LEFT-CLICK a villager to select it, then RIGHT-CLICK on the ground to move it.");
                    progressStage();
                }
                else if (stageProgress == 1 && hasUnitSelected("villager")) {
                    Unit unit = (Unit) UnitClientEvents.getSelectedUnits().get(0);
                    MoveToTargetBlockGoal goal = unit.getMoveGoal();
                    if (goal != null && goal.getMoveTarget() != null) {
                        specialMsg("Nice work.");
                        nextStageAfterDelay(100);
                    }
                }
            }
            case BOX_SELECT_UNITS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Now let's try selecting a group of villagers.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 1) {
                    msg("To do this, hold LEFT-CLICK and DRAG your mouse across them, then release to select.");
                    setHelpButtonText("Hold LEFT-CLICK and DRAG your mouse across a group of villagers, " +
                                    "then release to select them.");
                    progressStage();
                }
                else if (stageProgress == 2 && UnitClientEvents.getSelectedUnits().size() > 1) {
                    nextStage();
                }
            }
            case MOVE_UNITS -> {
                if (stageProgress == 0 && UnitClientEvents.getSelectedUnits().size() > 1) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Now, RIGHT-CLICK where you want to move the group.");
                    setHelpButtonText("With a group of villagers selected, RIGHT-CLICK on the ground to move them.");
                    progressStage();
                }
                else if (stageProgress == 1 && UnitClientEvents.getSelectedUnits().size() > 1) {
                    Unit unit = (Unit) UnitClientEvents.getSelectedUnits().get(1);
                    MoveToTargetBlockGoal goal = unit.getMoveGoal();
                    if (goal != null && goal.getMoveTarget() != null) {
                        specialMsg("Great job!");
                        nextStageAfterDelay(100);
                    }
                }
            }
            case UNIT_TIPS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("TIP: You can also double click a unit to select all units of the same type.");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 1) {
                    msg("TIP: If you want to deselect your units, press the tilde (~) key");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 2) {
                    msg("TIP: For you RTS fans, control grouping with CTRL+NUM is also a feature.");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 3) {
                    nextStageAfterSpace();
                }
            }
            case BUILD_INTRO -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("It's time to start your base.");
                    progressStageAfterDelay(100);
                }
                else if (stageProgress == 1) {
                    msg("The first and most important building of any base is always the capitol.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 2) {
                    msg("This looks like a good spot for it, being flat ground, near lots of resources and with " +
                        "plenty of space around it for other buildings.");
                    OrthoviewClientEvents.forceMoveCam(BUILD_CAM_POS, 50);
                    progressStageAfterDelay(180);
                }
                else if (stageProgress == 3) {
                    msg("Note that building takes resources. Luckily, the TOP-LEFT shows we have more than enough " +
                        "WOOD and ORE needed.");
                    nextStageAfterDelay(120);
                }
            }
            case BUILD_TOWN_CENTRE -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Select your workers, then click the bottom-left button and click on the " +
                        "ground where you want to place your capitol.");
                    setHelpButtonText("Select your workers, then click the bottom-left button and click on the " +
                                      "ground to place a town centre.");
                    TutorialRendering.setButtonName(TownCentre.buildingName);
                    progressStage();
                }
                else if (stageProgress == 1 && BuildingClientEvents.getBuildings().size() > 0) {
                    if (BuildingClientEvents.getBuildings().get(0) instanceof TownCentre)
                        nextStage();
                }
            }
            case BUILDING_TIPS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("If they aren't already, you can have all of your villagers help to build to speed up progress. " +
                        "To do this, select your workers and RIGHT-CLICK the building.");
                    setHelpButtonText("Just wait for your Town Centre to complete. If your workers stopped building " +
                                      "for some reason, just select them and RIGHT-CLICK it to resume.");
                    progressStageAfterDelay(240);
                }
                else if (stageProgress == 1) {
                    msg("You can also select the building itself like a unit to see how far along it is in building.");
                    progressStage();
                }
                else if (stageProgress == 2 && BuildingClientEvents.getBuildings().size() > 0) {
                    if (BuildingClientEvents.getBuildings().get(0) instanceof TownCentre townCentre && townCentre.isBuilt) {
                        specialMsg("Congratulations, you now have a base set up!");
                        nextStageAfterDelay(100);
                    }
                }
            } //msg("You can also set a rally point for your building with RIGHT-CLICK.");
            case TRAIN_WORKER -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Capitols like the Town Centre are the only building that can produce workers like villagers.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 1) {
                    msg("Note that producing workers takes 50 FOOD each. We should have enough resources for quite a lot");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 2) {
                    msg("LEFT-CLICK to select your Town Centre, RIGHT-CLICK to set a rally point - units will " +
                        "automatically go to that spot when they appear.");
                    setHelpButtonText("LEFT-CLICK to select your Town Centre, then RIGHT-CLICK on the ground to set a rally point.");
                    progressStage();
                }
                else if (stageProgress == 3 && BuildingClientEvents.getBuildings().size() > 0) {
                    if (BuildingClientEvents.getBuildings().get(0) instanceof TownCentre townCentre &&
                        townCentre.getRallyPoint() != null) {
                        msg("Now let's try making a villager. With your Town Centre selected, click the " +
                            "bottom-left button to start producing one.");
                        setHelpButtonText("LEFT-CLICK to select your Town Centre, click the bottom-left button make a villager, " +
                                          "then wait for it to be completed.");
                        TutorialRendering.setButtonName(VillagerProd.itemName);
                        progressStage();
                    }
                }
                else if (stageProgress == 4 && BuildingClientEvents.getBuildings().size() > 0) {
                    if (BuildingClientEvents.getBuildings().get(0) instanceof TownCentre townCentre &&
                            townCentre.productionQueue.size() > 0) {
                        msg("TIP: you can queue up as many units to build as you can afford.");
                        progressStage();
                    }
                }
                else if (stageProgress == 5 && UnitClientEvents.getAllUnits().size() > 3) {
                    specialMsg("Nice work. Workers are vital for a healthy base!");
                    clearHelpButtonText();
                    TutorialRendering.clearButtonName();
                    progressStageAfterDelay(100);
                }
                else if (stageProgress == 6) {
                    msg("TIP: you can cancel any in-progress units for a full refund by clicking their icon at the bottom.");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 7) {
                    nextStageAfterSpace();
                }
            }
            case GATHER_WOOD -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Now that we have a bunch of workers, we can start gathering some resources.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 1) {
                    msg("There's a forest over here that we can get wood from.");
                    OrthoviewClientEvents.forceMoveCam(WOOD_POS, 100);
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 2) {
                    msg("To gather a resource, select a worker, then RIGHT-CLICK a tree.");
                    setHelpButtonText("Select any villager, then RIGHT-CLICK any tree.");
                    progressStage();
                }
                else if (stageProgress == 3) {
                    for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
                        if (entity instanceof VillagerUnit villager &&
                                villager.getGatherResourceGoal().isGathering() &&
                                villager.getGatherResourceGoal().getTargetResourceName() == ResourceName.WOOD) {
                            specialMsg("Well done.");
                            clearHelpButtonText();
                            progressStageAfterDelay(100);
                            break;
                        }
                    }
                }
                else if (stageProgress == 4) {
                    msg("TIP: Workers keep gathering until told to do something else. " +
                            "Once they have at least 50 total resources they return it to the town centre.");
                    nextStageAfterDelay(160);
                }
            }
            case GATHER_ORE -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Now let's do the same thing for ore.");
                    progressStageAfterDelay(100);
                }
                else if (stageProgress == 1) {
                    msg("Here's a beach with coal and copper we can get ore from.");
                    OrthoviewClientEvents.forceMoveCam(ORE_POS, 50);
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 2) {
                    msg("Select a worker, then RIGHT-CLICK an ore block.");
                    setHelpButtonText("Select any villager, then RIGHT-CLICK any ore block.");
                    progressStage();
                }
                else if (stageProgress == 3) {
                    for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
                        if (entity instanceof VillagerUnit villager &&
                            villager.getGatherResourceGoal().isGathering() &&
                            villager.getGatherResourceGoal().getTargetResourceName() == ResourceName.ORE) {
                            specialMsg("Well done.");
                            clearHelpButtonText();
                            progressStageAfterDelay(100);
                            break;
                        }
                    }
                }
                else if (stageProgress == 4) {
                    msg("TIP: Sandy biomes like deserts and beaches have more and better ores.");
                    nextStageAfterDelay(120);
                }
            }
            case HUNT_ANIMALS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("While your other workers are busy on wood and ore, let's find some food.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 1) {
                    OrthoviewClientEvents.forceMoveCam(FOOD_POS, 50);
                    TutorialServerboundPacket.doServerAction(TutorialAction.SPAWN_ANIMALS);
                    msg("Here are some pigs that you can hunt for food.");
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 2) {
                    msg("Just like wood and ore, select a worker and RIGHT-CLICK an animal to start hunting it.");
                    setHelpButtonText("Select any villager, then RIGHT-CLICK an animal and wait for it to be killed." +
                                        "Make sure that villager doesn't already have a full inventory.");
                    progressStageAfterDelay(180);
                }
                else if (stageProgress == 3) {
                    msg("Make sure your worker isn't holding any other resources!");
                    progressStage();
                }
                else if (stageProgress == 4) {
                    for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
                        if (entity instanceof VillagerUnit villager) {
                            LivingEntity targetEntity = villager.getTarget();
                            if (ResourceSources.isHuntableAnimal(targetEntity) &&
                                    targetEntity.getHealth() < targetEntity.getMaxHealth()) {
                                msg("TIP: If your worker can't hold all the food after hunting an animal, it will drop to the ground.");
                                progressStage();
                                break;
                            }
                        }
                    }
                }
                else if (stageProgress == 5) {
                    for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
                        if (entity instanceof VillagerUnit villager) {
                            LivingEntity targetEntity = villager.getTarget();
                            if (ResourceSources.isHuntableAnimal(targetEntity) &&
                                    targetEntity.getHealth() < targetEntity.getMaxHealth() / 2) {
                                msg("TIP: Dropped items like food and saplings can be picked by ANY unit and returned for resources.");
                                progressStage();
                                break;
                            }
                        }
                    }
                }
                else if (stageProgress == 6) {
                    for (LivingEntity entity : UnitClientEvents.getAllUnits())
                        if (entity instanceof VillagerUnit villager)
                            for (ItemStack itemStack : villager.getItems())
                                if (Resources.getTotalResourcesFromItems(List.of(itemStack)).food >= 100)
                                    villagersHoldingFood += 1;

                    if (villagersHoldingFood > 0) {
                        specialMsg("Great work!");
                        clearHelpButtonText();
                        progressStageAfterDelay(100);
                    }
                }
                else if (stageProgress == 7) {
                    msg("Your villager should now return the food to your town centre, but if they aren't, " +
                        "simply select the villager and RIGHT-CLICK your Town Centre.");
                    setHelpButtonText("Select your villager that hunted the pig and RIGHT-CLICK your Town Centre.");
                    progressStage();
                }
                else if (stageProgress == 8) {
                    int villagersHoldingFoodNow = 0;
                    for (LivingEntity entity : UnitClientEvents.getAllUnits()) {
                        if (entity instanceof VillagerUnit villager) {
                            for (ItemStack itemStack : villager.getItems()) {
                                Resources res = Resources.getTotalResourcesFromItems(List.of(itemStack));
                                if (res.food > 0)
                                    villagersHoldingFoodNow += 1;
                            }
                        }
                    }
                    if (villagersHoldingFoodNow < villagersHoldingFood) {
                        specialMsg("Excellent.");
                        clearHelpButtonText();
                        progressStageAfterDelay(100);
                    }
                }
                else if (stageProgress == 9) {
                    msg("TIP: Units hold up to 100 total resources, but hunting allows you to go above this maximum.");
                    nextStageAfterSpace();
                }
            }
            case EXPLAIN_BUILDINGS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("Let's expand your base. Some new buildings have been unlocked for you. Select a worker if you " +
                            "haven't already to check them out.");
                    setHelpButtonText("Select any worker to check out your new building options.");
                    shouldPauseTicking = () -> UnitClientEvents.getSelectedUnits().isEmpty() ||
                            !(UnitClientEvents.getSelectedUnits().get(0) instanceof VillagerUnit) ||
                            BuildingClientEvents.getBuildingToPlace() != null;
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 1 && hasUnitSelected("villager")) {
                    TutorialRendering.setButtonName(OakStockpile.buildingName);
                    msg("Stockpiles can give your workers a place to drop off resources faster.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 2 && hasUnitSelected("villager")) {
                    TutorialRendering.setButtonName(VillagerHouse.buildingName);
                    msg("Houses raise your max unit population. Check the top left for your current totals.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 3 && hasUnitSelected("villager")) {
                    TutorialRendering.setButtonName(WheatFarm.buildingName);
                    msg("Farms give you a slow but renewable source of food in exchange for wood.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 4 && hasUnitSelected("villager")) {
                    clearHelpButtonText();
                    TutorialRendering.setButtonName(Barracks.buildingName);
                    msg("Finally, a barracks lets you start training soldiers to fight enemies.");
                    progressStageAfterDelay(160);
                }
                else {
                    nextStage();
                }
            }
            case BUILD_BASE -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    TutorialRendering.clearButtonName();
                    msg("Let's take some time to check out a few of these buildings.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 1) {
                    msg("When you're ready, build a barracks and get prepare to train your first army.");
                    setHelpButtonText("Select any villager and build a barracks building at the bottom left. Make sure " +
                                      "you've gathered enough wood to afford it!");
                    progressStage();
                }
                else if (stageProgress == 2) {
                    for (Building building : BuildingClientEvents.getBuildings()) {
                        if (building instanceof Barracks barracks && barracks.isBuilt) {
                            specialMsg("Great job.");
                            nextStageAfterDelay(100);
                            break;
                        }
                    }
                }
            }
            case EXPLAIN_BARRACKS -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    msg("The barracks is one of many buildings that can produce military units. " +
                            "Select your barracks to check them out.");
                    setHelpButtonText("Select your barracks to check out your military unit options");
                    shouldPauseTicking = () -> BuildingClientEvents.getSelectedBuildings().isEmpty() ||
                            !(BuildingClientEvents.getSelectedBuildings().get(0) instanceof Barracks);
                    progressStageAfterDelay(140);
                }
                else if (stageProgress == 1 && hasBuildingSelected(Barracks.buildingName)) {
                    TutorialRendering.setButtonName(VindicatorProd.itemName);
                    msg("Vindicators are melee units with high health and moderate damage.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 2 && hasBuildingSelected(Barracks.buildingName)) {
                    clearHelpButtonText();
                    TutorialRendering.setButtonName(PillagerProd.itemName);
                    msg("Pillagers are ranged units which attack slowly but with high damage.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 3) {
                    nextStage();
                }
            }
            case BUILD_ARMY -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    setHelpButtonText("Select your barracks and produce a total of 3 Pillagers and/or Vindicators. If you need more food " +
                                    "try building a farm or hunt more animals. If you need more population supply, try building a house.");
                    TutorialRendering.clearButtonName();
                    msg("Try building 3 units from here to continue.");
                    progressStage();
                }
                else if (stageProgress == 1) {
                    int armyCount = 0;
                    for (LivingEntity entity : UnitClientEvents.getAllUnits())
                        if (entity instanceof VindicatorUnit || entity instanceof PillagerUnit)
                            armyCount += 1;
                    if (armyCount >= 3) {
                        specialMsg("Awesome!");
                        progressStageAfterDelay(100);
                        TutorialServerboundPacket.doServerAction(TutorialAction.SPAWN_MONSTER_WORKERS);
                    }
                }
                else if (stageProgress == 2) {
                    msg("TIP: If you lose track of your military units, you can press K or click the button " +
                        "on the right to select all of them at once.");
                    nextStageAfterDelay(200);
                    TutorialServerboundPacket.doServerAction(TutorialAction.START_MONSTER_BASE);
                }
            }
            case DEFEND_BASE -> {
                if (stageProgress == 0) {
                    msg("Uh oh, looks like some monsters are about to attack!");
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_NIGHT_TIME);
                    TutorialServerboundPacket.doServerAction(TutorialAction.SPAWN_MONSTERS_A);
                    OrthoviewClientEvents.forceMoveCam(MONSTER_CAMERA_POS, 50);
                    progressStageAfterDelay(120);
                }
                if (stageProgress == 1) {
                    setHelpButtonText("With your units selected, RIGHT-CLICK an enemy to attack them. " +
                            "Units will also automatically attack nearby enemies if they are idle.");
                    msg("With your units selected, RIGHT-CLICK an enemy to attack them. " +
                            "Units will also automatically attack nearby enemies if they are idle.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 2) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.ATTACK_WITH_MONSTERS_A);
                    progressStage();
                }
                else if (stageProgress == 3) {
                    if (UnitClientEvents.getAllUnits().stream().filter(
                            u -> u instanceof PillagerUnit || u instanceof VindicatorUnit)
                            .toList().isEmpty()) {
                        progressStage();
                    }
                    if (UnitClientEvents.getAllUnits().stream().filter(
                            u -> u instanceof ZombieUnit || u instanceof SkeletonUnit)
                            .toList().isEmpty()) {
                        msg("Watch out! More monsters incoming!");
                        TutorialServerboundPacket.doServerAction(TutorialAction.SPAWN_MONSTERS_B);
                        OrthoviewClientEvents.forceMoveCam(MONSTER_CAMERA_POS, 50);
                        progressStageAfterDelay(100);
                    }
                }
                else if (stageProgress == 4) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.ATTACK_WITH_MONSTERS_B);
                    progressStageAfterDelay(200);
                }
                else if (stageProgress == 5) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.EXPAND_MONSTER_BASE_A);
                    TutorialServerboundPacket.doServerAction(TutorialAction.SET_DAY_TIME);
                    specialMsg("Dawn breaks!");
                    progressStage();
                }
                else if (stageProgress == 6) {
                    if (UnitClientEvents.getAllUnits().stream().filter(
                                    u -> u instanceof ZombieUnit || u instanceof SkeletonUnit)
                            .toList().isEmpty()) {
                        specialMsg("Nicely done, you successfully defended your base!");
                        clearHelpButtonText();
                        progressStageAfterDelay(100);
                    }
                }
                else if (stageProgress == 7) {
                    msg("TIP: During an attack, be sure to protect your workers. If a worker dies while holding " +
                        "resources, it is dropped and can be stolen by your enemies!");
                    nextStageAfterDelay(200);
                }
            }
            case REPAIR_BUILDING -> {
                if (stageProgress == 0) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.EXPAND_MONSTER_BASE_B);
                    for (Building building : BuildingClientEvents.getBuildings())
                        if (building.getHealth() < building.getMaxHealth() && building.getFaction() == Faction.VILLAGERS && damagedBuildings.size() < 3)
                            damagedBuildings.add(building);
                    if (damagedBuildings.isEmpty()) {
                        nextStage();
                    } else {
                        msg("Looks like some of your buildings were damaged in the attack.");
                        progressStageAfterDelay(100);
                    }
                }
                else if (stageProgress == 1) {
                    msg("Try repairing one of them by selecting a villager, then RIGHT-CLICKING the damaged building. " +
                            "Repairs cost 1 wood for each block.");
                    setHelpButtonText("Select a worker and RIGHT-CLICK a damaged building, then wait for it to be fully repaired. " +
                            "Repairs cost 1 wood for each block.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 2) {
                    for (Building building : damagedBuildings) {
                        if (building.getHealth() >= building.getMaxHealth()) {
                            specialMsg("Good job!");
                            clearHelpButtonText();
                            progressStageAfterDelay(100);
                            break;
                        }
                    }
                }
                else if (stageProgress == 3) {
                    msg("TIP: Building health is determined by how many blocks it's made up of. If they have less than " +
                            "half blocks remaining, they are destroyed completely.");
                    progressStageAfterDelay(200);
                }
                else if (stageProgress == 4) {
                    TutorialServerboundPacket.doServerAction(TutorialAction.SPAWN_MONSTER_BASE_ARMY);
                    nextStageAfterSpace();
                }
            }
            case BUILD_BRIDGE -> {
                if (stageProgress == 0) {
                    OrthoviewClientEvents.forceMoveCam(MONSTER_BASE_POS, 80);
                    if (FogOfWarClientEvents.isEnabled())
                        msg("The monsters have a base across the river (but you can't see it yet because " +
                            "fog of war is enabled). We should destroy it before they attack again.");
                    else
                        msg("The monsters have a base across the river, we should destroy it before they attack again.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 1) {
                    msg("To cross the river, we need to build a bridge.");
                    progressStageAfterDelay(100);
                }
                else if (stageProgress == 2) {
                    OrthoviewClientEvents.forceMoveCam(BRIDGE_POS, 50);
                    msg("This looks like a good spot for one. Select a worker and build a bridge here. " +
                        "You can scroll to rotate the bridge before placement.");
                    setHelpButtonText("Select a worker and build a bridge across the river. You can scroll to rotate the bridge. " +
                            "before placement. If the bridge is too short, you can build more segments connecting to it.");
                    progressStage();
                }
                else if (stageProgress == 3) {
                    for (Building building : BuildingClientEvents.getBuildings()) {
                        if (building instanceof OakBridge bridge) {
                            msg("TIP: You may need more than one bridge segment to cross the river. After completing " +
                                    "one you can connect new segments to it.");
                            progressStage();
                            break;
                        }
                    }
                }
                else if (stageProgress == 4) {
                    for (Building building : BuildingClientEvents.getBuildings()) {
                        if (building instanceof OakBridge bridge && bridge.isBuilt) {
                            specialMsg("Nice job.");
                            progressStageAfterDelay(100);
                            break;
                        }
                    }
                }
                else if (stageProgress == 5) {
                    msg("TIP: Bridges are always neutral regardless of who built them. This means anyone can attack, " +
                            "repair and connect new segments to them.");
                    progressStageAfterDelay(200);
                }
                else if (stageProgress == 6) {
                    msg("TIP: Be very careful when crossing bridges. If it is destroyed while your units are crossing, " +
                        "they will land in the water and be defenceless!");
                    progressStageAfterDelay(200);
                }
                else if (stageProgress == 7) {
                    nextStage();
                }
            }
            case ATTACK_ENEMY_BASE -> {
                if (stageProgress == 0) {
                    msg("Reinforcements have arrived! Use them to crush the enemy base!");
                    TutorialServerboundPacket.doServerAction(TutorialAction.SPAWN_FRIENDLY_ARMY);
                    OrthoviewClientEvents.forceMoveCam(ARMY_POS, 50);
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 1) {
                    msg("Your army includes an iron golem and your workers can now build blacksmiths to produce more if needed.");
                    setHelpButtonText("Prepare your army and destroy all buildings in the monsters' base");
                    progressStageAfterDelay(200);
                }
                else if (stageProgress == 2) {
                    msg("TIP: Most ranged units can't attack buildings. Order them to attack units instead!");
                    progressStageAfterDelay(200);
                }
                else if (stageProgress == 3) {
                    for (Building building : BuildingClientEvents.getBuildings()) {
                        if (building.getFaction() == Faction.MONSTERS && building.getHealth() < building.getMaxHealth()) {
                            msg("TIP: The monsters' capitol, the Mausoleum, produces an artificial night time around it. " +
                                    "If you can destroy it during the day, undead units will burn under the sun.");
                            progressStage();
                            break;
                        }
                    }
                }
                else if (stageProgress == 4) {
                    boolean botAlive = false;
                    for (Building building : BuildingClientEvents.getBuildings())
                        if (building.getFaction() == Faction.MONSTERS)
                            botAlive = true;

                    if (!botAlive) {
                        // should show the standard victory screen
                        progressStageAfterDelay(200);
                    }
                }
                else if (stageProgress == 5) {
                    nextStageAfterSpace();
                }
            }
            case OUTRO -> {
                if (stageProgress == 0) {
                    specialMsg("Congratulations! You have completed the tutorial!");
                    progressStageAfterDelay(100);
                }
                else if (stageProgress == 1) {
                    msg("You may now continue with all of the buildings and units unlocked.");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 2) {
                    msg("To reset the game and try a new faction, type /rts-reset");
                    progressStageAfterDelay(120);
                }
                else if (stageProgress == 3) {
                    msg("If you would like to play against another player, check out the Reign of Nether CurseForge " +
                        "page for a guide on server hosting.");
                    progressStageAfterDelay(160);
                }
                else if (stageProgress == 4) {
                    msg("Until next time... Good luck and have fun!");
                    progressStageAfterDelay(100);
                }
                else if (stageProgress == 5) {
                    specialMsg("Tutorial mode disabled");
                    nextStage();
                }
            }
            case COMPLETED -> {
                if (stageProgress == 0) {
                    setEnabled(false);
                    progressStage();
                }
            }
        }
    }
}
