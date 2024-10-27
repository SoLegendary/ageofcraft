package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.buildings.shared.AbstractStockpile;
import com.solegendary.reignofnether.building.buildings.villagers.TownCentre;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.resources.ResourceName;
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

public class SpruceStockpile extends AbstractStockpile {

    public final static String buildingName = "Dark Stockpile";
    public final static String structureName = "stockpile_spruce";
    public final static ResourceCost cost = ResourceCosts.STOCKPILE;
    public ResourceName mostAbundantNearbyResource = ResourceName.NONE;

    public SpruceStockpile(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(buildingName, level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);

        this.startingBlockTypes.add(Blocks.SPRUCE_LOG);
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                SpruceStockpile.buildingName,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/chest.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == SpruceStockpile.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(TownCentre.buildingName) ||
                        BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(SpruceStockpile.class),
                null,
                List.of(
                        FormattedCharSequence.forward(SpruceStockpile.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Storage for units and players to drop off resources", Style.EMPTY)
                ),
                null
        );
    }
}
