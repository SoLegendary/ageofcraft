package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.piglins.GruntProd;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class CentralPortal extends ProductionBuilding implements NetherConvertingBuilding {

    public final static String buildingName = "Central Portal";
    public final static String structureName = "central_portal";
    public final static ResourceCost cost = ResourceCosts.CENTRAL_PORTAL;

    public NetherZone netherConversionZone = null;

    @Override public double getMaxRange() { return 30; }
    @Override public double getStartingRange() { return 6; }
    @Override public NetherZone getZone() { return netherConversionZone; }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);

        if (!this.getLevel().isClientSide() && this.getBlocksPlaced() >= getBlocksTotal()) {
            BlockPos bp;
            if (this.rotation == Rotation.CLOCKWISE_90 ||
                this.rotation == Rotation.COUNTERCLOCKWISE_90) {
                bp = this.centrePos.offset(0,-1,0);
            } else {
                bp = this.centrePos.offset(-1,0,0);
            }
            if (this.getLevel().getBlockState(bp).isAir())
                this.getLevel().setBlockAndUpdate(bp, Blocks.FIRE.defaultBlockState());
        }
    }

    public CentralPortal(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), true);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.OBSIDIAN;
        this.icon = new ResourceLocation("minecraft", "textures/block/obsidian.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.4f;
        this.canAcceptResources = true;

        this.startingBlockTypes.add(Blocks.NETHER_BRICKS);

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                    GruntProd.getStartButton(this, Keybindings.keyQ)
            );
    }

    @Override
    public void setNetherZone(NetherZone nz) {
        if (netherConversionZone == null) {
            netherConversionZone = nz;
            if (!level.isClientSide()) {
                BuildingServerEvents.netherZones.add(netherConversionZone);
                BuildingServerEvents.saveNetherZones();
            }
        }
    }

    @Override
    public void onBuilt() {
        super.onBuilt();
        setNetherZone(new NetherZone(centrePos.offset(0,-6,0), getMaxRange(), getStartingRange()));
    }

    @Override
    public boolean canDestroyBlock(BlockPos relativeBp) {
        BlockPos worldBp = relativeBp.offset(this.originPos);
        Block block = this.getLevel().getBlockState(worldBp).getBlock();
        return block != Blocks.OBSIDIAN && block != Blocks.NETHER_PORTAL;
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                CentralPortal.buildingName,
                new ResourceLocation("minecraft", "textures/block/obsidian.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == CentralPortal.class,
                () -> false,
                () -> true,
                () -> BuildingClientEvents.setBuildingToPlace(CentralPortal.class),
                null,
                List.of(
                        FormattedCharSequence.forward(CentralPortal.buildingName + " (Capitol)", Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedPop(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("The primary portal to transport piglin grunts from the nether.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("You may only have one capitol building at any time.", Style.EMPTY)
                ),
                null
        );
    }

    @Override
    public BlockPos getIndoorSpawnPoint(ServerLevel level) {
        return super.getIndoorSpawnPoint(level).offset(0,-5,0);
    }
}