package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchGolemSmithing;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.IronGolemProd;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class IronGolemBuilding extends Building {

    public final static String buildingName = "Iron Golem";
    public final static String structureName = "iron_golem";
    public final static ResourceCost cost = ResourceCosts.IRON_GOLEM_BUILDING;

    public IronGolemBuilding(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.IRON_BLOCK;
        this.icon = new ResourceLocation("minecraft", "textures/block/iron_block.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 3.0f;

        this.startingBlockTypes.add(Blocks.JUNGLE_FENCE);
    }

    public Faction getFaction() {return Faction.VILLAGERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    @Override
    public void onBuilt() {
        super.onBuilt();
        if (!this.getLevel().isClientSide()) {
            this.destroy((ServerLevel) this.getLevel());
            Entity entity = EntityRegistrar.IRON_GOLEM_UNIT.get().spawn((ServerLevel) this.getLevel(), null,
                    null,
                    this.centrePos.offset(0,-1,0),
                    MobSpawnType.SPAWNER,
                    true,
                    false
            );
            if (entity instanceof Unit unit)
                unit.setOwnerName(ownerName);
        }
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            IronGolemBuilding.buildingName,
            new ResourceLocation("minecraft", "textures/block/iron_block.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == IronGolemBuilding.class,
            TutorialClientEvents::isEnabled,
            () -> ResearchClient.hasResearch(ResearchGolemSmithing.itemName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(IronGolemBuilding.class),
            null,
            List.of(
                FormattedCharSequence.forward(IronGolemBuilding.buildingName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedPopAndTime(IronGolemProd.cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("An Iron Golem that can be built in the field.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Requires research at a Blacksmith", Style.EMPTY)
            ),
            null
        );
    }
}
