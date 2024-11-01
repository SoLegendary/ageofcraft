package com.solegendary.reignofnether.building;

// class for static building functions

import com.solegendary.reignofnether.building.buildings.monsters.*;
import com.solegendary.reignofnether.building.buildings.piglins.*;
import com.solegendary.reignofnether.building.buildings.piglins.BlackstoneBridge;
import com.solegendary.reignofnether.building.buildings.monsters.SpruceBridge;
import com.solegendary.reignofnether.building.buildings.villagers.OakStockpile;
import com.solegendary.reignofnether.building.buildings.villagers.OakBridge;
import com.solegendary.reignofnether.building.buildings.villagers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class BuildingUtils {

    public static int getTotalCompletedBuildingsOwned(boolean isClientSide, String ownerName) {
        List<Building> buildings;
        if (isClientSide)
            buildings = BuildingClientEvents.getBuildings();
        else
            buildings = BuildingServerEvents.getBuildings();

        return buildings.stream().filter(b -> b.isBuilt && b.ownerName.equals(ownerName)).toList().size();
    }

    public static boolean isBuildingBuildable(boolean isClientSide, Building building) {
        if (isClientSide)
            return BuildingClientEvents.getBuildings().stream().map(b -> b.originPos).toList().contains(building.originPos) &&
                    building.getBlocksPlaced() < building.getBlocksTotal();
        else
            return BuildingServerEvents.getBuildings().stream().map(b -> b.originPos).toList().contains(building.originPos) &&
                    building.getBlocksPlaced() < building.getBlocksTotal();
    }

    public static boolean doesPlayerOwnCapitol(boolean isClientSide, String playerName) {
        List<Building> buildings = isClientSide ? BuildingClientEvents.getBuildings() : BuildingServerEvents.getBuildings();
        for (Building building : buildings)
            if (building.isCapitol && building.ownerName.equals(playerName))
                return true;
        return false;
    }

    // returns a list of BPs that may reside in unique chunks for fog of war calcs
    public static HashSet<BlockPos> getUniqueChunkBps(Building building) {
        AABB aabb = new AABB(
                building.minCorner,
                building.maxCorner.offset(1, 1, 1)
        );

        HashSet<BlockPos> uniqueBps = new HashSet<>();
        int minX = (int) Math.floor(aabb.minX / 16) * 16;
        int maxX = (int) Math.ceil(aabb.maxX / 16) * 16;
        int minZ = (int) Math.floor(aabb.minZ / 16) * 16;
        int maxZ = (int) Math.ceil(aabb.maxZ / 16) * 16;

        // Add unique chunk positions by stepping through chunks
        for (int x = minX; x <= maxX; x += 16) {
            for (int z = minZ; z <= maxZ; z += 16) {
                uniqueBps.add(new BlockPos(x, (int) aabb.minY, z));
            }
        }

        return uniqueBps;
    }


    // given a string name return a new instance of that building
    public static Building getNewBuilding(String buildingName, Level level, BlockPos pos, Rotation rotation, String ownerName, boolean isDiagonalBridge) {
        if (buildingName.toLowerCase().contains("bridge"))
            ownerName = "";

        Building building = null;
        switch(buildingName) {
            case OakBridge.buildingName -> building = new OakBridge(level, pos, rotation, ownerName, isDiagonalBridge);
            case SpruceBridge.buildingName -> building = new SpruceBridge(level, pos, rotation, ownerName, isDiagonalBridge);
            case BlackstoneBridge.buildingName -> building = new BlackstoneBridge(level, pos, rotation, ownerName, isDiagonalBridge);

            case OakStockpile.buildingName -> building = new OakStockpile(level, pos, rotation, ownerName);
            case SpruceStockpile.buildingName -> building = new SpruceStockpile(level, pos, rotation, ownerName);
            case VillagerHouse.buildingName -> building = new VillagerHouse(level, pos, rotation, ownerName);
            case Graveyard.buildingName -> building = new Graveyard(level, pos, rotation, ownerName);
            case WheatFarm.buildingName -> building = new WheatFarm(level, pos, rotation, ownerName);
            case Laboratory.buildingName -> building = new Laboratory(level, pos, rotation, ownerName);
            case Barracks.buildingName -> building = new Barracks(level, pos, rotation, ownerName);
            case PumpkinFarm.buildingName -> building = new PumpkinFarm(level, pos, rotation, ownerName);
            case HauntedHouse.buildingName -> building = new HauntedHouse(level, pos, rotation, ownerName);
            case Blacksmith.buildingName -> building = new Blacksmith(level, pos, rotation, ownerName);
            case TownCentre.buildingName -> building = new TownCentre(level, pos, rotation, ownerName);
            case IronGolemBuilding.buildingName -> building = new IronGolemBuilding(level, pos, rotation, ownerName);
            case Mausoleum.buildingName -> building = new Mausoleum(level, pos, rotation, ownerName);
            case SculkCatalyst.buildingName -> building = new SculkCatalyst(level, pos, rotation, ownerName);
            case SpiderLair.buildingName -> building = new SpiderLair(level, pos, rotation, ownerName);
            case ArcaneTower.buildingName -> building = new ArcaneTower(level, pos, rotation, ownerName);
            case Library.buildingName -> building = new Library(level, pos, rotation, ownerName);
            case Dungeon.buildingName -> building = new Dungeon(level, pos, rotation, ownerName);
            case Watchtower.buildingName -> building = new Watchtower(level, pos, rotation, ownerName);
            case DarkWatchtower.buildingName -> building = new DarkWatchtower(level, pos, rotation, ownerName);
            case Castle.buildingName -> building = new Castle(level, pos, rotation, ownerName);
            case Stronghold.buildingName -> building = new Stronghold(level, pos, rotation, ownerName);
            case CentralPortal.buildingName -> building = new CentralPortal(level, pos, rotation, ownerName);
            case Portal.buildingName,
                 Portal.buildingNameMilitary,
                 Portal.buildingNameCivilian,
                 Portal.buildingNameTransport -> building = new Portal(level, pos, rotation, ownerName);
            case NetherwartFarm.buildingName -> building = new NetherwartFarm(level, pos, rotation, ownerName);
            case Bastion.buildingName -> building = new Bastion(level, pos, rotation, ownerName);
            case HoglinStables.buildingName -> building = new HoglinStables(level, pos, rotation, ownerName);
            case FlameSanctuary.buildingName -> building = new FlameSanctuary(level, pos, rotation, ownerName);
            case WitherShrine.buildingName -> building = new WitherShrine(level, pos, rotation, ownerName);
            case Fortress.buildingName -> building = new Fortress(level, pos, rotation, ownerName);
        }
        if (building != null)
            building.setLevel(level);
        return building;
    }

    // note originPos may be an air block
    public static Building findBuilding(boolean isClientSide, BlockPos pos) {
        List<Building> buildings = isClientSide ? BuildingClientEvents.getBuildings() : BuildingServerEvents.getBuildings();

        for (Building building : buildings)
            if (building.originPos.equals(pos) || building.isPosInsideBuilding(pos))
                return building;
        return null;
    }

    // functions for corners/centrePos given only blocks
    // if you have access to the Building itself, you should use .minCorner, .maxCorner and .centrePos
    public static BlockPos getMinCorner(ArrayList<BuildingBlock> blocks) {
        MinMaxValues minMax = calculateMinMax(blocks);
        return new BlockPos(minMax.minX, minMax.minY, minMax.minZ);
    }

    public static BlockPos getMaxCorner(ArrayList<BuildingBlock> blocks) {
        MinMaxValues minMax = calculateMinMax(blocks);
        return new BlockPos(minMax.maxX, minMax.maxY, minMax.maxZ);
    }

    public static BlockPos getCentrePos(ArrayList<BuildingBlock> blocks) {
        MinMaxValues minMax = calculateMinMax(blocks);
        return new BlockPos(
                (minMax.minX + minMax.maxX) / 2,
                (minMax.minY + minMax.maxY) / 2,
                (minMax.minZ + minMax.maxZ) / 2
        );
    }

    private static MinMaxValues calculateMinMax(ArrayList<BuildingBlock> blocks) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BuildingBlock block : blocks) {
            BlockPos pos = block.getBlockPos();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;

            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        return new MinMaxValues(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static class MinMaxValues {
        int minX, minY, minZ;
        int maxX, maxY, maxZ;

        public MinMaxValues(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }



    public static Vec3i getBuildingSize(ArrayList<BuildingBlock> blocks) {
        BlockPos min = getMinCorner(blocks);
        BlockPos max = getMaxCorner(blocks);
        return new Vec3i(
                max.getX() - min.getX(),
                max.getY() - min.getY(),
                max.getZ() - min.getZ()
        );
    }

    // get BlockPos values with absolute world positions
    public static ArrayList<BuildingBlock> getAbsoluteBlockData(ArrayList<BuildingBlock> staticBlocks, LevelAccessor level, BlockPos originPos, Rotation rotation) {
        ArrayList<BuildingBlock> blocks = new ArrayList<>();

        for (BuildingBlock block : staticBlocks) {
            block = block.rotate(level, rotation);
            BlockPos bp = block.getBlockPos();

            block.setBlockPos(new BlockPos(
                    bp.getX() + originPos.getX(),
                    bp.getY() + originPos.getY() + 1,
                    bp.getZ() + originPos.getZ()
            ));
            blocks.add(block);
        }
        return blocks;
    }

    // returns whether the given pos is part of ANY building in the level
    // WARNING: very processing expensive!
    public static boolean isPosPartOfAnyBuilding(boolean isClientSide, BlockPos bp, boolean onlyPlacedBlocks, int range) {
        List<Building> buildings = isClientSide
                ? BuildingClientEvents.getBuildings()
                : BuildingServerEvents.getBuildings();

        // Precompute range squared to avoid repeated calculation
        int rangeSquared = range * range;

        return buildings.stream().anyMatch(building ->
                (range == 0 || bp.distSqr(building.centrePos) < rangeSquared) &&
                        building.isPosPartOfBuilding(bp, onlyPlacedBlocks)
        );
    }


    // returns whether the given pos is part of ANY building in the level
    public static boolean isPosInsideAnyBuilding(boolean isClientSide, BlockPos bp) {
        List<Building> buildings;
        if (isClientSide)
            buildings = BuildingClientEvents.getBuildings();
        else
            buildings = BuildingServerEvents.getBuildings();

        for (Building building : buildings)
            if (building.isPosInsideBuilding(bp))
                return true;
        return false;
    }

    public static Building findClosestBuilding(boolean isClientSide, Vec3 pos, Predicate<Building> condition) {
        List<Building> buildings;
        if (isClientSide)
            buildings = BuildingClientEvents.getBuildings();
        else
            buildings = BuildingServerEvents.getBuildings();

        double closestDist = 9999;
        Building closestBuilding = null;
        for (Building building : buildings) {
            if (condition.test(building)) {
                BlockPos bp = building.centrePos;
                Vec3 bpVec3 = new Vec3(bp.getX(), bp.getY(), bp.getZ());
                double dist = bpVec3.distanceToSqr(pos);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestBuilding = building;
                }
            }
        }
        return closestBuilding;
    }

    public static boolean isInNetherRange(boolean isClientSide, BlockPos bp) {
        List<Building> buildings = getBuildingsList(isClientSide);

        for (Building building : buildings) {
            if (building instanceof NetherConvertingBuilding netherBuilding) {
                double maxRangeSquared = Math.pow(netherBuilding.getMaxRange(), 2);
                if (bp.distSqr(building.centrePos) <= maxRangeSquared) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWithinRangeOfMaxedCatalyst(LivingEntity entity) {
        List<Building> buildings = getBuildingsList(entity.level.isClientSide());

        double maxCatalystRangeSquared = SculkCatalyst.ESTIMATED_RANGE * SculkCatalyst.ESTIMATED_RANGE;

        for (Building building : buildings) {
            if (building instanceof SculkCatalyst sc) {
                if (entity.distanceToSqr(Vec3.atCenterOf(sc.centrePos)) < maxCatalystRangeSquared &&
                        sc.getUncappedNightRange() >= SculkCatalyst.nightRangeMax * 1.5f) {
                    return true;
                }
            }
        }
        return false;
    }

    // Helper method to get the buildings list based on client or server side.
    private static List<Building> getBuildingsList(boolean isClientSide) {
        return isClientSide
                ? BuildingClientEvents.getBuildings()
                : BuildingServerEvents.getBuildings();
    }

}
