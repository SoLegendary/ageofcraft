package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MyRenderer;
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

public class NetherwartFarm extends Building {

    public final static String buildingName = "Netherwart Farm";
    public final static String structureName = "netherwart_farm";
    public final static ResourceCost cost = ResourceCosts.NETHERWART_FARM;

    public NetherwartFarm(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.NETHER_WART_BLOCK;
        this.icon = new ResourceLocation("minecraft", "textures/block/nether_wart_stage2.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.WARPED_STEM);

        this.explodeChance = 0;
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                NetherwartFarm.buildingName,
                new ResourceLocation("minecraft", "textures/block/nether_wart_stage2.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == NetherwartFarm.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(CentralPortal.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(NetherwartFarm.class),
                null,
                List.of(
                        FormattedCharSequence.forward(NetherwartFarm.buildingName, Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE001  " + cost.wood + "  +  " + ResourceCosts.REPLANT_WOOD_COST + "  per  crop  planted", MyRenderer.iconStyle),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A field of netherwart that can be farmed collect food.", Style.EMPTY),
                        FormattedCharSequence.forward("Workers automatically use wood to replant while working.", Style.EMPTY)
                ),
                null
        );
    }
}
