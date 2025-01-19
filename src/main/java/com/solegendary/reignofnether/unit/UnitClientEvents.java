package com.solegendary.reignofnether.unit;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.alliance.AllianceSystem;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.villagers.IronGolemBuilding;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerServerboundPacket;
import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.resources.*;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.unit.goals.MeleeAttackBuildingGoal;
import com.solegendary.reignofnether.unit.interfaces.*;
import com.solegendary.reignofnether.unit.packets.UnitActionServerboundPacket;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import com.solegendary.reignofnether.unit.units.monsters.WardenUnit;
import com.solegendary.reignofnether.unit.units.monsters.ZoglinUnit;
import com.solegendary.reignofnether.unit.units.piglins.BruteUnit;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import com.solegendary.reignofnether.unit.units.piglins.HoglinUnit;
import com.solegendary.reignofnether.unit.units.villagers.*;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;

import static com.solegendary.reignofnether.building.BuildingClientEvents.getPlayerToBuildingRelationship;
import static com.solegendary.reignofnether.cursor.CursorClientEvents.getPreselectedBlockPos;
import static com.solegendary.reignofnether.hud.HudClientEvents.hudSelectedEntity;
import static com.solegendary.reignofnether.unit.Checkpoint.CHECKPOINT_TICKS_FADE;
import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.*;

