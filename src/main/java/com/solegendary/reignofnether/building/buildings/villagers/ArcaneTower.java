package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.unit.units.villagers.EvokerProd;
import com.solegendary.reignofnether.unit.units.villagers.WitchProd;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class ArcaneTower extends ProductionBuilding {

    public final static String buildingName = "Arcane Tower";
    public final static String structureName = "arcane_tower";
    public final static ResourceCost cost = ResourceCosts.ARCANE_TOWER;

    public ArcaneTower(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.AMETHYST_BLOCK;
        this.icon = new ResourceLocation("minecraft", "textures/block/amethyst_block.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.STONE_BRICKS);
        this.startingBlockTypes.add(Blocks.ANDESITE_WALL);
        this.startingBlockTypes.add(Blocks.POLISHED_ANDESITE_STAIRS);
        this.startingBlockTypes.add(Blocks.POLISHED_ANDESITE);

        this.buildTimeModifier = 0.7f;
        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                WitchProd.getStartButton(this, Keybindings.keyQ),
                EvokerProd.getStartButton(this, Keybindings.keyW)
            );
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            ArcaneTower.buildingName,
            new ResourceLocation("minecraft", "textures/block/amethyst_block.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == ArcaneTower.class,
            TutorialClientEvents::isEnabled,
            () -> BuildingClientEvents.hasFinishedBuilding(Barracks.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(ArcaneTower.class),
            null,
            List.of(
                FormattedCharSequence.forward(ArcaneTower.buildingName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("A magical tower that is home to Witches and Evokers.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Requires a Barracks.", Style.EMPTY)
            ),
            null
        );
    }

    @Override
    public BlockPos getIndoorSpawnPoint(ServerLevel level) {
        return super.getIndoorSpawnPoint(level).offset(0,-10,0);
    }
}
