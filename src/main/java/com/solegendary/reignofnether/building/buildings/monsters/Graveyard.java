package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.units.monsters.*;
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
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Graveyard extends ProductionBuilding {

    public final static String buildingName = "Graveyard";
    public final static String structureName = "graveyard";
    public final static ResourceCost cost = ResourceCosts.GRAVEYARD;

    public Graveyard(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.MOSSY_STONE_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/mossy_stone_bricks.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.DEEPSLATE_BRICKS);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                ZombieProd.getStartButton(this, Keybindings.keyQ),
                HuskProd.getStartButton(this, Keybindings.keyQ),
                DrownedProd.getStartButton(this, Keybindings.keyW),
                SkeletonProd.getStartButton(this, Keybindings.keyE),
                StrayProd.getStartButton(this, Keybindings.keyE)
            );
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            Graveyard.buildingName,
            new ResourceLocation("minecraft", "textures/block/mossy_stone_bricks.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == Graveyard.class,
            () -> false,
            () -> BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(Graveyard.class),
            null,
            List.of(
                FormattedCharSequence.forward(Graveyard.buildingName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("A field of the dead that can raise Zombies and Skeletons", Style.EMPTY)
            ),
            null
        );
    }
}