public class UnitClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();

    // max possible pop you can have regardless of buildings, adjustable via /gamerule maxPopulation cheat
    public static int maxPopulation = ResourceCosts.DEFAULT_MAX_POPULATION;

    // list of vecs used in RenderChunkRegionMixin to replace leaf rendering
    private static final int WINDOW_RADIUS = 5; // size of area to hide leaves
    public static final int WINDOW_UPDATE_TICKS_MAX = 5; // size of area to hide leaves
    public static final List<ArrayList<Vec3>> unitWindowVecs = Collections.synchronizedList(new ArrayList<>());
    public static final List<BlockPos> windowPositions = Collections.synchronizedList(new ArrayList<>());
    public static int windowUpdateTicks = UnitClientEvents.WINDOW_UPDATE_TICKS_MAX;

    // list of ids that correspond to idle workers - should only be updated from server side
    public static final ArrayList<Integer> idleWorkerIds = new ArrayList<>();

    // units moused over or inside a box select
    private static final ArrayList<LivingEntity> preselectedUnits = new ArrayList<>();
    // units selected by click or box select
    private static final ArrayList<LivingEntity> selectedUnits = new ArrayList<>();
    // tracking of all existing units
    private static final ArrayList<LivingEntity> allUnits = new ArrayList<>();

    @Nullable private static UnitActionItem lastClientUAIActioned = null;

    public static boolean neutralAggro = false;

    public static ArrayList<LivingEntity> getPreselectedUnits() { return preselectedUnits; }
    public static ArrayList<LivingEntity> getSelectedUnits() {
        return selectedUnits;
    }
    public static ArrayList<LivingEntity> getAllUnits() {
        return allUnits;
    }

    public static void addPreselectedUnit(LivingEntity unit) {
        if (unit instanceof Player player && (player.isSpectator() || player.isCreative()))
            return;
        if (!FogOfWarClientEvents.isInBrightChunk(unit))
            return;
        if (unit.isPassenger())
            return;
        preselectedUnits.add(unit);
    }
    public static void addSelectedUnit(LivingEntity unit) {
        CursorClientEvents.setLeftClickAction(null);
        if (!FogOfWarClientEvents.isInBrightChunk(unit))
            return;
        if (unit.isPassenger())
            return;
        selectedUnits.add(unit);
        selectedUnits.sort(Comparator.comparing(HudClientEvents::getSimpleEntityName));
        selectedUnits.sort(Comparator.comparing(Entity::getId));
        BuildingClientEvents.clearSelectedBuildings();
    }
    public static void clearPreselectedUnits() {
        preselectedUnits.clear();
    }
    public static void clearSelectedUnits() {
        selectedUnits.clear();
    }

    private static long lastLeftClickTime = 0; // to track double clicks
    private static final long DOUBLE_CLICK_TIME_MS = 500;

    private static boolean isLeftClickAttack() {
        return CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK;
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent evt) {
        if (evt.getLevel().isClientSide())
            selectedUnits.removeIf(e -> e.getId() == evt.getEntityMounting().getId());
    }

    public static int getCurrentPopulation(String playerName) {
        int currentPopulation = 0;
        if (MC.level != null) {
            for (LivingEntity entity : allUnits) {
                if (entity instanceof Unit unit)
                    if (unit.getOwnerName().equals(playerName))
                        currentPopulation += unit.getPopCost();
            }
            for (Building building : BuildingClientEvents.getBuildings())
                if (building.ownerName.equals(playerName))
                    if (building instanceof ProductionBuilding prodBuilding) {
                        for (ProductionItem prodItem : prodBuilding.productionQueue)
                            currentPopulation += prodItem.popCost;
                    } else if (building instanceof IronGolemBuilding) {
                        currentPopulation += ResourceCosts.IRON_GOLEM.population;
                    }
        }
        return currentPopulation;
    }

    public static void sendUnitCommandManual(String playerName, UnitAction action, int unitId, int[] unitIds,
                                             BlockPos preselectedBlockPos, BlockPos selectedBuildingPos) {
        if (MC.player != null && playerName.equals(MC.player.getName().getString()))
            sendUnitCommandManual(action, unitId, unitIds, preselectedBlockPos, selectedBuildingPos);
    }

    public static void sendUnitCommandManual(UnitAction action, int unitId, int[] unitIds,
                                             BlockPos preselectedBlockPos) {
        sendUnitCommandManual(action, unitId, unitIds, preselectedBlockPos, new BlockPos(0,0,0));
    }

    public static void sendUnitCommandManual(UnitAction action, int unitId, int[] unitIds,
                                             BlockPos preselectedBlockPos, boolean clientside, boolean serverside) {
        sendUnitCommandManual(action, unitId, unitIds, preselectedBlockPos,
                new BlockPos(0,0,0), clientside, serverside);
    }

    public static void sendUnitCommandManual(UnitAction action, int unitId, int[] unitIds) {
        sendUnitCommandManual(action, unitId, unitIds,
                new BlockPos(0,0,0),
                new BlockPos(0,0,0));
    }

    public static void sendUnitCommandManual(UnitAction action, int[] unitIds) {
        sendUnitCommandManual(action, -1, unitIds,
                new BlockPos(0,0,0),
                new BlockPos(0,0,0));
    }

    public static void sendUnitCommandManual(UnitAction action, int unitId, int[] unitIds,
                                             BlockPos preselectedBlockPos, BlockPos selectedBuildingPos) {
        if (MC.player != null) {
            UnitActionItem actionItem = new UnitActionItem(
                MC.player.getName().getString(),
                action, unitId, unitIds,
                preselectedBlockPos,
                selectedBuildingPos
            );
            // prevent spam clicking the same action repeatedly
            if (!actionItem.equals(lastClientUAIActioned)) {
                actionItem.action(MC.level);
                lastClientUAIActioned = actionItem;
            }

            PacketHandler.INSTANCE.sendToServer(new UnitActionServerboundPacket(
                MC.player.getName().getString(),
                action, unitId, unitIds,
                preselectedBlockPos,
                selectedBuildingPos,
                Keybindings.shiftMod.isDown()
            ));
        }
    }

    public static void sendUnitCommandManual(UnitAction action, int unitId, int[] unitIds,
                                             BlockPos preselectedBlockPos, BlockPos selectedBuildingPos,
                                             boolean clientside, boolean serverside) {
        if (MC.player != null) {
            if (clientside) {
                UnitActionItem actionItem = new UnitActionItem(
                        MC.player.getName().getString(),
                        action, unitId, unitIds,
                        preselectedBlockPos,
                        selectedBuildingPos
                );
                actionItem.action(MC.level);
            }
            if (serverside) {
                PacketHandler.INSTANCE.sendToServer(new UnitActionServerboundPacket(
                        MC.player.getName().getString(),
                        action, unitId, unitIds,
                        preselectedBlockPos,
                        selectedBuildingPos,
                        Keybindings.shiftMod.isDown()
                ));
            }
        }
    }

    public static void sendUnitCommand(UnitAction action) {
        BlockPos bp = getPreselectedBlockPos();

        if (action.name().toLowerCase().contains("startrts")) {
            if (action == UnitAction.STARTRTS_VILLAGERS) {
                PlayerServerboundPacket.startRTS(Faction.VILLAGERS, (double) bp.getX(), (double) bp.getY(), (double) bp.getZ());
            } else if (action == UnitAction.STARTRTS_MONSTERS) {
                PlayerServerboundPacket.startRTS(Faction.MONSTERS, (double) bp.getX(), (double) bp.getY(), (double) bp.getZ());
            } else if (action == UnitAction.STARTRTS_PIGLINS) {
                PlayerServerboundPacket.startRTS(Faction.PIGLINS, (double) bp.getX(), (double) bp.getY(), (double) bp.getZ());
            }
            return;
        }
        else if (action.name().toLowerCase().contains("sandbox_spawn")) {
            if (action == UnitAction.STARTRTS_VILLAGERS) {
                PlayerServerboundPacket.startRTS(Faction.VILLAGERS, (double) bp.getX(), (double) bp.getY(), (double) bp.getZ());
            } else if (action == UnitAction.STARTRTS_MONSTERS) {
                PlayerServerboundPacket.startRTS(Faction.MONSTERS, (double) bp.getX(), (double) bp.getY(), (double) bp.getZ());
            } else if (action == UnitAction.STARTRTS_PIGLINS) {
                PlayerServerboundPacket.startRTS(Faction.PIGLINS, (double) bp.getX(), (double) bp.getY(), (double) bp.getZ());
            }
            return;
        }

        if (MC.player != null) {
            int[] selUnits = selectedUnits.stream()
                    .filter(u -> {
                        if (u instanceof Unit unit)
                            for (Ability ability : unit.getAbilities())
                                if (ability.isChanneling() && ability.oneClickOneUse && ability.action == action)
                                    return false;
                        return true;
                    })
                    .mapToInt(Entity::getId).toArray();

            UnitActionItem actionItem = new UnitActionItem(
                MC.player.getName().getString(),
                action,
                preselectedUnits.size() > 0 ? preselectedUnits.get(0).getId() : -1,
                selUnits,
                bp,
                HudClientEvents.hudSelectedBuilding != null ? HudClientEvents.hudSelectedBuilding.originPos : new BlockPos(0,0,0)
            );
            actionItem.action(MC.level);

            PacketHandler.INSTANCE.sendToServer(new UnitActionServerboundPacket(
                MC.player.getName().getString(),
                action,
                preselectedUnits.size() > 0 ? preselectedUnits.get(0).getId() : -1,
                selUnits,
                bp,
                HudClientEvents.hudSelectedBuilding != null ? HudClientEvents.hudSelectedBuilding.originPos : new BlockPos(0,0,0),
                Keybindings.shiftMod.isDown()
            ));
        }
    }

    private static void resolveMoveAction() {
        // follow friendly unit
        if (preselectedUnits.size() == 1 && !targetingSelf()) {
            if (hudSelectedEntity instanceof WitchUnit witchUnit) {
                sendUnitCommand(UnitAction.THROW_LINGERING_REGEN_POTION);
            } else {
                sendUnitCommand(UnitAction.FOLLOW);
            }
        }
        // move to ground pos (disabled during camera manip)
        else if (!Keybindings.altMod.isDown() && selectedUnits.size() > 0 && MC.level != null) {
            ResourceName resName = ResourceSources.getBlockResourceName(getPreselectedBlockPos(), MC.level);
            boolean isGathering = hudSelectedEntity instanceof WorkerUnit && resName != ResourceName.NONE;

            if (selectedUnits.size() == 1 || isGathering)
                sendUnitCommand(UnitAction.MOVE);
            else { // if we do not have a gathering villager as the fist

                List<Pair<Integer, BlockPos>> formationPairs = UnitFormations.getMoveFormation(
                    MC.level, selectedUnits, getPreselectedBlockPos()
                );

                for (Pair<Integer, BlockPos> pair : formationPairs) {
                    int entityId = pair.getFirst();
                    BlockPos targetPos = pair.getSecond();
                    Entity entity = MC.level.getEntity(entityId);
                    if (entity instanceof Unit unit &&
                        unit.getMoveGoal() != null) {

                        //sendUnitCommandManual(UnitAction.MOVE, -1, new int[]{entityId}, targetPos, true, false);

                        sendUnitCommandManual(UnitAction.MOVE, -1, new int[]{entityId}, targetPos);
                    }
                }
                for (LivingEntity le : selectedUnits)
                    if (le instanceof Unit unit && unit.getMoveGoal() != null)
                        unit.getMoveGoal().lastSelectedMoveTarget = getPreselectedBlockPos();
            }
        }
    }

    public static ResourceName getSelectedUnitResourceTarget() {
        Entity entity = hudSelectedEntity;
        if (entity instanceof WorkerUnit workerUnit)
            return workerUnit.getGatherResourceGoal().getTargetResourceName();
        return ResourceName.NONE;
    }

    public static void syncOwnerName(int entityId, String ownerName) {
        for(LivingEntity entity : allUnits)
            if (entity.getId() == entityId && MC.level != null)
                if (entity instanceof Unit unit)
                    unit.setOwnerName(ownerName);
    }

    /**
     * Update data on a unit from serverside, mainly to ensure unit HUD data is up-to-date
     * Only try to update health and pos if out of view
     */
    public static void syncUnitStats(int entityId, float health, Vec3 pos, String ownerName) {
        for (LivingEntity entity : allUnits) {
            if (entity.getId() == entityId && MC.level != null) {
                boolean isLoadedClientside = MC.level.getEntity(entityId) != null;
                if (!isLoadedClientside) {
                    entity.setHealth(health);
                    entity.setPos(pos);
                }
                MinimapClientEvents.removeMinimapUnit(entityId);
                return;
            }
        }
        // if the unit doesn't exist at all clientside, create a MinimapUnit to at least track its minimap position
        MinimapClientEvents.syncMinimapUnits(new BlockPos(pos.x, pos.y, pos.z), entityId, ownerName);
    }

    public static void syncWorkerUnit(int entityId, boolean isBuilding, ResourceName gatherName, BlockPos gatherPos, int gatherTicks) {
        for(LivingEntity entity : allUnits) {
            if (entity.getId() == entityId && MC.level != null) {
                if (entity instanceof WorkerUnit workerUnit) {
                    workerUnit.getBuildRepairGoal().setIsBuildingServerside(isBuilding);
                    workerUnit.getGatherResourceGoal().syncFromServer(gatherName, gatherPos, gatherTicks);
                }
            }
        }
    }

    public static void syncUnitResources(int entityId, Resources res) {
        for(LivingEntity entity : allUnits) {
            if (entity.getId() == entityId && MC.level != null) {
                if (entity instanceof Unit unit) {
                    unit.getItems().clear();
                    unit.getItems().add(new ItemStack(Items.SUGAR, res.food));
                    unit.getItems().add(new ItemStack(Items.STICK, res.wood));
                    unit.getItems().add(new ItemStack(Items.STONE, res.ore));
                }
            }
        }
    }

    /*
    private static double variance = 0;
    @SubscribeEvent
    public static void onRenderOverLay(RenderGuiOverlayEvent.Pre evt) {
        MiscUtil.drawDebugStrings(evt.getPoseStack(), MC.font, new String[] {
                "var: " + variance,
        });
    }
     */

    private static final int VIS_CHECK_TICKS_MAX = 10;
    private static int ticksToNextVisCheck = VIS_CHECK_TICKS_MAX;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END)
            return;

        //if (MC.level != null)
        //    variance = WaveSpawner.getYVariance(MC.level, getPreselectedBlockPos(), 8);

        ticksToNextVisCheck -= 1;

        if (ticksToNextVisCheck <= 0) {
            ticksToNextVisCheck = VIS_CHECK_TICKS_MAX;

            // prevent selection of units out of view
            selectedUnits.removeIf(e -> !FogOfWarClientEvents.isInBrightChunk(e));
        }

        // calculate vecs used to hide leaf blocks around units
        if (MC.player != null && OrthoviewClientEvents.hideLeavesMethod == OrthoviewClientEvents.LeafHideMethod.AROUND_UNITS_AND_CURSOR &&
            OrthoviewClientEvents.isEnabled()) {

            synchronized (windowPositions) {
                windowPositions.clear();
                UnitClientEvents.getAllUnits().forEach(u -> {
                    if (FogOfWarClientEvents.isInBrightChunk(u))
                        windowPositions.add(u.getOnPos());
                });
                BlockPos cursorBp = CursorClientEvents.getPreselectedBlockPos();
                windowPositions.add(cursorBp);

                synchronized (unitWindowVecs) {
                    unitWindowVecs.clear();
                    windowPositions.forEach(bp -> {
                        float dist = OrthoviewClientEvents.getZoom() * 2;
                        if (bp.distSqr(MC.player.getOnPos()) < (dist * dist))
                            unitWindowVecs.add(MyMath.prepIsPointInsideRect3d(Minecraft.getInstance(),
                                    new Vector3d(bp.getX() - WINDOW_RADIUS, bp.getY(), bp.getZ() - WINDOW_RADIUS), // tl
                                    new Vector3d(bp.getX() - WINDOW_RADIUS, bp.getY(), bp.getZ() + WINDOW_RADIUS), // bl
                                    new Vector3d(bp.getX() + WINDOW_RADIUS, bp.getY(), bp.getZ() + WINDOW_RADIUS)  // br
                            ));
                    });
                }
            }
        } else {
            synchronized (windowPositions) {
                windowPositions.clear();
            }
            synchronized (unitWindowVecs) {
                unitWindowVecs.clear();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveEvent(EntityLeaveLevelEvent evt) {
        synchronized (idleWorkerIds) {
            idleWorkerIds.removeIf(id -> id == evt.getEntity().getId());
        }
    }

    /**
     * Clientside entities will join and leave based on render distance, but we want to keep entities tracked at all times
     * Therefore, only remove entities if they leave serverside via UnitClientboundPacket.
     */
    public static void onEntityLeave(int entityId) {
        selectedUnits.removeIf(e -> e.getId() == entityId);
        //System.out.println("selectedUnits removed entity: " + entityId);
        preselectedUnits.removeIf(e -> e.getId() == entityId);
        //System.out.println("preselectedUnits removed entity: " + entityId);
        allUnits.removeIf(e -> e.getId() == entityId);
        //System.out.println("allUnits removed entity: " + entityId);
        MinimapClientEvents.removeMinimapUnit(entityId);
    }
    /**
     * Add and update entities from clientside action
     */

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent evt) {
        Entity entity = evt.getEntity();

        if (entity instanceof Unit unit && evt.getLevel().isClientSide) {
            TutorialClientEvents.updateStage();

            if (selectedUnits.removeIf(e -> e.getId() == entity.getId()))
                selectedUnits.add((LivingEntity) entity);
            if (preselectedUnits.removeIf(e -> e.getId() == entity.getId()))
                preselectedUnits.add((LivingEntity) entity);
            allUnits.removeIf(e -> e.getId() == entity.getId());
            allUnits.add((LivingEntity) entity);

            unit.initialiseGoals(); // for clientside data tracking - server automatically does this via registerGoals();
            unit.setupEquipmentAndUpgradesClient();

            addUnitPoofs(evt.getLevel(), entity);
        }
        if (entity instanceof LivingEntity le && ResourceSources.isHuntableAnimal(le))
            addUnitPoofs(evt.getLevel(), entity);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Post evt) {
        if (!OrthoviewClientEvents.isEnabled()) return;
        if (MC.level == null) return;

        // prevent clicking behind HUDs
        if (HudClientEvents.isMouseOverAnyButtonOrHud()) {
            CursorClientEvents.setLeftClickAction(null);
            return;
        }

        // Can only detect clicks client side but only see and modify goals serverside so produce entity queues here
        // and consume in onWorldTick; we also can't add entities directly as they will not have goals populated
        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {

            if (selectedUnits.size() > 0 && isLeftClickAttack()) {
                // A + left click -> force attack single unit (even if friendly)
                if (preselectedUnits.size() == 1 && !targetingSelf()) {
                    sendUnitCommand(UnitAction.ATTACK);
                }
                // A + left click -> force attack building (even if friendly)
                else if (BuildingClientEvents.getPreselectedBuilding() != null)
                    sendUnitCommand(UnitAction.ATTACK_BUILDING);
                // A + left click -> attack move ground
                else
                    sendUnitCommand(UnitAction.ATTACK_MOVE);
            }

            // select all nearby units of the same type when the same unit is double-clicked
            // only works for owned units
            else if (selectedUnits.size() == 1 && MC.level != null && !Keybindings.shiftMod.isDown() &&
               (System.currentTimeMillis() - lastLeftClickTime) < DOUBLE_CLICK_TIME_MS &&
                preselectedUnits.size() > 0 && selectedUnits.contains(preselectedUnits.get(0))) {

                lastLeftClickTime = 0;
                LivingEntity selectedUnit = selectedUnits.get(0);
                List<? extends LivingEntity> nearbyEntities = MiscUtil.getEntitiesWithinRange(
                        new Vector3d(selectedUnit.position().x, selectedUnit.position().y, selectedUnit.position().z),
                        OrthoviewClientEvents.getZoom(),
                        selectedUnits.get(0).getClass(),
                        MC.level
                );
                if (getPlayerToEntityRelationship(selectedUnit) == Relationship.OWNED) {
                    clearSelectedUnits();
                    for (LivingEntity entity : nearbyEntities)
                        if (getPlayerToEntityRelationship(entity) == Relationship.OWNED)
                            addSelectedUnit(entity);
                    HudClientEvents.setLowestCdHudEntity();
                }
            }
            // move on left click
            else if (CursorClientEvents.getLeftClickAction() == UnitAction.MOVE)
                resolveMoveAction();
            // resolve any other abilities not explicitly covered here
            else if (CursorClientEvents.getLeftClickAction() != null && MC.player != null) {
                sendUnitCommand(CursorClientEvents.getLeftClickAction());
            }


            // left click -> select a single unit
            // if shift is held, deselect a unit or add it to the selected group
            else if (preselectedUnits.size() == 1 && !isLeftClickAttack()) {
                boolean deselected = false;

                if (Keybindings.shiftMod.isDown())
                    deselected = selectedUnits.removeIf(id -> id.equals(preselectedUnits.get(0)));

                if (Keybindings.shiftMod.isDown() && !deselected &&
                    preselectedUnits.get(0) instanceof Unit &&
                    getPlayerToEntityRelationship(preselectedUnits.get(0)) == Relationship.OWNED) {
                        addSelectedUnit(preselectedUnits.get(0));
                }
                else if (!deselected) { // select a single unit - this should be the only code path that allows you to select a non-owned unit
                    clearSelectedUnits();
                    addSelectedUnit(preselectedUnits.get(0));
                }
            }
            // deselect any non-owned units if we managed to select them with owned units
            // and disallow selecting > 1 non-owned unit or the client player
            if (selectedUnits.size() > 1)
                selectedUnits.removeIf(e -> getPlayerToEntityRelationship(e) != Relationship.OWNED || e.getId() == MC.player.getId());

            lastLeftClickTime = System.currentTimeMillis();
        }
        else if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (BuildingClientEvents.getBuildingToPlace() != null) {
                BuildingClientEvents.setBuildingToPlace(null);
                return;
            }
            if (selectedUnits.size() > 0) {
                Building preSelBuilding = BuildingClientEvents.getPreselectedBuilding();

                // right click -> attack unfriendly unit
                if (preselectedUnits.size() == 1 &&
                    !targetingSelf() &&
                    ((neutralAggro && getPlayerToEntityRelationship(preselectedUnits.get(0)) == Relationship.NEUTRAL) ||
                    getPlayerToEntityRelationship(preselectedUnits.get(0)) == Relationship.HOSTILE ||
                     ResourceSources.isHuntableAnimal(preselectedUnits.get(0)))) {

                     if (hudSelectedEntity instanceof WitchUnit witchUnit) {
                         sendUnitCommand(UnitAction.THROW_LINGERING_HARMING_POTION);
                     } else {
                         sendUnitCommand(UnitAction.ATTACK);
                     }
                }
                // right click -> attack unfriendly building
                else if (hudSelectedEntity instanceof AttackerUnit &&
                        (preSelBuilding != null) &&
                        !(preSelBuilding instanceof AbstractBridge) &&
                        ((neutralAggro && getPlayerToBuildingRelationship(preSelBuilding) == Relationship.NEUTRAL) ||
                        getPlayerToBuildingRelationship(preSelBuilding) == Relationship.HOSTILE)) {
                    sendUnitCommand(UnitAction.ATTACK_BUILDING);
                }
                // right click -> return resources
                else if (hudSelectedEntity instanceof Unit unit &&
                        unit.getReturnResourcesGoal() != null &&
                        Resources.getTotalResourcesFromItems(unit.getItems()).getTotalValue() > 0 &&
                        preSelBuilding != null && preSelBuilding.canAcceptResources && preSelBuilding.isBuilt &&
                        getPlayerToBuildingRelationship(preSelBuilding) == Relationship.OWNED) {
                    sendUnitCommand(UnitAction.RETURN_RESOURCES);
                }
                // right click -> build or repair preselected building
                else if (hudSelectedEntity instanceof WorkerUnit && preSelBuilding != null &&
                        (getPlayerToBuildingRelationship(preSelBuilding) == Relationship.OWNED) ||
                        preSelBuilding instanceof AbstractBridge) {

                    if (preSelBuilding.name.contains(" Farm") && preSelBuilding.isBuilt)
                        sendUnitCommand(UnitAction.FARM);
                    else if (BuildingUtils.isBuildingBuildable(true, preSelBuilding))
                        sendUnitCommand(UnitAction.BUILD_REPAIR);
                    else
                        resolveMoveAction();
                }
                // right click -> follow friendly unit or go to preselected blockPos
                else
                    resolveMoveAction();
            }
        }
        // clear all cursor actions
        CursorClientEvents.setLeftClickAction(null);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent evt) {
        if (MC.level == null)
            return;
        /**
         *  TODO: make these visible to 1st-person players but currently had a visual glitch
         *  doesnt align to camera very well, sometimes sinks below ground and too thin
         *  TODO: make this visible behind blocks (but only seems to work if orthoview is creative mode)
         */
        // if orthoview uses creative mode: RenderLevelStageEvent.Stage.AFTER_WEATHER
        // if orthoview uses spectator mode: RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS

        if ((OrthoviewClientEvents.isEnabled() && evt.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) ||
            (!OrthoviewClientEvents.isEnabled() && evt.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS))
        {
            ArrayList<LivingEntity> selectedUnits = getSelectedUnits();
            ArrayList<LivingEntity> preselectedUnits = getPreselectedUnits();

            Set<Entity> unitsToDraw = new HashSet<>();
            unitsToDraw.addAll(selectedUnits);
            unitsToDraw.addAll(preselectedUnits);

            // draw outlines on all (pre)selected units but only draw once per unit based on conditions
            // don't render preselection outlines if mousing over HUD
            if (OrthoviewClientEvents.isEnabled()) {
                for (Entity entity : unitsToDraw) {
                    if (!FogOfWarClientEvents.isInBrightChunk(entity))
                        continue;

                    if (preselectedUnits.contains(entity) &&
                            isLeftClickAttack() &&
                            !targetingSelf() && !HudClientEvents.isMouseOverAnyButtonOrHud())
                        MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entity.getBoundingBox(), 1.0f, 0.3f, 0.3f, 1.0f, false);
                    else if (selectedUnits.contains(entity))
                        MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entity.getBoundingBox(), 1.0f, 1.0f, 1.0f, 1.0f, false);
                    else if (preselectedUnits.contains(entity) && !HudClientEvents.isMouseOverAnyButtonOrHud())
                        MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entity.getBoundingBox(),1.0f, 1.0f, 1.0f, MiscUtil.isRightClickDown(MC) ? 1.0f : 0.5f, false);
                }
            }
            for (LivingEntity entity : allUnits) {
                if (!FogOfWarClientEvents.isInBrightChunk(entity) ||
                        entity.isPassenger())
                    continue;

                Relationship unitRs = getPlayerToEntityRelationship(entity);

                float alpha = 0.5f;
                if (selectedUnits.stream().map(u -> u.getId()).toList().contains(entity.getId()))
                    alpha = 1.0f;

                // draw only the bottom of the outline boxes
                AABB entityAABB = entity.getBoundingBox();
                entityAABB = entityAABB.setMaxY(entityAABB.minY);
                boolean excludeMaxY = OrthoviewClientEvents.isEnabled();

                // always-shown highlights to indicate unit relationships
                if (OrthoviewClientEvents.isEnabled()) {
                    switch (unitRs) {
                        case OWNED -> MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entityAABB, 0.2f, 1.0f, 0.2f, alpha, excludeMaxY);
                        case FRIENDLY -> MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entityAABB, 0.2f, 0.2f, 1.0f, alpha, excludeMaxY);
                        case HOSTILE -> MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entityAABB, 1.0f, 0.2f, 0.2f, alpha, excludeMaxY);
                        case NEUTRAL -> MyRenderer.drawLineBoxOutlineOnly(evt.getPoseStack(), entityAABB, 1.0f, 1.0f, 0.1f, alpha, excludeMaxY);
                    }
                }
            }
        }

        // AFTER_CUTOUT_BLOCKS lets us see checkpoints through leaves
        if (OrthoviewClientEvents.isEnabled() && evt.getStage() == AFTER_CUTOUT_BLOCKS) {
            // draw unit checkpoints
            for (LivingEntity entity : getSelectedUnits()) {
                if (entity instanceof Unit unit) {

                    float entityYOffset1 = 1.74f - ((LivingEntity) unit).getEyeHeight() - 1;
                    Vec3 lastPos = ((LivingEntity) unit).getEyePosition().add(0, entityYOffset1,0);

                    for (Checkpoint cp : unit.getCheckpoints()) {
                        int ticksUnderFade = Math.min(cp.ticksLeft, CHECKPOINT_TICKS_FADE);
                        float a = ((float) ticksUnderFade / (float) CHECKPOINT_TICKS_FADE) * 0.5f;
                        if (cp.isForEntity()) {
                            MyRenderer.drawLine(evt.getPoseStack(), lastPos, cp.getPos(), cp.isGreen ? 0 : 1, cp.isGreen ? 1 : 0, 0, a);
                            lastPos = cp.getPos();
                        } else {
                            MyRenderer.drawLine(evt.getPoseStack(), lastPos, cp.getPos(), cp.isGreen ? 0 : 1, cp.isGreen ? 1 : 0, 0, a);
                            if (MC.level.getBlockState(cp.bp.offset(0,1,0)).getBlock() instanceof SnowLayerBlock) {
                                AABB aabb = new AABB(cp.bp);
                                aabb = aabb.setMaxY(aabb.maxY + 0.13f);
                                MyRenderer.drawSolidBox(evt.getPoseStack(), aabb, Direction.UP, cp.isGreen ? 0 : 1, cp.isGreen ? 1 : 0, 0, a,
                                        new ResourceLocation("forge:textures/white.png"));
                            } else {
                                MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.UP, cp.bp, cp.isGreen ? 0 : 1, cp.isGreen ? 1 : 0, 0, a);
                            }
                            lastPos = cp.getPos();
                        }
                    }
                    // draw path nodes
                    /*
                    if (unit instanceof Mob mob && mob.getNavigation().getPath() != null) {
                        for (Node node : mob.getNavigation().getPath().nodes) {
                            BlockPos bp = new BlockPos(node.x, node.y, node.z).below();
                            MyRenderer.drawBlockFace(evt.getPoseStack(), Direction.UP, bp, 0, 1, 0, a);
                        }
                    }
                     */
                }
            }
        }
    }

    @SubscribeEvent
    public static void onButtonPress(ScreenEvent.KeyPressed.Pre evt) {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_DELETE) {
            LivingEntity entity = hudSelectedEntity;
            if (entity != null && getPlayerToEntityRelationship(entity) == Relationship.OWNED &&
                    !(entity instanceof CreeperUnit))
                sendUnitCommand(UnitAction.DELETE);
        }
    }

    public static boolean targetingSelf() {
        return selectedUnits.size() == 1 &&
                preselectedUnits.size() == 1 &&
                selectedUnits.get(0).equals(preselectedUnits.get(0));
    }

    public static Relationship getPlayerToEntityRelationship(LivingEntity entity) {
        if (MC.level != null && MC.player != null) {
            String playerName = MC.player.getName().getString();

            // Check if the entity is a Unit with no owner (neutral)
            if (entity instanceof Unit unit && unit.getOwnerName().isBlank()) {
                return Relationship.NEUTRAL;
            }

            // If the entity is a player, default to hostile unless further alliance checks are needed
            if (entity instanceof Player playerEntity) {
                String entityName = playerEntity.getName().getString();

                if (playerName.equals(entityName)) {
                    return Relationship.OWNED;
                } else if (AllianceSystem.isAllied(playerName, entityName)) {
                    return Relationship.FRIENDLY;
                } else {
                    return Relationship.HOSTILE;
                }
            }

            // Check if the entity is not a Unit (e.g., an NPC or neutral entity)
            if (!(entity instanceof Unit)) {
                return Relationship.NEUTRAL;
            }

            // For Units, check ownership and alliance
            String ownerName = ((Unit) entity).getOwnerName();

            if (playerName.equals(ownerName)) {
                return Relationship.OWNED;
            } else if (AllianceSystem.isAllied(playerName, ownerName)) {
                return Relationship.FRIENDLY;
            } else {
                return Relationship.HOSTILE;
            }
        }

        // If the world or player is null, return NEUTRAL
        return Relationship.NEUTRAL;
    }

    public static void syncConvertedUnits(String ownerName, int[] oldUnitIds, int[] newUnitIds) {

        for (int i = 0; i < oldUnitIds.length; i++) {
            if (MC.level == null)
                break;

            Entity oldEntity = MC.level.getEntity(oldUnitIds[i]);
            Entity newEntity = MC.level.getEntity(newUnitIds[i]);

            if (oldEntity instanceof Unit oldUnit &&
                newEntity instanceof Unit newUnit) {

                // retain selections
                int j = i;
                if (selectedUnits.removeIf(e -> e.getId() == oldUnitIds[j]))
                    selectedUnits.add((LivingEntity) newEntity);

                // retain control groups
                HudClientEvents.convertControlGroups(oldUnitIds, newUnitIds);

                if (oldUnit.getTargetGoal().getTarget() != null)
                    sendUnitCommandManual(
                        UnitAction.ATTACK,
                        oldUnit.getTargetGoal().getTarget().getId(),
                        new int[] { newEntity.getId() }
                    );
                if (oldUnit.getFollowTarget() != null)
                    sendUnitCommandManual(
                        UnitAction.FOLLOW,
                        oldUnit.getFollowTarget().getId(),
                        new int[] { newEntity.getId() }
                    );
                if (oldUnit.getMoveGoal().getMoveTarget() != null)
                    sendUnitCommandManual(
                        UnitAction.MOVE, -1,
                        new int[] { newEntity.getId() },
                        oldUnit.getMoveGoal().getMoveTarget()
                    );
                if (oldUnit.getReturnResourcesGoal() != null &&
                    oldUnit.getReturnResourcesGoal().getBuildingTarget() != null)
                    sendUnitCommandManual(
                        UnitAction.RETURN_RESOURCES, -1,
                        new int[] { newEntity.getId() },
                        oldUnit.getReturnResourcesGoal().getBuildingTarget().originPos,
                        new BlockPos(0,0,0)
                    );
            }
            if (oldEntity instanceof AttackerUnit oldAUnit &&
                newEntity instanceof AttackerUnit newAUnit) {

                if (oldAUnit.getAttackMoveTarget() != null)
                    sendUnitCommandManual(
                        UnitAction.ATTACK_MOVE, -1,
                        new int[] { newEntity.getId() },
                        oldAUnit.getAttackMoveTarget()
                    );
            }
        }
        // for some reason if we don't discard here first the vehicle also gets discarded
        if (MC.level != null) {
            for (int id : oldUnitIds) {
                Entity e = MC.level.getEntity(id);
                if (e instanceof ConvertableUnit cUnit)
                    cUnit.setShouldDiscard(true);
            }
        }
        sendUnitCommandManual(UnitAction.DISCARD, oldUnitIds);
    }

    public static void syncUnitAnimation(UnitAnimationAction animAction, boolean startAnimation, int entityId, int targetId,
                                         BlockPos buildingBp) {
        for (LivingEntity entity : getAllUnits()) {
            if (entity instanceof EvokerUnit eUnit && eUnit.getId() == entityId) {
                if (eUnit.getCastFangsGoal() != null) {
                    if (startAnimation)
                        eUnit.getCastFangsGoal().startCasting();
                    else
                        eUnit.getCastFangsGoal().stop();
                }
            } else if (entity instanceof WardenUnit wUnit && wUnit.getId() == entityId) {
                if (wUnit.getSonicBoomGoal() != null) {
                    if (startAnimation)
                        wUnit.startSonicBoomAnimation();
                    else
                        wUnit.stopSonicBoomAnimation();
                }
            } else if (entity instanceof GhastUnit gUnit && gUnit.getId() == entityId && startAnimation) {
                gUnit.showShootingFace();
            } else if (entity instanceof BruteUnit bUnit && bUnit.getId() == entityId) {
                bUnit.isHoldingUpShield = startAnimation;
            } else if (entity instanceof WorkerUnit wUnit && entity instanceof AttackerUnit aUnit && entity.getId() == entityId) {
                if (startAnimation && MC.level != null) {
                    if (entity instanceof VillagerUnit vUnit && vUnit.getUnitProfession() == VillagerUnitProfession.HUNTER && vUnit.isVeteran())
                        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
                    else
                        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));

                    aUnit.setUnitAttackTarget((LivingEntity) MC.level.getEntity(targetId)); // set itself as a target just for animation purposes, doesn't tick clientside anyway
                } else {
                    entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.AIR));
                    aUnit.setUnitAttackTarget(null);
                }
            } else if (entity instanceof VindicatorUnit vUnit && entity.getId() == entityId) {
                if (startAnimation && MC.level != null) {
                    if (targetId > 0) {
                        vUnit.setUnitAttackTarget((LivingEntity) MC.level.getEntity(targetId)); // set itself as a target just for animation purposes, doesn't tick clientside anyway
                    } else {
                        vUnit.setAttackBuildingTarget(buildingBp);
                    }
                } else {
                    vUnit.setUnitAttackTarget(null);
                    ((MeleeAttackBuildingGoal) vUnit.getAttackBuildingGoal()).stopAttacking();
                }
            }
        }
    }

    public static void playKeyframeAnimation(UnitAnimationAction animAction, int entityId) {
        for (LivingEntity entity : getAllUnits()) {
            if (entity instanceof KeyframeAnimated kfa && entity.getId() == entityId) {
                kfa.playSingleAnimation(animAction);
                return;
            }
        }
    }

    // usually used for attacking buildings
    public static void playAttackAnimation(int entityId) {
        for (LivingEntity entity : getAllUnits()) {
            if (entity.getId() == entityId) {
                if (entity instanceof IronGolemUnit ||
                    entity instanceof HoglinUnit ||
                    entity instanceof ZoglinUnit ||
                    entity instanceof RavagerUnit ||
                    entity instanceof WardenUnit) {
                    entity.handleEntityEvent((byte) 4);
                }
            }
        }
    }

    public static void syncIdleWorkers(int[] idleWorkerIds) {
        if (MC.level == null)
            return;

        UnitClientEvents.idleWorkerIds.clear();
        for (int id : idleWorkerIds) {
            for (LivingEntity entity : getAllUnits()) {
                if (entity.getId() == id &&
                    entity instanceof WorkerUnit unit &&
                    getPlayerToEntityRelationship(entity) == Relationship.OWNED)
                    UnitClientEvents.idleWorkerIds.add(id);
            }
        }
    }

    public static void addUnitPoofs(Level level, Entity entity) {
        RandomSource rand = RandomSource.create();
        for(int j = 0; j < 35; ++j) {
            double d0 = rand.nextGaussian() * 0.2;
            double d1 = rand.nextGaussian() * 0.2;
            double d2 = rand.nextGaussian() * 0.2;
            level.addParticle(ParticleTypes.POOF, entity.getX(), entity.getY(), entity.getZ(), d0, d1, d2);
        }
    }

    public static void setMaxPopulation(int value) {
        maxPopulation = value;
    }

    public static void makeVillagerVeteran(int unitId) {
        for (LivingEntity entity : getAllUnits())
            if (entity instanceof VillagerUnit vUnit && unitId == entity.getId())
                vUnit.isVeteran = true;
    }

    /*
    public static RenderLevelStageEvent.Stage stage = AFTER_CUTOUT_BLOCKS;

    @SubscribeEvent
    public static void onButtonPress2(ScreenEvent.KeyPressed.Pre evt) {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_L) {
            if (AFTER_CUTOUT_BLOCKS.equals(stage)) {
                stage = AFTER_TRANSLUCENT_BLOCKS;
            } else if (AFTER_TRANSLUCENT_BLOCKS.equals(stage)) {
                stage = AFTER_WEATHER;
            } else if (AFTER_WEATHER.equals(stage)) {
                stage = AFTER_SKY;
            } else if (AFTER_SKY.equals(stage)) {
                stage = AFTER;
            } else if (AFTER_CUTOUT_BLOCKS.equals(stage)) {
                stage = AFTER_TRANSLUCENT_BLOCKS;
            } else if (AFTER_CUTOUT_BLOCKS.equals(stage)) {
                stage = AFTER_TRANSLUCENT_BLOCKS;
            } else if (AFTER_CUTOUT_BLOCKS.equals(stage)) {
                stage = AFTER_TRANSLUCENT_BLOCKS;
            }
        }
    }
     */

    /*

    public static int yOffset = 0;
    public static int scale = 0;

    @SubscribeEvent
    public static void onButtonPress2(ScreenEvent.KeyPressed.Pre evt) {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_LEFT) {
            scale -= 1;
        } else if (evt.getKeyCode() == GLFW.GLFW_KEY_RIGHT) {
            scale += 1;
        } else if (evt.getKeyCode() == GLFW.GLFW_KEY_DOWN) {
            yOffset += 1;
        } else if (evt.getKeyCode() == GLFW.GLFW_KEY_UP) {
            yOffset -= 1;
        }
    }


    @SubscribeEvent
    public static void onRenderOverLay(RenderGuiOverlayEvent.Pre evt) {
        MiscUtil.drawDebugStrings(evt.getPoseStack(), MC.font, new String[] {
                "yoffset: " + yOffset,
                "scale: " + scale,
        });
    }



    public static int option = 0;

    public static double arm_x_rot = 0;
    public static double arm_y_rot = 0;
    public static double arm_z_rot = 0;
    public static int xp_rot = -90;
    public static int yp_rot = 180;
    public static double x_pos = 16.0f;
    public static double y_pos = 0.125f;
    public static double z_pos = -0.625f;

    @SubscribeEvent
    public static void onButtonPress2(ScreenEvent.KeyPressed.Pre evt) {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_LEFT || evt.getKeyCode() == GLFW.GLFW_KEY_RIGHT) {
            int mult = evt.getKeyCode() == GLFW.GLFW_KEY_LEFT ? -1 : 1;
            switch (option) {
                case 0 -> arm_x_rot += mult * 0.1;
                case 1 -> arm_y_rot += mult * 0.1;
                case 2 -> arm_z_rot += mult * 0.1;
                case 3 -> xp_rot += mult * 5;
                case 4 -> yp_rot += mult * 5;
                case 5 -> x_pos += mult * 0.1;
                case 6 -> y_pos += mult * 0.1;
                case 7 -> z_pos += mult * 0.1;
            }
        }
        else if (evt.getKeyCode() == GLFW.GLFW_KEY_UP) {
            option -= 1;
            if (option < 0)
                option = 7;
        }
        else if (evt.getKeyCode() == GLFW.GLFW_KEY_DOWN) {
            option += 1;
            if (option > 7)
                option = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderOverLay(RenderGuiOverlayEvent.Pre evt) {
        MiscUtil.drawDebugStrings(evt.getPoseStack(), MC.font, new String[] {
                "arm_x_rot: " + arm_x_rot + (option == 0 ? " <" : ""),
                "arm_y_rot: " + arm_y_rot + (option == 1 ? " <" : ""),
                "arm_z_rot: " + arm_z_rot + (option == 2 ? " <" : ""),
                "xp_rot: " + xp_rot + (option == 3 ? " <" : ""),
                "yp_rot: " + yp_rot + (option == 4 ? " <" : ""),
                "x_pos: " + x_pos + (option == 5 ? " <" : ""),
                "y_pos: " + y_pos + (option == 6 ? " <" : ""),
                "z_pos: " + z_pos + (option == 7 ? " <" : ""),
        });
    }
     */

    /*
    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyPressed.Pre evt) {
        if (Keybindings.altMod.isDown() && evt.getKeyCode() == GLFW.GLFW_KEY_SPACE && !getAllUnits().isEmpty()) {
            PacketHandler.INSTANCE.sendToServer(new UnitActionServerboundPacket(
                    "",
                    UnitAction.DEBUG1, 0, new int[]{0},
                    new BlockPos(0,0,0),
                    new BlockPos(0,0,0)
            ));
        }
        if (Keybindings.ctrlMod.isDown() && evt.getKeyCode() == GLFW.GLFW_KEY_SPACE && !getAllUnits().isEmpty()) {
            PacketHandler.INSTANCE.sendToServer(new UnitActionServerboundPacket(
                    "",
                    UnitAction.DEBUG2, 0, new int[]{0},
                    new BlockPos(0,0,0),
                    new BlockPos(0,0,0)
            ));
        }
    }
     */
}
