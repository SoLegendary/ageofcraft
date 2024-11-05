package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.building.buildings.monsters.Dungeon;
import com.solegendary.reignofnether.building.buildings.monsters.Laboratory;
import com.solegendary.reignofnether.building.buildings.piglins.FlameSanctuary;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.villagers.Castle;
import com.solegendary.reignofnether.building.buildings.villagers.IronGolemBuilding;
import com.solegendary.reignofnether.fogofwar.FrozenChunkClientboundPacket;
import com.solegendary.reignofnether.nether.NetherBlocks;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.*;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class BuildingServerEvents {

    private static final int BUILDING_SYNC_TICKS_MAX = 20; // how often we send out unit syncing packets
    private static int buildingSyncTicks = BUILDING_SYNC_TICKS_MAX;

    private static int TNT_BUILDING_BASE_DAMAGE = 20;
    private static int MAX_SCAFFOLD_DEPTH = 5;

    private static ServerLevel serverLevel = null;

    // buildings that currently exist serverside
    private static final ArrayList<Building> buildings = new ArrayList<>();

    public static final ArrayList<NetherZone> netherZones = new ArrayList<>();

    public static ArrayList<Building> getBuildings() { return buildings; }

    public static final Random random = new Random();

    public static void saveBuildings() {
        if (serverLevel == null)
            return;

        BuildingSaveData buildingData = BuildingSaveData.getInstance(serverLevel);
        buildingData.buildings.clear();

        getBuildings().forEach(b -> {
            boolean isUpgraded = b.isUpgraded();
            Portal.PortalType portalType = null;
            if (b instanceof Portal portal && portal.portalType != Portal.PortalType.BASIC)
                portalType = portal.portalType;

            buildingData.buildings.add(new BuildingSave(
                    b.originPos,
                    serverLevel,
                    b.name,
                    b.ownerName,
                    b.rotation,
                    b instanceof ProductionBuilding pb ? pb.getRallyPoint() : b.originPos,
                    b.isDiagonalBridge,
                    b.isBuilt,
                    isUpgraded,
                    portalType
            ));
            System.out.println("saved buildings/nether in serverevents: " + b.originPos);
        });
        buildingData.save();
        serverLevel.getDataStorage().save();
    }

    public static void saveNetherZones() {
        if (serverLevel == null)
            return;

        NetherZoneSaveData netherData = NetherZoneSaveData.getInstance(serverLevel);
        netherData.netherZones.clear();
        netherData.netherZones.addAll(netherZones);
        netherData.save();
        serverLevel.getDataStorage().save();

        System.out.println("saved " + netherZones.size() + " netherzones in serverevents");
    }

    @SubscribeEvent
    public static void loadBuildingsAndNetherZones(ServerStartedEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);

        if (level != null) {
            BuildingSaveData buildingData = BuildingSaveData.getInstance(level);
            NetherZoneSaveData netherData = NetherZoneSaveData.getInstance(level);
            ArrayList<BlockPos> placedNZs = new ArrayList<>();

            buildingData.buildings.forEach(b -> {
                Building building = BuildingUtils.getNewBuilding(b.name, level, b.originPos, b.rotation, b.ownerName, b.isDiagonalBridge);

                if (building != null) {
                    building.isBuilt = b.isBuilt;
                    BuildingServerEvents.getBuildings().add(building);

                    if (building instanceof ProductionBuilding pb)
                        pb.setRallyPoint(b.rallyPoint);

                    if (b.isUpgraded) {
                        if (building instanceof Castle castle)
                            castle.changeStructure(Castle.upgradedStructureName);
                        else if (building instanceof Laboratory lab)
                            lab.changeStructure(Laboratory.upgradedStructureName);
                        else if (building instanceof Portal portal)
                            portal.changeStructure(b.portalType);
                    }
                    // setNetherZone can only be run once - this supercedes where it normally happens in tick() -> onBuilt()
                    if (building instanceof NetherConvertingBuilding ncb)
                        for (NetherZone nz : netherData.netherZones)
                            if (building.isPosInsideBuilding(nz.getOrigin())) {
                                ncb.setNetherZone(nz);
                                placedNZs.add(nz.getOrigin());
                                System.out.println("loaded netherzone for: " + b.name + "|" + b.originPos);
                                break;
                            }
                    System.out.println("loaded building in serverevents: " + b.name + "|" + b.originPos);
                }
            });
            netherData.netherZones.forEach(nz -> {
                if (!placedNZs.contains(nz.getOrigin())) {
                    BuildingServerEvents.netherZones.add(nz);
                    System.out.println("loaded orphaned netherzone: " + nz.getOrigin());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent evt) {
        saveNetherZones();
        saveBuildings();
    }

    public static void placeBuilding(String buildingName, BlockPos pos, Rotation rotation, String ownerName,
                                     int[] builderUnitIds, boolean queue, boolean isDiagonalBridge) {
        Building newBuilding = BuildingUtils.getNewBuilding(buildingName, serverLevel, pos, rotation, ownerName, isDiagonalBridge);
        boolean buildingExists = buildings.stream().anyMatch(b -> b.originPos.equals(pos));

        if (newBuilding != null && !buildingExists) {
            // Handle special building (Iron Golem)
            if (newBuilding instanceof IronGolemBuilding) {
                int currentPop = UnitServerEvents.getCurrentPopulation(serverLevel, ownerName);
                int popSupply = BuildingServerEvents.getTotalPopulationSupply(ownerName);

                boolean canAffordPop = ResourcesServerEvents.resourcesList.stream()
                        .anyMatch(r -> r.ownerName.equals(ownerName) &&
                                (currentPop + ResourceCosts.IRON_GOLEM.population) <= popSupply);

                if (!canAffordPop) {
                    ResourcesClientboundPacket.warnInsufficientPopulation(ownerName);
                    return;
                }
            }

            if (newBuilding.canAfford(ownerName)) {
                buildings.add(newBuilding);
                newBuilding.forceChunk(true);
                int minY = BuildingUtils.getMinCorner(newBuilding.blocks).getY();

                if (!(newBuilding instanceof AbstractBridge))
                    for (BuildingBlock block : newBuilding.blocks)
                        if (block.getBlockPos().getY() == minY && !block.getBlockState().isAir())
                            placeScaffoldingUnder(block, newBuilding);

                newBuilding.blocks.stream()
                        .filter(block -> block.getBlockPos().getY() == minY &&
                                newBuilding.startingBlockTypes.contains(block.getBlockState().getBlock()))
                        .forEach(newBuilding::addToBlockPlaceQueue);

                BuildingClientboundPacket.placeBuilding(pos, buildingName, rotation, ownerName,
                        newBuilding.blockPlaceQueue.size(), isDiagonalBridge, false, false, Portal.PortalType.BASIC, false);

                ResourcesServerEvents.addSubtractResources(new Resources(
                        newBuilding.ownerName, -newBuilding.foodCost, -newBuilding.woodCost, -newBuilding.oreCost));

                assignBuilderUnits(builderUnitIds, queue, newBuilding);
            } else if (!PlayerServerEvents.isBot(ownerName)) {
                warnInsufficientResources(newBuilding);
            }

            UnitServerEvents.getAllUnits().stream()
                    .filter(entity -> entity instanceof Unit unit && unit.getOwnerName().equals(ownerName) &&
                            newBuilding.isPosInsideBuilding(entity.getOnPos().above().above()))
                    .forEach(entity -> moveNonBuildersAwayFromBuildingFoundations(entity, builderUnitIds, newBuilding));
        }
    }

    private static void placeScaffoldingUnder(BuildingBlock block, Building newBuilding) {
        BlockPos basePos = block.getBlockPos();
        int yBelow = 0;
        BlockState bsBelow;

        // Search downward for a solid block up to -5 levels below
        while (yBelow > -MAX_SCAFFOLD_DEPTH) {
            yBelow--;
            bsBelow = serverLevel.getBlockState(basePos.offset(0, yBelow, 0));
            if (bsBelow.getMaterial().isSolidBlocking())
                break; // Found a solid block, exit loop
        }
        if (yBelow <= -MAX_SCAFFOLD_DEPTH)
            return;

        // Place scaffolding from the lowest point back to the original block's level
        for (int y = yBelow + 1; y < 0; y++) {
            BlockPos scaffoldPos = basePos.offset(0, y, 0);
            BuildingBlock scaffold = new BuildingBlock(scaffoldPos, Blocks.SCAFFOLDING.defaultBlockState());
            newBuilding.getScaffoldBlocks().add(scaffold);
            newBuilding.addToBlockPlaceQueue(scaffold);
        }
    }


    private static void assignBuilderUnits(int[] builderUnitIds, boolean queue, Building newBuilding) {
        for (int id : builderUnitIds) {
            Entity entity = serverLevel.getEntity(id);
            if (entity instanceof WorkerUnit workerUnit) {
                if (queue) {
                    if (workerUnit.getBuildRepairGoal().queuedBuildings.isEmpty()) {
                        ((Unit) entity).resetBehaviours();
                        WorkerUnit.resetBehaviours(workerUnit);
                    }
                    workerUnit.getBuildRepairGoal().queuedBuildings.add(newBuilding);
                    if (workerUnit.getBuildRepairGoal().getBuildingTarget() == null) {
                        workerUnit.getBuildRepairGoal().startNextQueuedBuilding();
                    }
                } else {
                    ((Unit) entity).resetBehaviours();
                    WorkerUnit.resetBehaviours(workerUnit);
                    workerUnit.getBuildRepairGoal().setBuildingTarget(newBuilding);
                }
            }
        }
    }

    private static void warnInsufficientResources(Building newBuilding) {
        ResourcesClientboundPacket.warnInsufficientResources(
                newBuilding.ownerName,
                ResourcesServerEvents.canAfford(newBuilding.ownerName, ResourceName.FOOD, newBuilding.foodCost),
                ResourcesServerEvents.canAfford(newBuilding.ownerName, ResourceName.WOOD, newBuilding.woodCost),
                ResourcesServerEvents.canAfford(newBuilding.ownerName, ResourceName.ORE, newBuilding.oreCost)
        );
    }

    private static void moveNonBuildersAwayFromBuildingFoundations(LivingEntity entity, int[] builderUnitIds, Building newBuilding) {
        if (Arrays.stream(builderUnitIds).noneMatch(id -> id == entity.getId())) {
            UnitServerEvents.addActionItem(
                    ((Unit) entity).getOwnerName(),
                    UnitAction.MOVE, -1,
                    new int[]{entity.getId()},
                    newBuilding.getClosestGroundPos(entity.getOnPos(), 2),
                    new BlockPos(0, 0, 0)
            );
        }
    }


    public static void cancelBuilding(Building building) {
        if (building == null || building.isCapitol)
            return;

        // remove from tracked buildings, all of its leftover queued blocks and then blow it up
        buildings.remove(building);
        if (building instanceof NetherConvertingBuilding nb && nb.getZone() != null) {
            nb.getZone().startRestoring();
            saveNetherZones();
        }
        FrozenChunkClientboundPacket.setBuildingDestroyedServerside(building.originPos);

        // AOE2-style refund: return the % of the non-built portion of the building
        // eg. cancelling a building at 70% completion will refund only 30% cost
        if (!building.isBuilt) {
            float buildPercent = building.getBlocksPlacedPercent();
            ResourcesServerEvents.addSubtractResources(new Resources(
                    building.ownerName,
                    Math.round(building.foodCost * (1 - buildPercent)),
                    Math.round(building.woodCost * (1 - buildPercent)),
                    Math.round(building.oreCost * (1 - buildPercent))
            ));
        }
        building.destroy((ServerLevel) building.getLevel());
    }

    public static int getTotalPopulationSupply(String ownerName) {
        if (ResearchServerEvents.playerHasCheat(ownerName, "foodforthought"))
            return UnitServerEvents.hardCapPopulation;

        int totalPopulationSupply = 0;
        for (Building building : buildings)
            if (building.ownerName.equals(ownerName) && building.isBuilt)
                totalPopulationSupply += building.popSupply;
        return Math.min(UnitServerEvents.hardCapPopulation, totalPopulationSupply);
    }

    // similar to BuildingClientEvents getPlayerToBuildingRelationship: given a Unit and Building, what is the relationship between them
    public static Relationship getUnitToBuildingRelationship(Unit unit, Building building) {
        if (unit.getOwnerName().equals(building.ownerName))
            return Relationship.OWNED;
        else
            return Relationship.HOSTILE;
    }

    // does the player own one of these buildings?
    public static boolean playerHasFinishedBuilding(String playerName, String buildingName) {
        for (Building building : buildings)
            if (building.name.equals(buildingName) && building.isBuilt && building.ownerName.equals(playerName))
                return true;
        return false;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (!PlayerServerEvents.rtsSyncingEnabled) {
            return;
        }
        for (Building building : buildings)
            BuildingClientboundPacket.placeBuilding(
                building.originPos,
                building.name,
                building.rotation,
                building.ownerName,
                building.blockPlaceQueue.size(),
                building instanceof AbstractBridge bridge && bridge.isDiagonalBridge,
                building.isBuilt,
                building.isUpgraded(),
                building instanceof Portal p ? p.portalType : Portal.PortalType.BASIC,
                true
            );
        System.out.println("Synced " + buildings.size() + " buildings with player logged in");
    }

    // if blocks are destroyed manually by a player then help it along by causing periodic explosions
    @SubscribeEvent
    public static void onPlayerBlockBreak(BlockEvent.BreakEvent evt) {
        if (!evt.getLevel().isClientSide()) {
            for (Building building : buildings)
                if (building.isPosPartOfBuilding(evt.getPos(), true))
                    building.onBlockBreak((ServerLevel) evt.getLevel(), evt.getPos(), true);
        }
    }

    // prevent dungeons spawners from actually spawning
    @SubscribeEvent
    public static void onLivingSpawn(LivingSpawnEvent.SpecialSpawn evt) {
        if (evt.getSpawnReason() == MobSpawnType.SPAWNER) {
            if (evt.getSpawner() != null &&
                evt.getSpawner().getSpawnerBlockEntity() != null) {
                BlockEntity be = evt.getSpawner().getSpawnerBlockEntity();
                BlockPos bp = evt.getSpawner().getSpawnerBlockEntity().getBlockPos();
                if (BuildingUtils.findBuilding(false, bp) instanceof Dungeon ||
                    BuildingUtils.findBuilding(false, bp) instanceof FlameSanctuary)
                    evt.getEntity().discard();
            }
        }
    }
    public static int destroyCheckTicks = 10;
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.level.isClientSide() || evt.level.dimension() != Level.OVERWORLD)
            return;

        serverLevel = (ServerLevel) evt.level;

        // Building sync logic
        buildingSyncTicks -= 1;
        if (buildingSyncTicks <= 0) {
            buildingSyncTicks = BUILDING_SYNC_TICKS_MAX;
            for (Building building : buildings)
                BuildingClientboundPacket.syncBuilding(building.originPos, building.getBlocksPlaced());
        }

        // Destroy check logic, executed every 5 ticks
        destroyCheckTicks -= 1;
        if (destroyCheckTicks <= 0) {
            destroyCheckTicks = 10;

            List<Building> buildingsToDestroy = buildings.stream().filter(Building::shouldBeDestroyed).toList();
            buildings.removeIf(b -> {
                if (b.shouldBeDestroyed()) {
                    if (b instanceof NetherConvertingBuilding nb && nb.getZone() != null) {
                        nb.getZone().startRestoring();
                        saveNetherZones();
                    }
                    FrozenChunkClientboundPacket.setBuildingDestroyedServerside(b.originPos);
                    return true;
                }
                return false;
            });

            for (Building building : buildingsToDestroy)
                building.destroy(serverLevel);
        }

        // Tick remaining buildings
        for (Building building : buildings)
            building.tick(serverLevel);

        // Nether zones logic
        for (NetherZone netherConversionZone : netherZones)
            netherConversionZone.tick(serverLevel);

        int nzSizeBefore = netherZones.size();
        netherZones.removeIf(NetherZone::isDone);
        int nzSizeAfter = netherZones.size();
        if (nzSizeBefore != nzSizeAfter)
            saveNetherZones();
    }


    // cancel all explosion damage to non-building blocks
    // cancel damage to entities and non-building blocks if it came from a non-entity source such as:
    // - building block breaks
    // - beds (vanilla)
    // - respawn anchors (vanilla)
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate evt) {
        Explosion exp = evt.getExplosion();

        GhastUnit ghastUnit = null;
        CreeperUnit creeperUnit = null;
        PillagerUnit pillagerUnit = null;

        if (evt.getExplosion().getSourceMob() instanceof CreeperUnit cUnit) {
            creeperUnit = cUnit;
        } // generic means it was from random blocks broken, so don't consider it or we might keep chaining
        else if (evt.getExplosion().getSourceMob() instanceof PillagerUnit pUnit) {
            pillagerUnit = pUnit;
        }
        else if (exp.getDamageSource() != DamageSource.GENERIC) {
            for (Entity entity : evt.getAffectedEntities()) {
                if (entity instanceof LargeFireball fireball &&
                        fireball.getOwner() instanceof GhastUnit gUnit) {
                    ghastUnit = gUnit;
                    exp.damageSource = new EntityDamageSource("explosion", ghastUnit);
                }
            }
        }

        // set fire to random blocks from a ghast fireball
        if (ghastUnit != null) {
            List<BlockPos> flammableBps = evt.getAffectedBlocks().stream().filter(bp -> {
                BlockState bs = evt.getLevel().getBlockState(bp);
                BlockState bsAbove = evt.getLevel().getBlockState(bp.above());
                return bs.getMaterial().isSolidBlocking() && bsAbove.isAir() ||
                        bsAbove.getBlock() instanceof TallGrassBlock ||
                        bsAbove.getBlock() instanceof RootsBlock;
            }).toList();

            if (flammableBps.size() > 0) {
                Random rand = new Random();
                for (int i = 0; i < GhastUnit.FIREBALL_FIRE_BLOCKS; i++) {
                    BlockPos bp = flammableBps.get(rand.nextInt(flammableBps.size()));
                    evt.getLevel().setBlockAndUpdate(bp.above(), Blocks.FIRE.defaultBlockState());
                }
            }
        }

        if (exp.getExploder() == null && exp.getSourceMob() == null && ghastUnit == null)
            evt.getAffectedEntities().clear();

        // explosive arrows from mounted pillagers
        if (exp.getSourceMob() instanceof PillagerUnit pUnit && pUnit.isPassenger())
            for (Entity entity : evt.getAffectedEntities())
                if (entity instanceof LivingEntity le)
                    le.setHealth(le.getHealth() - 2); // for some reason there's still iframes so we cant use hurt()

        // apply creeper, ghast and mounted pillager attack damage as bonus damage to buildings
        // this is dealt in addition to the actual blocks destroyed by the explosion itself
        if (creeperUnit != null || ghastUnit != null || pillagerUnit != null || exp.getExploder() instanceof PrimedTnt) {
            Set<Building> affectedBuildings = new HashSet<>();
            for (BlockPos bp : evt.getAffectedBlocks()) {
                Building building = BuildingUtils.findBuilding(false, bp);
                if (building != null)
                    affectedBuildings.add(building);
            }
            for (Building building : affectedBuildings) {
                int atkDmg = 0;
                if (ghastUnit != null) {
                    atkDmg = (int) ghastUnit.getUnitAttackDamage();
                } else if (creeperUnit != null) {
                    atkDmg = (int) creeperUnit.getUnitAttackDamage();
                    if (creeperUnit.isPowered())
                        atkDmg *= 2;
                } else if (pillagerUnit != null) {
                    atkDmg = (int) pillagerUnit.getUnitAttackDamage() / 2;
                } else if (exp.getExploder() instanceof PrimedTnt) {
                    atkDmg = TNT_BUILDING_BASE_DAMAGE;
                }

                if (atkDmg > 0) {
                    // all explosion damage will directly hit all occupants at an average of half rate
                    if (building instanceof GarrisonableBuilding garr)
                        for (LivingEntity le : garr.getOccupants())
                            le.hurt(exp.getDamageSource(), random.nextInt(atkDmg + 1));

                    if (building instanceof AbstractBridge)
                        atkDmg /= 2;

                    building.destroyRandomBlocks(atkDmg);
                }

            }
        }
        // don't do any block damage apart from the scripted building damage above or damage to leaves/tnt
        evt.getAffectedBlocks().removeIf(bp -> {
            BlockState bs = evt.getLevel().getBlockState(bp);
            return !(bs.getBlock() instanceof LeavesBlock) &&
                    !(bs.getBlock() instanceof TntBlock);
        });
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent evt) {
        if (BuildingUtils.isPosInsideAnyBuilding(evt.getEntity().getLevel().isClientSide(), evt.getEntity().getOnPos()))
            evt.setCanceled(true);
    }

    @SubscribeEvent
    public static void onCropTrample(BlockEvent.FarmlandTrampleEvent evt) {
        if (BuildingUtils.isPosInsideAnyBuilding(evt.getEntity().getLevel().isClientSide(), evt.getPos()))
            evt.setCanceled(true);
    }

    public static void replaceClientBuilding(BlockPos buildingPos) {
        if (!PlayerServerEvents.rtsSyncingEnabled)
            return;
        for (Building building : buildings) {
            if (building.originPos.equals(buildingPos)) {
                BuildingClientboundPacket.placeBuilding(
                        building.originPos,
                        building.name,
                        building.rotation,
                        building.ownerName,
                        building.blockPlaceQueue.size(),
                        building instanceof AbstractBridge bridge && bridge.isDiagonalBridge,
                        building.isBuilt,
                        building.isUpgraded(),
                        building instanceof Portal p ? p.portalType : Portal.PortalType.BASIC,
                        false
                );
                return;
            }
        }
    }

    private static final float MIN_NETHER_BLOCKS_PERCENT = 0.8f;

    public static boolean isOnNetherBlocks(List<BuildingBlock> blocks, BlockPos originPos, ServerLevel level) {
        int netherBlocksBelow = 0;
        int blocksBelow = 0;
        for (BuildingBlock block : blocks) {
            if (block.getBlockPos().getY() == 0 && level != null) {
                BlockPos bp = block.getBlockPos().offset(originPos).offset(0,1,0);
                BlockState bs = block.getBlockState(); // building block
                BlockState bsBelow = level.getBlockState(bp.below()); // world block

                if (bs.getMaterial().isSolid()) {
                    blocksBelow += 1;
                    if (NetherBlocks.isNetherBlock(level, bp.below()))
                        netherBlocksBelow += 1;
                }
            }
        }
        if (blocksBelow <= 0) return false; // avoid division by 0
        return ((float) netherBlocksBelow / (float) blocksBelow) > MIN_NETHER_BLOCKS_PERCENT;
    }
}
