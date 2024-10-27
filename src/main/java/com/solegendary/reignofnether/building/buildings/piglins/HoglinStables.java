package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchHoglinCavalry;
import com.solegendary.reignofnether.resources.ResourceCost;
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

public class HoglinStables extends ProductionBuilding {

    public final static String buildingName = "Hoglin Stables";
    public final static String structureName = "hoglin_stables";
    public final static ResourceCost cost = ResourceCosts.HOGLIN_STABLES;

    public HoglinStables(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.CRIMSON_STEM;
        this.icon = new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/crimson_stem.png");

        this.canSetRallyPoint = false;

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.NETHER_BRICK_FENCE);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                ResearchHoglinCavalry.getStartButton(this, Keybindings.keyQ)
            );
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            HoglinStables.buildingName,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/crimson_stem.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == HoglinStables.class,
            () -> false,
            () -> BuildingClientEvents.hasFinishedBuilding(Portal.buildingName) ||
                    BuildingClientEvents.hasFinishedBuilding("Civilian Portal") ||
                    BuildingClientEvents.hasFinishedBuilding("Military Portal") ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(HoglinStables.class),
            null,
            List.of(
                FormattedCharSequence.forward(HoglinStables.buildingName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("A shelter to keep Hoglins stabled in the overworld,", Style.EMPTY),
                FormattedCharSequence.forward("enabling their production at military portals.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Requires a Basic Portal", Style.EMPTY)
            ),
            null
        );
    }
}
