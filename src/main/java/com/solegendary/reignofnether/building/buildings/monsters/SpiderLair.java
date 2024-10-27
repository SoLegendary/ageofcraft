package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.monsters.PoisonSpiderProd;
import com.solegendary.reignofnether.unit.units.monsters.SpiderProd;
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

public class SpiderLair extends ProductionBuilding {

    public final static String buildingName = "Spider Lair";
    public final static String structureName = "spider_lair";
    public final static ResourceCost cost = ResourceCosts.SPIDER_LAIR;

    public SpiderLair(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.COBWEB;
        this.icon = new ResourceLocation("minecraft", "textures/block/cobweb.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;

        this.startingBlockTypes.add(Blocks.DEEPSLATE);
        this.startingBlockTypes.add(Blocks.COBBLED_DEEPSLATE);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                SpiderProd.getStartButton(this, Keybindings.keyQ),
                PoisonSpiderProd.getStartButton(this, Keybindings.keyW)
            );
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                SpiderLair.buildingName,
                new ResourceLocation("minecraft", "textures/block/cobweb.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == SpiderLair.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(Laboratory.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(SpiderLair.class),
                null,
                List.of(
                        FormattedCharSequence.forward(SpiderLair.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A silken cave to grow spiders, both large and poisonous.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Requires a Laboratory.", Style.EMPTY)
                ),
                null
        );
    }
}
