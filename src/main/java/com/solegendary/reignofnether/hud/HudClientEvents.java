package com.solegendary.reignofnether.hud;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.CallToArmsUnit;
import com.solegendary.reignofnether.alliance.AllianceSystem;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientEvents;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.config.ConfigClientEvents;
import com.solegendary.reignofnether.gamemode.ClientGameModeHelper;
import com.solegendary.reignofnether.gamemode.GameMode;
import com.solegendary.reignofnether.guiscreen.TopdownGui;
import com.solegendary.reignofnether.hud.buttons.ActionButtons;
import com.solegendary.reignofnether.hud.buttons.StartButtons;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerClientEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesClientEvents;
import com.solegendary.reignofnether.sandbox.SandboxClientEvents;
import com.solegendary.reignofnether.survival.SurvivalClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.units.monsters.*;
import com.solegendary.reignofnether.unit.units.piglins.HeadhunterUnit;
import com.solegendary.reignofnether.unit.units.piglins.HoglinUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.VillagerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static com.solegendary.reignofnether.hud.buttons.HelperButtons.*;
import static com.solegendary.reignofnether.tutorial.TutorialClientEvents.helpButton;
import static com.solegendary.reignofnether.unit.UnitClientEvents.*;

public class HudClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();

    private static String tempMsg = "";
    private static int tempMsgTicksLeft = 0;
    private static final int TEMP_MSG_TICKS_FADE = 50; // ticks left when the msg starts to fade
    private static final int TEMP_MSG_TICKS_MAX = 150; // ticks to show the msg for
    private static final int MAX_BUTTONS_PER_ROW = 6;

    public static final ArrayList<ControlGroup> controlGroups = new ArrayList<>(10);
    public static int lastSelCtrlGroupKey = -1;

    private static final ArrayList<Button> buildingButtons = new ArrayList<>();
    private static final ArrayList<Button> unitButtons = new ArrayList<>();
    private static final ArrayList<Button> productionButtons = new ArrayList<>();
    // buttons which are rendered at the moment in RenderEvent
    private static final ArrayList<Button> renderedButtons = new ArrayList<>();

    // unit that is selected in the list of unit icons
    public static LivingEntity hudSelectedEntity = null;
    // building that is selected in the list of unit icons
    public static Building hudSelectedBuilding = null;
    // classes used to render unit or building portrait (mode, frame, healthbar, stats)
    public static PortraitRendererUnit portraitRendererUnit = new PortraitRendererUnit();
    public static PortraitRendererBuilding portraitRendererBuilding = new PortraitRendererBuilding();

    private static RectZone unitPortraitZone = null;
    private static RectZone buildingPortraitZone = null;

    public static int mouseX = 0;
    public static int mouseY = 0;
    private static int mouseLeftDownX = 0;
    private static int mouseLeftDownY = 0;

    private final static int iconBgColour = 0x64000000;
    private final static int frameBgColour = 0xA0000000;

    private static final ArrayList<RectZone> hudZones = new ArrayList<>();

    public static void setLowestCdHudEntity() {
        if (UnitClientEvents.getSelectedUnits().isEmpty() || hudSelectedEntity == null) {
            return;
        }

        List<Pair<LivingEntity, Float>> pairs = UnitClientEvents.getSelectedUnits().stream().map((le) -> {
            float totalCd = 0;
            if (le instanceof Unit unit) {
                for (Ability ability : unit.getAbilities()) {
                    totalCd += ability.getCooldown();
                    if (ability.isChanneling())
                        totalCd += 10;
                }
            }
            return new Pair<>(le, totalCd);
        }).filter(p -> {
            String str1 = getModifiedEntityName(p.getFirst());
            String str2 = getModifiedEntityName(hudSelectedEntity);
            return str1.equals(str2);
        }).sorted(Comparator.comparing(Pair::getSecond)).toList();

        if (!pairs.isEmpty())
            setHudSelectedEntity(pairs.get(0).getFirst());
    }

    public static void setHudSelectedEntity(LivingEntity entity) {
        hudSelectedEntity = entity;
    }

    // eg. entity.reignofnether.zombie_unit -> zombie
    public static String getSimpleEntityName(Entity entity) {
        if (entity instanceof Unit) {
            if (entity.hasCustomName()) {
                return entity.getType()
                    .getDescription()
                    .getString()
                    .replace(" ", "")
                    .replace("entity.reignofnether.", "")
                    .replace("_unit", "")
                    .replace(".none", "");
            } else {
                return entity.getName()
                    .getString()
                    .replace(" ", "")
                    .replace("entity.reignofnether.", "")
                    .replace("_unit", "")
                    .replace(".none", "");
            }
        } else {
            return entity.getName().getString();
        }
    }

    // not to be used for resource paths
    public static String getModifiedEntityName(Entity entity) {
        String name = getSimpleEntityName(entity);

        ItemStack itemStack = ((LivingEntity) entity).getItemBySlot(EquipmentSlot.HEAD);

        if (itemStack.getItem() instanceof BannerItem) {
            name += " " + I18n.get("units.villagers.reignofnether.captain");
        }
        if (entity.getPassengers().size() == 1) {
            Entity passenger = entity.getPassengers().get(0);
            if (entity instanceof RavagerUnit && passenger instanceof PillagerUnit) {
                name = I18n.get("units.villagers.reignofnether.ravager_artillery");
            } else if (entity instanceof PoisonSpiderUnit && (
                    passenger instanceof SkeletonUnit || passenger instanceof StrayUnit
            )) {
                name = I18n.get("units.monsters.reignofnether.poison_spider_jockey");
            } else if (entity instanceof SpiderUnit && (
                passenger instanceof SkeletonUnit || passenger instanceof StrayUnit
            )) {
                name = I18n.get("units.monsters.reignofnether.spider_jockey");
            }else if (entity instanceof HoglinUnit && passenger instanceof HeadhunterUnit) {
                name = I18n.get("units.piglins.reignofnether.hoglin_rider");
            } else {
                String pName = getSimpleEntityName(entity.getPassengers().get(0)).replace("_", " ");
                String nameCap = pName.substring(0, 1).toUpperCase() + pName.substring(1);
                name += " & " + nameCap;
            }
        }
        if (entity instanceof VillagerUnit vUnit) {
            switch (vUnit.getUnitProfession()) {
                case FARMER -> {
                    if (vUnit.isVeteran())
                        name = I18n.get("units.reignofnether.veteran_farmer");
                    else
                        name = I18n.get("units.reignofnether.farmer");
                }
                case LUMBERJACK -> {
                    if (vUnit.isVeteran())
                        name = I18n.get("units.reignofnether.veteran_lumberjack");
                    else
                        name = I18n.get("units.reignofnether.lumberjack");
                }
                case MINER -> {
                    if (vUnit.isVeteran())
                        name = I18n.get("units.reignofnether.veteran_miner");
                    else
                        name = I18n.get("units.reignofnether.miner");
                }
                case MASON -> {
                    if (vUnit.isVeteran())
                        name = I18n.get("units.reignofnether.veteran_mason");
                    else
                        name = I18n.get("units.reignofnether.mason");
                }
                case HUNTER -> {
                    if (vUnit.isVeteran())
                        name = I18n.get("units.reignofnether.veteran_hunter");
                    else
                        name = I18n.get("units.reignofnether.hunter");
                }
                default -> name = I18n.get("units.villagers.reignofnether.villager");
            }
        }
        if (entity instanceof CreeperUnit cUnit && cUnit.isPowered()) {
            name = I18n.get("units.monsters.reignofnether.charged_creeper");
        }
        return name;
    }

    public static void showTemporaryMessage(String msg) {
        showTemporaryMessage(msg, TEMP_MSG_TICKS_MAX);
    }

    public static void showTemporaryMessage(String msg, int ticks) {
        tempMsgTicksLeft = ticks;
        tempMsg = msg;
    }

    public static void removeFromControlGroups(int entityId) {
        for (ControlGroup controlGroup : controlGroups)
            controlGroup.entityIds.removeIf(id -> id == entityId);
    }

    @SubscribeEvent
    public static void onDrawScreen(ScreenEvent.Render.Post evt) {
        if (!OrthoviewClientEvents.isEnabled() || !(evt.getScreen() instanceof TopdownGui)) {
            return;
        }
        if (MC.level == null) {
            return;
        }

        mouseX = evt.getMouseX();
        mouseY = evt.getMouseY();

        // where to start drawing the centre hud (from left to right: portrait, stats, unit icon buttons)
        int hudStartingXPos = Button.iconFrameSize * 6 + (Button.iconFrameSize / 2);

        ArrayList<LivingEntity> selUnits = UnitClientEvents.getSelectedUnits();
        ArrayList<Building> selBuildings = BuildingClientEvents.getSelectedBuildings();

        // create all the unit buttons for this frame
        int screenWidth = MC.getWindow().getGuiScaledWidth();
        int screenHeight = MC.getWindow().getGuiScaledHeight();

        int iconSize = 14;
        int iconFrameSize = Button.iconFrameSize;

        // screenWidth ranges roughly between 440-540
        int buttonsPerRow = (int) Math.ceil((float) (screenWidth - 340) / iconFrameSize);
        buttonsPerRow = Math.min(buttonsPerRow, 8);
        buttonsPerRow = Math.max(buttonsPerRow, 4);

        buildingButtons.clear();
        unitButtons.clear();
        productionButtons.clear();
        renderedButtons.clear();
        hudZones.clear();
        unitPortraitZone = null;
        buildingPortraitZone = null;

        int blitX = hudStartingXPos;
        int blitY = MC.getWindow().getGuiScaledHeight();
        int blitXStart = blitX;

        // assign hudSelectedBuilding like hudSelectedUnit in onRenderLiving
        if (selBuildings.size() <= 0) {
            hudSelectedBuilding = null;
        } else if (hudSelectedBuilding == null || selBuildings.size() == 1
            || !selBuildings.contains(hudSelectedBuilding)) {
            hudSelectedBuilding = selBuildings.get(0);
        }

        if (hudSelectedBuilding != null) {
            boolean hudSelBuildingOwned =
                BuildingClientEvents.getPlayerToBuildingRelationship(hudSelectedBuilding) == Relationship.OWNED;

            // -----------------
            // Building portrait
            // -----------------
            blitY -= portraitRendererBuilding.frameHeight;

            buildingPortraitZone = portraitRendererBuilding.render(evt.getPoseStack(),
                blitX,
                blitY,
                hudSelectedBuilding
            );
            hudZones.add(buildingPortraitZone);

            blitX += portraitRendererBuilding.frameWidth + 10;

            blitXStart = blitX + 20;


            // ---------------------------
            // Multiple selected buildings
            // ---------------------------
            for (Building building : selBuildings) {
                if (hudSelBuildingOwned && buildingButtons.size() < (buttonsPerRow * 2)) {
                    // mob head icon

                    buildingButtons.add(new Button(building.name,
                        iconSize,
                        building.icon,
                        building,
                        () -> hudSelectedBuilding.name.equals(building.name),
                        () -> false,
                        () -> true,
                        () -> {
                            // click to select this unit type as a group
                            if (hudSelectedBuilding.name.equals(building.name)) {
                                BuildingClientEvents.clearSelectedBuildings();
                                BuildingClientEvents.addSelectedBuilding(building);
                            } else { // select this one specific unit
                                hudSelectedBuilding = building;
                            }
                        },
                        null,
                        null
                    ));
                }
            }

            if (buildingButtons.size() >= 2) {
                blitX += 20;
                blitY += 6;
                // background frame
                hudZones.add(MyRenderer.renderFrameWithBg(evt.getPoseStack(),
                    blitX - 5,
                    blitY - 10,
                    iconFrameSize * buttonsPerRow + 10,
                    iconFrameSize * 2 + 20,
                    frameBgColour
                ));

                int buttonsRendered = 0;
                for (Button buildingButton : buildingButtons) {
                    // replace last icon with a +X number of buildings icon and hover tooltip for what those
                    // buildings are
                    if (buttonsRendered >= (buttonsPerRow * 2) - 1 && selBuildings.size() > (buttonsPerRow * 2)) {
                        int numExtraBuildings = selBuildings.size() - (buttonsPerRow * 2) + 1;
                        RectZone plusBuildingsZone = MyRenderer.renderIconFrameWithBg(evt.getPoseStack(),
                            buildingButton.frameResource,
                            blitX,
                            blitY,
                            iconFrameSize,
                            iconBgColour
                        );
                        GuiComponent.drawCenteredString(evt.getPoseStack(),
                            MC.font,
                            "+" + numExtraBuildings,
                            blitX + iconFrameSize / 2,
                            blitY + 8,
                            0xFFFFFF
                        );

                        if (plusBuildingsZone.isMouseOver(mouseX, mouseY)) {
                            List<FormattedCharSequence> tooltipLines = new ArrayList<>();
                            int numBuildings = 0;

                            for (int i = selBuildings.size() - numExtraBuildings; i < selBuildings.size(); i++) {
                                Building building = selBuildings.get(i);
                                Building nextBuilding = null;
                                String buildingName = building.name;

                                String nextBuildingName = null;
                                numBuildings += 1;

                                if (i < selBuildings.size() - 1) {
                                    nextBuilding = selBuildings.get(i + 1);
                                    nextBuildingName = nextBuilding.name;
                                }
                                if (!buildingName.equals(nextBuildingName)) {
                                    tooltipLines.add(FormattedCharSequence.forward("x" + numBuildings + " " + I18n.get(buildingName),
                                        Style.EMPTY
                                    ));
                                    numBuildings = 0;
                                }
                            }
                            MyRenderer.renderTooltip(evt.getPoseStack(), tooltipLines, mouseX, mouseY);
                        }
                        break;
                    } else {
                        buildingButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                        renderedButtons.add(buildingButton);
                        buildingButton.renderHealthBar(evt.getPoseStack());
                        blitX += iconFrameSize;
                        if (buttonsRendered == buttonsPerRow - 1) {
                            blitX = blitXStart;
                            blitY += iconFrameSize + 6;
                        }
                    }
                    buttonsRendered += 1;
                }
            }

            // ---------------------------------------------------------------
            // Building production queue (show only if 1 building is selected)
            // ---------------------------------------------------------------
            else if ((hudSelBuildingOwned || !PlayerClientEvents.isRTSPlayer) && hudSelectedBuilding instanceof ProductionBuilding selProdBuilding) {
                blitY = screenHeight - iconFrameSize * 2 - 5;

                for (int i = 0; i < selProdBuilding.productionQueue.size(); i++) {

                    Button button = selProdBuilding.productionQueue.get(i)
                            .getCancelButton(selProdBuilding, i == 0);
                    if (!hudSelBuildingOwned) {
                        button.onLeftClick = () -> { };
                        button.onRightClick = () -> { };
                    }
                    productionButtons.add(button);
                }

                if (productionButtons.size() >= 1) {
                    // background frame
                    hudZones.add(MyRenderer.renderFrameWithBg(evt.getPoseStack(),
                        blitX - 5,
                        blitY - 10,
                        iconFrameSize * buttonsPerRow + 10,
                        iconFrameSize * 2 + 15,
                        frameBgColour
                    ));

                    // name and progress %
                    ProductionItem firstProdItem = selProdBuilding.productionQueue.get(0);
                    float percentageDoneInv = firstProdItem.ticksLeft / firstProdItem.ticksToProduce;

                    int colour = 0xFFFFFF;
                    if (!firstProdItem.isBelowPopulationSupply()) {
                        colour = 0xFF0000;
                        if (percentageDoneInv <= 0) {
                            percentageDoneInv = 0.01f;
                        }
                    }
                    GuiComponent.drawString(evt.getPoseStack(),
                        MC.font,
                        Math.round(100 - (percentageDoneInv * 100f)) + "% " + productionButtons.get(0).name,
                        blitX + iconFrameSize + 5,
                        blitY + 2,
                        colour
                    );

                    int buttonsRendered = 0;
                    for (Button prodButton : productionButtons) {
                        // top row for currently-in-progress item
                        if (buttonsRendered == 0) {
                            prodButton.greyPercent = 1 - percentageDoneInv;
                            prodButton.render(evt.getPoseStack(), blitX, blitY - 5, mouseX, mouseY);
                            renderedButtons.add(prodButton);
                        }
                        // replace last icon with a +X number of production items left in queue
                        else if (buttonsRendered >= buttonsPerRow && productionButtons.size() > (buttonsPerRow + 1)) {
                            int numExtraItems = productionButtons.size() - buttonsPerRow;
                            MyRenderer.renderIconFrameWithBg(evt.getPoseStack(),
                                prodButton.frameResource,
                                blitX,
                                blitY + iconFrameSize,
                                iconFrameSize,
                                iconBgColour
                            );
                            GuiComponent.drawCenteredString(evt.getPoseStack(),
                                MC.font,
                                "+" + numExtraItems,
                                blitX + iconFrameSize / 2,
                                blitY + iconFrameSize + 8,
                                0xFFFFFF
                            );
                            break;
                        }
                        // bottom row for all other queued items
                        else {
                            prodButton.render(evt.getPoseStack(), blitX, blitY + iconFrameSize, mouseX, mouseY);
                            renderedButtons.add(prodButton);
                            blitX += iconFrameSize;
                        }
                        buttonsRendered += 1;
                    }
                }
            }


            // ---------------------------
            // Building production buttons
            // ---------------------------
            blitX = 0;
            blitY = screenHeight - iconFrameSize;

            if (hudSelectedBuilding != null && (hudSelBuildingOwned) && !hudSelectedBuilding.isBuilt) {
                if (!buildingCancelButton.isHidden.get()) {
                    buildingCancelButton.render(evt.getPoseStack(), 0, screenHeight - iconFrameSize, mouseX, mouseY);
                    renderedButtons.add(buildingCancelButton);
                }
            } else if (hudSelBuildingOwned) {

                List<AbilityButton> buildingAbilities = List.of();
                if (hudSelectedBuilding != null) {
                    buildingAbilities = hudSelectedBuilding.getAbilityButtons()
                        .stream()
                        .filter(b -> !b.isHidden.get())
                        .toList();
                }
                if (buildingAbilities.size() > 0) {
                    blitY -= Button.iconFrameSize;
                }

                // production buttons on bottom row
                if (hudSelectedBuilding instanceof ProductionBuilding selProdBuilding) {
                    List<Button> visibleProdButtons = selProdBuilding.productionButtons.stream()
                        .filter(b -> !b.isHidden.get())
                        .toList();
                    if (visibleProdButtons.size() > MAX_BUTTONS_PER_ROW) {
                        blitY -= Button.iconFrameSize;
                    }

                    int rowButtons = 0;
                    for (Button prodButton : visibleProdButtons) {
                        rowButtons += 1;
                        if (rowButtons > MAX_BUTTONS_PER_ROW) {
                            rowButtons = 0;
                            blitX = 0;
                            blitY += Button.iconFrameSize;
                        }
                        prodButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                        productionButtons.add(prodButton);
                        renderedButtons.add(prodButton);
                        blitX += iconFrameSize;
                    }
                }
                blitY += Button.iconFrameSize;
                blitX = 0;
                for (AbilityButton abilityButton : buildingAbilities) {
                    if (!abilityButton.isHidden.get()) {
                        abilityButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                        renderedButtons.add(abilityButton);
                        blitX += iconFrameSize;
                    }
                }
            }
        }

        // --------------------------
        // Unit head portrait + stats
        // --------------------------
        else if (hudSelectedEntity != null && portraitRendererUnit.model != null
            && portraitRendererUnit.renderer != null) {

            blitY -= portraitRendererUnit.frameHeight;

            // write capitalised unit name
            String name = getModifiedEntityName(hudSelectedEntity).replace("_", " ");
            if (hudSelectedEntity.hasCustomName()) {
                name = hudSelectedEntity.getCustomName().getString();
            }

            String nameCap = name.substring(0, 1).toUpperCase() + name.substring(1);

            unitPortraitZone = portraitRendererUnit.render(evt.getPoseStack(),
                nameCap,
                blitX,
                blitY,
                hudSelectedEntity
            );
            hudZones.add(unitPortraitZone);

            blitX += portraitRendererUnit.frameWidth;

            if (hudSelectedEntity instanceof Unit unit) {
                hudZones.add(portraitRendererUnit.renderStats(evt.getPoseStack(), nameCap, blitX, blitY, unit));

                blitX += portraitRendererUnit.statsWidth;

                int totalRes = Resources.getTotalResourcesFromItems(unit.getItems()).getTotalValue();

                if (hudSelectedEntity instanceof Mob mob && mob.canPickUpLoot() && totalRes > 0) {
                    hudZones.add(portraitRendererUnit.renderResourcesHeld(evt.getPoseStack(),
                        nameCap,
                        blitX,
                        blitY,
                        unit
                    ));

                    // return button
                    if (getPlayerToEntityRelationship(hudSelectedEntity) == Relationship.OWNED) {
                        Button returnButton = new Button("Return resources",
                            Button.itemIconSize,
                            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/chest.png"),
                            Keybindings.keyD,
                            () -> unit.getReturnResourcesGoal().getBuildingTarget() != null,
                            () -> false,
                            () -> true,
                            () -> sendUnitCommand(UnitAction.RETURN_RESOURCES_TO_CLOSEST),
                            null,
                            List.of(FormattedCharSequence.forward(I18n.get("hud.reignofnether.drop_off_resources"),
                                Style.EMPTY
                            ))
                        );
                        returnButton.render(evt.getPoseStack(), blitX + 10, blitY + 38, mouseX, mouseY);
                        renderedButtons.add(returnButton);
                    }
                }
            }
            if (hudSelectedEntity instanceof Unit unit
                && Resources.getTotalResourcesFromItems(unit.getItems()).getTotalValue() > 0) {
                blitX += portraitRendererUnit.statsWidth + 5;
            } else {
                blitX += 15;
            }
        }

        // ----------------------------------------------
        // Unit icons to select types and show healthbars
        // ----------------------------------------------
        blitXStart = blitX;
        blitY = screenHeight - iconFrameSize * 2 - 10;

        for (LivingEntity unit : selUnits) {
            if (getPlayerToEntityRelationship(unit) == Relationship.OWNED && unitButtons.size() < (buttonsPerRow * 2)) {
                // mob head icon
                String unitName = getSimpleEntityName(unit);
                String buttonImagePath;

                if (unit.isVehicle()) {
                    buttonImagePath = "textures/mobheads/" + unitName + "_half.png";
                } else {
                    buttonImagePath = "textures/mobheads/" + unitName + ".png";
                }

                Button button = new Button(unitName,
                    iconSize,
                    new ResourceLocation(ReignOfNether.MOD_ID, buttonImagePath),
                    unit,
                    () -> hudSelectedEntity == null || getModifiedEntityName(hudSelectedEntity).equals(
                        getModifiedEntityName(unit)),
                    () -> false,
                    () -> true,
                    () -> {
                        // select this one specific unit
                        if (getModifiedEntityName(hudSelectedEntity).equals(getModifiedEntityName(unit))) {
                            UnitClientEvents.clearSelectedUnits();
                            UnitClientEvents.addSelectedUnit(unit);
                        } else { // click to select this unit type as a group
                            HudClientEvents.setHudSelectedEntity(unit);
                        }
                    },
                    null,
                    null
                );
                if (unit.isVehicle()) {
                    String passengerName = getSimpleEntityName(unit.getFirstPassenger());
                    button.bgIconResource = new ResourceLocation(ReignOfNether.MOD_ID,
                        "textures/mobheads/" + passengerName + ".png"
                    );
                }
                unitButtons.add(button);
            }
        }

        if (unitButtons.size() >= 2) {
            // background frame
            hudZones.add(MyRenderer.renderFrameWithBg(evt.getPoseStack(),
                blitX - 5,
                blitY - 10,
                iconFrameSize * buttonsPerRow + 10,
                iconFrameSize * 2 + 20,
                frameBgColour
            ));

            int buttonsRendered = 0;
            for (Button unitButton : unitButtons) {
                // replace last icon with a +X number of units icon and hover tooltip for what those units are
                if (buttonsRendered >= (buttonsPerRow * 2) - 1 && selUnits.size() > (buttonsPerRow * 2)) {
                    int numExtraUnits = selUnits.size() - (buttonsPerRow * 2) + 1;
                    RectZone plusUnitsZone = MyRenderer.renderIconFrameWithBg(evt.getPoseStack(),
                        unitButton.frameResource,
                        blitX,
                        blitY,
                        iconFrameSize,
                        iconBgColour
                    );
                    GuiComponent.drawCenteredString(evt.getPoseStack(),
                        MC.font,
                        "+" + numExtraUnits,
                        blitX + iconFrameSize / 2,
                        blitY + 8,
                        0xFFFFFF
                    );

                    if (plusUnitsZone.isMouseOver(mouseX, mouseY)) {
                        List<FormattedCharSequence> tooltipLines = new ArrayList<>();
                        int numUnits = 0;

                        for (int i = selUnits.size() - numExtraUnits; i < selUnits.size(); i++) {

                            LivingEntity unit = selUnits.get(i);
                            LivingEntity nextUnit = null;
                            String unitName = HudClientEvents.getSimpleEntityName(unit);
                            String nextUnitName = null;
                            numUnits += 1;

                            if (i < selUnits.size() - 1) {
                                nextUnit = selUnits.get(i + 1);
                                nextUnitName = HudClientEvents.getSimpleEntityName(nextUnit);
                            }
                            if (!unitName.equals(nextUnitName)) {
                                tooltipLines.add(FormattedCharSequence.forward("x" + numUnits + " " + I18n.get(unitName),
                                    Style.EMPTY
                                ));
                                numUnits = 0;
                            }
                        }
                        MyRenderer.renderTooltip(evt.getPoseStack(), tooltipLines, mouseX, mouseY);
                    }
                    break;
                } else {
                    unitButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                    renderedButtons.add(unitButton);
                    unitButton.renderHealthBar(evt.getPoseStack());
                    blitX += iconFrameSize;
                    if (buttonsRendered == buttonsPerRow - 1) {
                        blitX = blitXStart;
                        blitY += iconFrameSize + 6;
                    }
                }
                buttonsRendered += 1;
            }
        }

        // --------------------------------------------------------
        // Unit action buttons (attack, stop, move, abilities etc.)
        // --------------------------------------------------------
        if (selUnits.size() > 0 && getPlayerToEntityRelationship(selUnits.get(0)) == Relationship.OWNED
                && hudSelectedEntity instanceof Unit unit) {
            blitX = 0;
            blitY = screenHeight - iconFrameSize;

            ArrayList<Button> actionButtons = new ArrayList<>();

            if (hudSelectedEntity instanceof AttackerUnit) {
                actionButtons.add(ActionButtons.ATTACK);
            }
            if (hudSelectedEntity instanceof WorkerUnit) {
                actionButtons.add(ActionButtons.BUILD_REPAIR);
                actionButtons.add(ActionButtons.GATHER);
            }
            if (unit.canGarrison() && GarrisonableBuilding.getGarrison(unit) == null) {
                actionButtons.add(ActionButtons.GARRISON);
            } else if (GarrisonableBuilding.getGarrison(unit) != null) {
                actionButtons.add(ActionButtons.UNGARRISON);
            }

            if (!(hudSelectedEntity instanceof WorkerUnit)) {
                actionButtons.add(ActionButtons.HOLD);
            }
            actionButtons.add(ActionButtons.STOP);

            if (hudSelectedEntity instanceof VillagerUnit vUnit)
                for (Ability ability : vUnit.getAbilities())
                    if (ability instanceof CallToArmsUnit callToArmsUnit)
                        actionButtons.add(callToArmsUnit.getButton(Keybindings.keyV));

            for (Button actionButton : actionButtons) {
                // GATHER button does not have a static icon
                if (actionButton == ActionButtons.GATHER && hudSelectedEntity instanceof WorkerUnit workerUnit) {
                    switch (workerUnit.getGatherResourceGoal().getTargetResourceName()) {
                        case NONE -> actionButton.iconResource = new ResourceLocation(ReignOfNether.MOD_ID,
                                "textures/icons/items/no_gather.png"
                        );
                        case FOOD -> actionButton.iconResource = new ResourceLocation(ReignOfNether.MOD_ID,
                                "textures/icons/items/hoe.png"
                        );
                        case WOOD -> actionButton.iconResource = new ResourceLocation(ReignOfNether.MOD_ID,
                                "textures/icons/items/axe.png"
                        );
                        case ORE -> actionButton.iconResource = new ResourceLocation(ReignOfNether.MOD_ID,
                                "textures/icons/items/pickaxe.png"
                        );
                    }
                    String resourceName = UnitClientEvents.getSelectedUnitResourceTarget().toString();
                    String key = String.format("resources.reignofnether.%s", resourceName.toLowerCase(Locale.ENGLISH));
                    actionButton.tooltipLines = List.of(
                            FormattedCharSequence.forward(I18n.get("hud.reignofnether" + ".gather_resources",
                                    I18n.get(key)
                            ), Style.EMPTY),
                            FormattedCharSequence.forward(I18n.get("hud.reignofnether.change_target_resource"), Style.EMPTY)
                    );
                }
                actionButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                renderedButtons.add(actionButton);
                blitX += iconFrameSize;
            }
            blitX = 0;
            blitY = screenHeight - (iconFrameSize * 2) - 4;

            // includes worker building buttons
            if (TutorialClientEvents.isAtOrPastStage(TutorialStage.BUILD_INTRO)) {
                List<AbilityButton> abilityButtons = List.of();
                for (LivingEntity livingEntity : selUnits) {
                    if (livingEntity == hudSelectedEntity) {
                        abilityButtons = ((Unit) livingEntity).getAbilityButtons();
                        break;
                    }
                }
                List<AbilityButton> shownAbilities = abilityButtons.stream()
                        .filter(ab -> !ab.isHidden.get() && !(ab.ability instanceof CallToArmsUnit))
                        .toList();

                int rowsUp = (int) Math.floor((float) (shownAbilities.size() - 1) / MAX_BUTTONS_PER_ROW);
                rowsUp = Math.max(0, rowsUp);
                blitY -= iconFrameSize * rowsUp;

                int i = 0;
                for (AbilityButton abilityButton : shownAbilities) {
                    if (!abilityButton.isHidden.get()) {
                        i += 1;
                        abilityButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                        renderedButtons.add(abilityButton);
                        blitX += iconFrameSize;
                        if (i % MAX_BUTTONS_PER_ROW == 0) {
                            blitX = 0;
                            blitY += iconFrameSize;
                        }
                    }
                }
            }
        }
        else if (PlayerClientEvents.isSandbox() && selUnits.isEmpty() && selBuildings.isEmpty()) {
            blitX = 0;
            blitY = screenHeight - iconFrameSize;

            ArrayList<Button> actionButtons = new ArrayList<>();
            actionButtons.add(SandboxClientEvents.getToggleBuildingFactionButton());
            actionButtons.add(SandboxClientEvents.getToggleBuildingCheatsButton());

            for (Button actionButton : actionButtons) {
                actionButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                renderedButtons.add(actionButton);
                blitX += iconFrameSize;
            }
            blitX = 0;
            blitY = screenHeight - (iconFrameSize * 2) - 4;

            List<AbilityButton> abilityButtons = SandboxClientEvents.getBuildingButtons();
            List<AbilityButton> shownAbilities = abilityButtons.stream()
                    .filter(ab -> !ab.isHidden.get() && !(ab.ability instanceof CallToArmsUnit))
                    .toList();

            int rowsUp = (int) Math.floor((float) (shownAbilities.size() - 1) / MAX_BUTTONS_PER_ROW);
            rowsUp = Math.max(0, rowsUp);
            blitY -= iconFrameSize * rowsUp;

            int i = 0;
            for (AbilityButton abilityButton : shownAbilities) {
                if (!abilityButton.isHidden.get()) {
                    i += 1;
                    abilityButton.render(evt.getPoseStack(), blitX, blitY, mouseX, mouseY);
                    renderedButtons.add(abilityButton);
                    blitX += iconFrameSize;
                    if (i % MAX_BUTTONS_PER_ROW == 0) {
                        blitX = 0;
                        blitY += iconFrameSize;
                    }
                }
            }
        }

        // ---------------------------
        // Resources icons and amounts
        // ---------------------------
        Resources resources = null;
        String selPlayerName = null;

        if (!UnitClientEvents.getSelectedUnits().isEmpty()) {
            if (UnitClientEvents.getSelectedUnits().get(0) instanceof Unit unit) {
                selPlayerName = unit.getOwnerName();
            }
        }
        if (!BuildingClientEvents.getSelectedBuildings().isEmpty()) {
            selPlayerName = BuildingClientEvents.getSelectedBuildings().get(0).ownerName;
        }

        if (selPlayerName != null) {
            resources = ResourcesClientEvents.getResources(selPlayerName);
        }
        boolean alliedWithSelPlayer = MC.player != null && AllianceSystem.isAllied(MC.player.getName().getString(), selPlayerName);
        boolean isSelPlayer = MC.player != null && MC.player.getName().getString().equals(selPlayerName);

        blitX = 0;
        blitY = 0;

        if ((!PlayerClientEvents.isRTSPlayer || alliedWithSelPlayer) && !isSelPlayer) {
            if (resources != null) {
                GuiComponent.drawString(evt.getPoseStack(),
                    MC.font,
                    I18n.get("hud.reignofnether.players_resources", selPlayerName),
                    blitX + 5,
                    blitY + 5,
                    0xFFFFFF
                );
            } else if (!TutorialClientEvents.isEnabled()) {
                GuiComponent.drawString(evt.getPoseStack(),
                    MC.font,
                    I18n.get("hud.reignofnether.you_are_spectator"),
                    blitX + 5,
                    blitY + 5,
                    0xFFFFFF
                );
                blitY += 10;
            }
            blitY += 20;
        }

        int resourceBlitYStart = blitY;

        if (resources != null) {
            for (String resourceName : new String[] { "food", "wood", "ore", "pop" }) {
                String rlPath = "";
                String resValueStr = "";
                ResourceName resName;

                List<FormattedCharSequence> tooltip;

                switch (resourceName) {
                    case "food" -> {
                        rlPath = "textures/icons/items/wheat.png";
                        resValueStr = String.valueOf(resources.food);
                        resName = ResourceName.FOOD;
                    }
                    case "wood" -> {
                        rlPath = "textures/icons/items/wood.png";
                        resValueStr = String.valueOf(resources.wood);
                        resName = ResourceName.WOOD;
                    }
                    case "ore" -> {
                        rlPath = "textures/icons/items/iron_ore.png";
                        resValueStr = String.valueOf(resources.ore);
                        resName = ResourceName.ORE;
                    }
                    default -> {
                        rlPath = "textures/icons/items/bed.png";
                        resValueStr = UnitClientEvents.getCurrentPopulation(selPlayerName) + "/"
                            + BuildingClientEvents.getTotalPopulationSupply(selPlayerName);
                        resName = ResourceName.NONE;
                    }
                }
                hudZones.add(MyRenderer.renderFrameWithBg(evt.getPoseStack(),
                    blitX + iconFrameSize - 1,
                    blitY,
                    49,
                    iconFrameSize,
                    frameBgColour
                ));

                hudZones.add(MyRenderer.renderIconFrameWithBg(evt.getPoseStack(),
                    new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame.png"),
                    blitX,
                    blitY,
                    iconFrameSize,
                    iconBgColour
                ));

                MyRenderer.renderIcon(evt.getPoseStack(),
                    new ResourceLocation(ReignOfNether.MOD_ID, rlPath),
                    blitX + 4,
                    blitY + 4,
                    iconSize
                );
                GuiComponent.drawCenteredString(evt.getPoseStack(),
                    MC.font,
                    resValueStr,
                    blitX + (iconFrameSize) + 24,
                    blitY + (iconSize / 2) + 1,
                    0xFFFFFF
                );

                // worker count assigned to each resource
                String finalSelPlayerName = selPlayerName;

                int numWorkersHunting = UnitClientEvents.getAllUnits()
                    .stream()
                    .filter(le -> le instanceof WorkerUnit wu && le instanceof Unit u && u.getOwnerName()
                        .equals(finalSelPlayerName) && ResourceSources.isHuntableAnimal(u.getTargetGoal().getTarget()))
                    .toList()
                    .size();

                int numWorkersAssigned = 0;
                // we can only see ReturnResourcesGoal data on server, so we can't use that here
                if (resName == ResourceName.NONE) {
                    numWorkersAssigned = UnitClientEvents.getAllUnits()
                        .stream()
                        .filter(u -> u instanceof WorkerUnit
                            && UnitClientEvents.getPlayerToEntityRelationship(u) == Relationship.OWNED)
                        .toList()
                        .size();
                } else {
                    for (LivingEntity le : UnitClientEvents.getAllUnits()) {
                        if (le instanceof Unit u && le instanceof WorkerUnit wu && u.getOwnerName()
                            .equals(finalSelPlayerName) && !UnitClientEvents.idleWorkerIds.contains(le.getId())) {

                            boolean alreadyAssigned = false;

                            if (u.getReturnResourcesGoal() != null) {
                                Resources res = Resources.getTotalResourcesFromItems(u.getItems());
                                if (resName == ResourceName.FOOD && res.food > 0
                                    || resName == ResourceName.WOOD && res.wood > 0
                                    || resName == ResourceName.ORE && res.ore > 0) {
                                    numWorkersAssigned += 1;
                                    alreadyAssigned = true;
                                }
                            }
                            if (!alreadyAssigned && wu.getGatherResourceGoal()
                                .getTargetResourceName()
                                .equals(resName)) {
                                numWorkersAssigned += 1;
                            }
                        }
                    }
                }
                if (resName == ResourceName.FOOD) {
                    numWorkersAssigned += numWorkersHunting;
                }

                hudZones.add(MyRenderer.renderIconFrameWithBg(evt.getPoseStack(),
                        new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame.png"),
                        blitX + 69,
                        blitY,
                        iconFrameSize,
                        iconBgColour
                ));

                GuiComponent.drawCenteredString(evt.getPoseStack(),
                        MC.font,
                        String.valueOf(numWorkersAssigned),
                        blitX + 69 + (iconFrameSize / 2),
                        blitY + (iconSize / 2) + 1,
                        0xFFFFFF
                );

                blitY += iconFrameSize - 1;
            }

            blitY = resourceBlitYStart;
            for (String resourceName : new String[] { "food", "wood", "ore", "population" }) {
                String locName = I18n.get("resources.reignofnether." + resourceName);
                List<FormattedCharSequence> tooltip;
                String key = String.format("resources.reignofnether.%s", resourceName);
                if (resourceName.equals("population")) {
                    tooltip = List.of(FormattedCharSequence.forward(I18n.get("hud.reignofnether.max_resources",
                        I18n.get(key),
                        maxPopulation
                    ), Style.EMPTY));
                } else {
                    tooltip = List.of(FormattedCharSequence.forward(I18n.get(key), Style.EMPTY));
                }
                if (mouseX >= blitX && mouseY >= blitY && mouseX < blitX + iconFrameSize
                    && mouseY < blitY + iconFrameSize) {
                    MyRenderer.renderTooltip(evt.getPoseStack(), tooltip, mouseX + 5, mouseY);
                }
                if (mouseX >= blitX + 69 && mouseY >= blitY && mouseX < blitX + 69 + iconFrameSize
                    && mouseY < blitY + iconFrameSize) {
                    List<FormattedCharSequence> tooltipWorkersAssigned;
                    if (resourceName.equals("population")) {
                        int numWorkers = UnitClientEvents.getAllUnits()
                            .stream()
                            .filter(u -> u instanceof WorkerUnit
                                && UnitClientEvents.getPlayerToEntityRelationship(u) == Relationship.OWNED)
                            .toList()
                            .size();
                        tooltipWorkersAssigned =
                            List.of(FormattedCharSequence.forward(I18n.get("hud.reignofnether.total_workers",
                            numWorkers
                        ), Style.EMPTY));
                    } else {
                        tooltipWorkersAssigned =
                            List.of(FormattedCharSequence.forward(I18n.get("hud.reignofnether.workers_on_" + resourceName
                        ), Style.EMPTY));
                    }
                    MyRenderer.renderTooltip(evt.getPoseStack(), tooltipWorkersAssigned, mouseX + 5, mouseY);

                }
                blitY += iconFrameSize - 1;
            }
        }

        // --------------------------
        // Temporary warning messages
        // --------------------------
        if (tempMsgTicksLeft > 0 && tempMsg.length() > 0) {
            int ticksUnderFade = Math.min(tempMsgTicksLeft, TEMP_MSG_TICKS_FADE);
            int alpha = (int) (0xFF * ((float) ticksUnderFade / (float) TEMP_MSG_TICKS_FADE));

            GuiComponent.drawCenteredString(evt.getPoseStack(),
                MC.font,
                tempMsg,
                screenWidth / 2,
                screenHeight - iconFrameSize * 2 - 50,
                0xFFFFFF + (alpha << 24)
            );
        }
        if (tempMsgTicksLeft > 0) {
            tempMsgTicksLeft -= 1;
        }

        // ---------------------
        // Control group buttons
        // ---------------------
        blitX = 100;
        // clean up untracked entities/buildings from control groups
        for (ControlGroup controlGroup : controlGroups) {
            controlGroup.clean();

            if (!controlGroup.isEmpty()) {
                Button ctrlGroupButton = controlGroup.getButton();
                ctrlGroupButton.render(evt.getPoseStack(), blitX, 0, mouseX, mouseY);
                renderedButtons.add(ctrlGroupButton);
                blitX += iconFrameSize;
            }
        }

        // ---------------------
        // Attack warning button
        // ---------------------
        Button attackWarningButton = AttackWarningClientEvents.getWarningButton();
        if (!attackWarningButton.isHidden.get()) {
            attackWarningButton.render(evt.getPoseStack(),
                screenWidth - (MinimapClientEvents.getMapGuiRadius() * 2) - (MinimapClientEvents.CORNER_OFFSET * 2)
                    - 14,
                screenHeight - MinimapClientEvents.getMapGuiRadius() - (MinimapClientEvents.CORNER_OFFSET * 2) - 2,
                mouseX,
                mouseY
            );
            renderedButtons.add(attackWarningButton);
        }

        // ----------------------
        // Map size toggle button
        // ----------------------
        Button toggleMapSizeButton = MinimapClientEvents.getToggleSizeButton();
        if (!toggleMapSizeButton.isHidden.get()) {
            toggleMapSizeButton.render(evt.getPoseStack(),
                    screenWidth - (toggleMapSizeButton.iconSize * 2),
                    screenHeight - (toggleMapSizeButton.iconSize * 2),
                    mouseX,
                    mouseY
            );
            renderedButtons.add(toggleMapSizeButton);
        }
        Button camSensitivityButton = MinimapClientEvents.getCamSensitivityButton();
        if (!camSensitivityButton.isHidden.get()) {
            camSensitivityButton.render(evt.getPoseStack(),
                    screenWidth - (camSensitivityButton.iconSize * 4),
                    screenHeight - (camSensitivityButton.iconSize * 2),
                    mouseX,
                    mouseY
            );
            renderedButtons.add(camSensitivityButton);
        }
        Button mapLockButton = MinimapClientEvents.getMapLockButton();
        if (!mapLockButton.isHidden.get()) {
            mapLockButton.render(evt.getPoseStack(),
                    screenWidth - (mapLockButton.iconSize * 2),
                    screenHeight - (mapLockButton.iconSize * 4),
                    mouseX,
                    mouseY
            );
            renderedButtons.add(mapLockButton);
        }
        Button nightCirclesButton = MinimapClientEvents.getNightCirclesModeButton();
        if (!nightCirclesButton.isHidden.get()) {
            nightCirclesButton.render(evt.getPoseStack(),
                    screenWidth - (nightCirclesButton.iconSize * 4),
                    screenHeight - (nightCirclesButton.iconSize * 4),
                    mouseX,
                    mouseY
            );
            renderedButtons.add(nightCirclesButton);
        }

        // ------------------------------
        // Start buttons (spectator only)
        // ------------------------------
        if (!PlayerClientEvents.isRTSPlayer && !PlayerClientEvents.rtsLocked) {

                Button diffsButton = ConfigClientEvents.getDiffsButton();
                if (!diffsButton.isHidden.get()) {
                    diffsButton.render(evt.getPoseStack(),
                            screenWidth - (StartButtons.ICON_SIZE * 10),
                            StartButtons.ICON_SIZE / 2,
                            mouseX,
                            mouseY
                    );
                    renderedButtons.add(diffsButton);
                }

                Button gamemodeButton = ClientGameModeHelper.getButton();
                if (gamemodeButton != null && !gamemodeButton.isHidden.get() && !TutorialClientEvents.isEnabled()) {
                    gamemodeButton.render(evt.getPoseStack(),
                            screenWidth - (StartButtons.ICON_SIZE * 8),
                            StartButtons.ICON_SIZE / 2,
                            mouseX,
                            mouseY
                    );
                    renderedButtons.add(gamemodeButton);
                }

                if (ClientGameModeHelper.gameMode != GameMode.SANDBOX) {
                    if (!StartButtons.villagerStartButton.isHidden.get()) {
                        StartButtons.villagerStartButton.render(evt.getPoseStack(),
                                screenWidth - (StartButtons.ICON_SIZE * 6),
                                StartButtons.ICON_SIZE / 2,
                                mouseX,
                                mouseY
                        );
                        renderedButtons.add(StartButtons.villagerStartButton);
                    }
                    if (!StartButtons.monsterStartButton.isHidden.get()) {
                        StartButtons.monsterStartButton.render(evt.getPoseStack(),
                                (int) (screenWidth - (StartButtons.ICON_SIZE * 4f)),
                                StartButtons.ICON_SIZE / 2,
                                mouseX,
                                mouseY
                        );
                        renderedButtons.add(StartButtons.monsterStartButton);
                    }
                    if (!StartButtons.piglinStartButton.isHidden.get()) {
                        StartButtons.piglinStartButton.render(evt.getPoseStack(),
                                screenWidth - (StartButtons.ICON_SIZE * 2),
                                StartButtons.ICON_SIZE / 2,
                                mouseX,
                                mouseY
                        );
                        renderedButtons.add(StartButtons.piglinStartButton);
                    }
                }
        }
        else if (SurvivalClientEvents.isEnabled) {
            Button nextWaveButton = SurvivalClientEvents.getNextWaveButton();
            if (!nextWaveButton.isHidden.get()) {
                nextWaveButton.tooltipOffsetY = 15;
                nextWaveButton.render(evt.getPoseStack(),
                        screenWidth - (StartButtons.ICON_SIZE * 2),
                        StartButtons.ICON_SIZE / 2,
                        mouseX,
                        mouseY
                );
                renderedButtons.add(nextWaveButton);
            }
        }

        // --------------------
        // Tutorial Help button
        // --------------------
        if (!helpButton.isHidden.get()) {
            int xi = screenWidth - (chatButton.iconSize * 2);
            int yi = 40;
            helpButton.render(evt.getPoseStack(), xi, yi, mouseX, mouseY);
            renderedButtons.add(helpButton);
        }
        // -----------
        // Chat button
        // -----------
        if (!chatButton.isHidden.get()) {
            int xi = screenWidth - (chatButton.iconSize * 2);
            int yi = 70;
            chatButton.render(evt.getPoseStack(), xi, yi, mouseX, mouseY);
            renderedButtons.add(chatButton);
        }
        // -------------------------
        // Select all military units
        // -------------------------
        if (!armyButton.isHidden.get()) {
            int xi = screenWidth - (armyButton.iconSize * 2);
            int yi = 100;
            armyButton.render(evt.getPoseStack(), xi, yi, mouseX, mouseY);
            renderedButtons.add(armyButton);
        }
        // -------------------
        // Idle workers button
        // -------------------
        if (!idleWorkerButton.isHidden.get()) {
            int xi = screenWidth - (idleWorkerButton.iconSize * 2);
            int yi = 130;
            idleWorkerButton.render(evt.getPoseStack(), xi, yi, mouseX, mouseY);
            GuiComponent.drawString(evt.getPoseStack(),
                MC.font,
                String.valueOf(idleWorkerIds.size()),
                xi + 2,
                yi + idleWorkerButton.iconSize - 1,
                0xFFFFFF
            );
            renderedButtons.add(idleWorkerButton);
        }

        // ------------------------------------------------------
        // Button tooltips (has to be rendered last to be on top)
        // ------------------------------------------------------
        for (Button button : renderedButtons)
            if (button.isMouseOver(mouseX, mouseY)) {
                button.renderTooltip(evt.getPoseStack(), mouseX, mouseY);
            }

        TutorialClientEvents.checkAndRenderNextAction(evt.getPoseStack(), renderedButtons);
    }

    public static boolean isMouseOverAnyButton() {
        for (Button button : renderedButtons)
            if (button.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        return false;
    }

    public static boolean isMouseOverAnyButtonOrHud() {
        for (RectZone hudZone : hudZones)
            if (hudZone.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        if (MinimapClientEvents.isPointInsideMinimap(mouseX, mouseY)) {
            return true;
        }
        return isMouseOverAnyButton();
    }

    @SubscribeEvent
    public static void onMousePress(ScreenEvent.MouseButtonPressed.Post evt) {

        for (Button button : renderedButtons) {
            if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
                button.checkClicked((int) evt.getMouseX(), (int) evt.getMouseY(), true);
            } else if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
                button.checkClicked((int) evt.getMouseX(), (int) evt.getMouseY(), false);
            }
        }
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
            mouseLeftDownX = (int) evt.getMouseX();
            mouseLeftDownY = (int) evt.getMouseY();
        }
    }

    // for some reason some bound vanilla keys like Q and E don't trigger KeyPressed but still trigger keyReleased
    @SubscribeEvent
    public static void onKeyRelease(ScreenEvent.KeyReleased.KeyReleased.Post evt) {
        if (MC.screen == null || !MC.screen.getTitle().getString().contains("topdowngui_container")) {
            return;
        }
        for (Button button : renderedButtons)
            button.checkPressed(evt.getKeyCode());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) {
            return;
        }
        if (OrthoviewClientEvents.isEnabled()) {
            portraitRendererUnit.tickAnimation();
        }

        // move camera to unit or building when its portrait is clicked/held on
        if (MiscUtil.isLeftClickDown(MC)) {
            if (buildingPortraitZone != null && buildingPortraitZone.isMouseOver(mouseX, mouseY)
                && buildingPortraitZone.isMouseOver(mouseLeftDownX, mouseLeftDownY) && MC.player != null
                && hudSelectedBuilding != null) {
                BlockPos pos = hudSelectedBuilding.centrePos;
                OrthoviewClientEvents.centreCameraOnPos(pos.getX(), pos.getZ());

            } else if (unitPortraitZone != null && unitPortraitZone.isMouseOver(mouseX, mouseY)
                && unitPortraitZone.isMouseOver(mouseLeftDownX, mouseLeftDownY) && MC.player != null) {
                OrthoviewClientEvents.centreCameraOnPos(hudSelectedEntity.getX(), hudSelectedEntity.getZ());
            }
        }
    }

    @SubscribeEvent
    // hudSelectedEntity and portraitRendererUnit should be assigned in the same event to avoid desyncs
    public static void onRenderLivingEntity(RenderLivingEvent.Post<? extends LivingEntity, ? extends Model> evt) {

        ArrayList<LivingEntity> units = UnitClientEvents.getSelectedUnits();

        // sort and hudSelect the first unit type in the list
        units.sort(Comparator.comparing(HudClientEvents::getSimpleEntityName));

        if (units.size() <= 0) {
            HudClientEvents.setHudSelectedEntity(null);
        } else if (hudSelectedEntity == null || units.size() == 1 || !units.contains(hudSelectedEntity)) {
            HudClientEvents.setHudSelectedEntity(units.get(0));
        }

        if (hudSelectedEntity == null) {
            portraitRendererUnit.model = null;
            portraitRendererUnit.renderer = null;
        } else if (evt.getEntity() == hudSelectedEntity) {
            portraitRendererUnit.model = evt.getRenderer().getModel();
            portraitRendererUnit.renderer = evt.getRenderer();
        }
    }

    @SubscribeEvent
    public static void onRenderNamePlate(RenderNameTagEvent evt) {
        //if (OrthoviewClientEvents.isEnabled())
        //    evt.setResult(Event.Result.DENY);
    }

    // MANAGE CONTROL GROUPS
    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyPressed.KeyPressed.Pre evt) {
        if (!(MC.screen instanceof TopdownGui)) {
            return;
        }

        // Prevent spectator mode options from showing up
        if (OrthoviewClientEvents.isEnabled()) {
            for (Keybinding numKey : Keybindings.nums)
                if (numKey.key == evt.getKeyCode()) {
                    evt.setCanceled(true);
                }
        }

        // Deselect everything
        if (evt.getKeyCode() == Keybindings.deselect.key) {
            UnitClientEvents.clearSelectedUnits();
            BuildingClientEvents.clearSelectedBuildings();
            BuildingClientEvents.setBuildingToPlace(null);
        }

        // Initialize controlGroups with empty arrays if not already initialized
        if (controlGroups.size() < Keybindings.nums.length) {
            controlGroups.clear(); // Clear in case of a previous partial initialization
            for (Keybinding keybinding : Keybindings.nums) {
                controlGroups.add(new ControlGroup());
            }
        }

        // Access and save to controlGroups if index is within bounds
        for (Keybinding keybinding : Keybindings.nums) {
            int index = Integer.parseInt(keybinding.buttonLabel);
            if (index >= 0 && index < controlGroups.size()) {  // Bounds check
                if (Keybindings.ctrlMod.isDown() && evt.getKeyCode() == keybinding.key) {
                    controlGroups.get(index).saveFromSelected(keybinding);
                }
            }
        }

        // Open chat while orthoview is enabled
        if (OrthoviewClientEvents.isEnabled() && evt.getKeyCode() == Keybindings.chat.key) {
            MC.setScreen(new ChatScreen(""));
        }

        // Cycle through selected units
        if (evt.getKeyCode() == Keybindings.tab.key) {
            cycleUnitSubgroups();
            cycleBuildingSubgroups();
        }
    }

    private static void cycleUnitSubgroups() {
        List<LivingEntity> entities = new ArrayList<>(getSelectedUnits().stream()
                .filter(e -> e instanceof Unit)
                .sorted(Comparator.comparing(HudClientEvents::getSimpleEntityName))
                .toList());

        if (entities.isEmpty())
            return;

        if (Keybindings.shiftMod.isDown())
            Collections.reverse(entities);

        if (hudSelectedEntity != null) {
            String hudSelectedEntityName = HudClientEvents.getModifiedEntityName(hudSelectedEntity);
            String lastEntityName = "";
            boolean cycled = false;
            for (LivingEntity entity : entities) {
                String currentEntityName = HudClientEvents.getModifiedEntityName(entity);
                if (lastEntityName.equals(hudSelectedEntityName) && !currentEntityName.equals(lastEntityName)) {
                    HudClientEvents.setHudSelectedEntity(entity);
                    cycled = true;
                    break;
                }
                lastEntityName = currentEntityName;
            }
            if (!cycled) {
                HudClientEvents.setHudSelectedEntity(entities.get(0));
            } else {
                HudClientEvents.setLowestCdHudEntity();
            }
        }
    }

    private static void cycleBuildingSubgroups() {
        List<Building> buildings = new ArrayList<>(BuildingClientEvents.getSelectedBuildings().stream()
                .sorted(Comparator.comparing(b -> b.name))
                .toList());

        if (buildings.isEmpty())
            return;

        if (Keybindings.shiftMod.isDown())
            Collections.reverse(buildings);

        if (hudSelectedBuilding != null) {
            String hudSelectedBuildingName = hudSelectedBuilding.name;
            String lastBuildingName = "";
            boolean cycled = false;
            for (Building building : buildings) {
                String currentBuildingName = building.name;
                if (lastBuildingName.equals(hudSelectedBuildingName) && !currentBuildingName.equals(lastBuildingName)) {
                    hudSelectedBuilding = building;
                    cycled = true;
                    break;
                }
                lastBuildingName = currentBuildingName;
            }
            if (!cycled)
                hudSelectedBuilding = buildings.get(0);
        }
    }

    // newUnitIds are replacing oldUnitIds - replace them in every control group while retaining their index
    public static void convertControlGroups(int[] oldUnitIds, int[] newUnitIds) {
        if (MC.level == null) {
            return;
        }
        for (ControlGroup group : controlGroups) {
            for (int i = 0; i < oldUnitIds.length; i++) {
                for (int j = 0; j < group.entityIds.size(); j++) {
                    if (group.entityIds.get(j) == oldUnitIds[i]) {
                        group.entityIds.add(j, newUnitIds[i]);
                        break;
                    }
                }
            }
            for (int i = 0; i < oldUnitIds.length; i++) {
                for (int j = 0; j < oldUnitIds.length; j++) {
                    final int k = j;
                    group.entityIds.removeIf(id -> id == oldUnitIds[k]);
                }
            }
        }
    }
}
