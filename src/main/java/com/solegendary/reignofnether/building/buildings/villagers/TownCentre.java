package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.units.villagers.VillagerProd;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class TownCentre extends ProductionBuilding {

    public final static String buildingName = "Town Centre";
    public final static String structureName = "town_centre";
    public final static ResourceCost cost = ResourceCosts.TOWN_CENTRE;

    public TownCentre(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), true);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.POLISHED_GRANITE;
        this.icon = new ResourceLocation("minecraft", "textures/block/polished_granite.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.4f;
        this.canAcceptResources = true;

        this.startingBlockTypes.add(Blocks.STONE_BRICK_STAIRS);
        this.startingBlockTypes.add(Blocks.GRASS_BLOCK);
        this.startingBlockTypes.add(Blocks.POLISHED_ANDESITE_STAIRS);

        if (level.isClientSide())
            this.productionButtons = List.of(
                VillagerProd.getStartButton(this, Keybindings.keyQ)
            );
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                TownCentre.buildingName,
                new ResourceLocation("minecraft", "textures/block/polished_granite.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == TownCentre.class,
                () -> false,
                () -> true,
                () -> BuildingClientEvents.setBuildingToPlace(TownCentre.class),
                null,
                List.of(
                        FormattedCharSequence.forward(TownCentre.buildingName + " (Capitol)", Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedPop(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A gazebo at the centre of your village that produces villagers.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("You may only have one capitol building at any time.", Style.EMPTY)
                ),
                null
        );
    }
}
