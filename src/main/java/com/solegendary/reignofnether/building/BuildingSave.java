package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

// contains all necessary data for a restarted server to recreate all buildings in the level
public class BuildingSave {

    public BlockPos originPos;
    public Level level;
    public String name;
    public String ownerName;
    public Rotation rotation;
    public BlockPos rallyPoint;
    public boolean isBuilt;
    public boolean isDiagonalBridge;
    public boolean isUpgraded; // castle flag, lab rod, etc.
    Portal.PortalType portalType;

    public BuildingSave(BlockPos originPos, Level level, String name, String ownerName, Rotation rotation, BlockPos rallyPoint,
                        boolean isDiagonalBridge, boolean isBuilt, boolean isUpgraded, Portal.PortalType portalType) {
        this.originPos = originPos;
        this.level = level;
        this.name = name;
        this.ownerName = ownerName;
        this.rotation = rotation;
        this.rallyPoint = rallyPoint;
        this.isDiagonalBridge = isDiagonalBridge;
        this.isBuilt = isBuilt;
        this.isUpgraded = isUpgraded;
        this.portalType = portalType;
    }
}
