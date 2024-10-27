package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
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

public class WheatFarm extends Building {

    public final static String buildingName = "Wheat Farm";
    public final static String structureName = "wheat_farm";
    public final static ResourceCost cost = ResourceCosts.WHEAT_FARM;

    private static final int ICE_CHECK_TICKS_MAX = 100;
    private int ticksToNextIceCheck = ICE_CHECK_TICKS_MAX;

    public WheatFarm(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.HAY_BLOCK;
        this.icon = new ResourceLocation("minecraft", "textures/block/hay_block_side.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.OAK_LOG);

        this.explodeChance = 0;
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                WheatFarm.buildingName,
                new ResourceLocation("minecraft", "textures/block/hay_block_side.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == WheatFarm.class,
                () -> !TutorialClientEvents.isAtOrPastStage(TutorialStage.EXPLAIN_BUILDINGS),
                () -> BuildingClientEvents.hasFinishedBuilding(TownCentre.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(WheatFarm.class),
                null,
                List.of(
                        FormattedCharSequence.forward(WheatFarm.buildingName, Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE001  " + cost.wood + "  +  " + ResourceCosts.REPLANT_WOOD_COST + "  per  crop  planted", MyRenderer.iconStyle),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A wheat field that be can tilled to collect food.", Style.EMPTY),
                        FormattedCharSequence.forward("Workers automatically use wood to replant seeds while working.", Style.EMPTY)
                ),
                null
        );
    }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (!tickLevel.isClientSide()) {
            ticksToNextIceCheck -= 1;
            if (ticksToNextIceCheck <= 0) {
                for (BuildingBlock bb : blocks)
                    if (tickLevel.getBlockState(bb.getBlockPos()).getBlock() == Blocks.ICE)
                        tickLevel.setBlockAndUpdate(bb.getBlockPos(), Blocks.WATER.defaultBlockState());
                ticksToNextIceCheck = ICE_CHECK_TICKS_MAX;
            }
        }
    }
}
