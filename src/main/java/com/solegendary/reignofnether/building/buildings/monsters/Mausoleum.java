package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.unit.units.monsters.ZombieVillagerProd;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Mausoleum extends ProductionBuilding implements NightSource {

    public final static String buildingName = "Mausoleum";
    public final static String structureName = "mausoleum";
    public final static ResourceCost cost = ResourceCosts.MAUSOLEUM;
    public final static int nightRange = 80;

    private final Set<BlockPos> nightBorderBps = new HashSet<>();

    public Mausoleum(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), true);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.blocks = getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation);
        this.portraitBlock = Blocks.DEEPSLATE_TILES;
        this.icon = new ResourceLocation("minecraft", "textures/block/deepslate_tiles.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.33f;
        this.canAcceptResources = true;

        this.startingBlockTypes.add(Blocks.STONE);
        this.startingBlockTypes.add(Blocks.STONE_BRICK_STAIRS);
        this.startingBlockTypes.add(Blocks.STONE_BRICKS);
        this.startingBlockTypes.add(Blocks.STONE_BRICK_STAIRS);

        if (level.isClientSide())
            this.productionButtons = List.of(
                ZombieVillagerProd.getStartButton(this, Keybindings.keyQ)
            );
        updateNightBorderBps();
    }

    public int getNightRange() { return nightRange; }

    @Override
    public void updateNightBorderBps() {
        this.nightBorderBps.clear();
        this.nightBorderBps.addAll(MiscUtil.getNightCircleBlocks(centrePos,
                getNightRange() - TimeClientEvents.VISIBLE_BORDER_ADJ, level));
    }

    @Override
    public Set<BlockPos> getNightBorderBps() {
        return nightBorderBps;
    }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (tickLevel.isClientSide && tickAgeAfterBuilt > 0 && tickAgeAfterBuilt % 100 == 0)
            updateNightBorderBps();
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            Mausoleum.buildingName,
            new ResourceLocation("minecraft", "textures/block/deepslate_tiles.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == Mausoleum.class,
            () -> false,
            () -> true,
            () -> BuildingClientEvents.setBuildingToPlace(Mausoleum.class),
            null,
            List.of(
                FormattedCharSequence.forward(Mausoleum.buildingName + " (Capitol)", Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedPop(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("A tomb of the dead that produces zombie villagers.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Distorts time to midnight within a " + nightRange + " block radius.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("You may only have one capitol building at any time.", Style.EMPTY)
            ),
            null
        );
    }
}