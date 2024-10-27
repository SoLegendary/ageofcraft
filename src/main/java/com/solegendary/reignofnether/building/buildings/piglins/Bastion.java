package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchBruteShields;
import com.solegendary.reignofnether.research.researchItems.ResearchHeavyTridents;
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

public class Bastion extends ProductionBuilding implements GarrisonableBuilding {

    public final static String buildingName = "Bastion";
    public final static String structureName = "bastion";
    public final static ResourceCost cost = ResourceCosts.BASTION;

    private final static int MAX_OCCUPANTS = 4;

    public Bastion(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.CHISELED_POLISHED_BLACKSTONE;
        this.icon = new ResourceLocation("minecraft", "textures/block/chiseled_polished_blackstone.png");

        this.canSetRallyPoint = false;

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.9f;

        this.startingBlockTypes.add(Blocks.POLISHED_BLACKSTONE);
        this.startingBlockTypes.add(Blocks.POLISHED_BLACKSTONE_BRICKS);
        this.startingBlockTypes.add(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                    ResearchBruteShields.getStartButton(this, Keybindings.keyQ),
                    ResearchHeavyTridents.getStartButton(this, Keybindings.keyW)
            );
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    // don't use this for abilities as it may not be balanced
    public int getAttackRange() { return 24; }
    // bonus for units attacking garrisoned units
    public int getExternalAttackRangeBonus() { return 10; }

    public boolean canDestroyBlock(BlockPos relativeBp) {
        return relativeBp.getY() != 10 &&
                relativeBp.getY() != 11;
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                Bastion.buildingName,
                new ResourceLocation("minecraft", "textures/block/chiseled_polished_blackstone.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == Bastion.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(Portal.buildingName) ||
                        BuildingClientEvents.hasFinishedBuilding("Civilian Portal") ||
                        BuildingClientEvents.hasFinishedBuilding("Military Portal") ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(Bastion.class),
                null,
                List.of(
                        FormattedCharSequence.forward(Bastion.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A fortified barracks to house military piglins,", Style.EMPTY),
                        FormattedCharSequence.forward("enabling them to be produced at military portals.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Also garrisons up to 5 units.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Requires a Basic Portal", Style.EMPTY)
                ),
                null
        );
    }

    @Override
    public BlockPos getEntryPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2,11,2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2,11,2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2,11,-2);
        } else {
            return new BlockPos(2,11,-2);
        }
    }

    @Override
    public BlockPos getExitPosition() {
        if (this.rotation == Rotation.NONE) {
            return new BlockPos(2,1,2);
        } else if (this.rotation == Rotation.CLOCKWISE_90) {
            return new BlockPos(-2,1,2);
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            return new BlockPos(-2,1,-2);
        } else {
            return new BlockPos(2,1,-2);
        }
    }

    @Override
    public boolean isFull() { return GarrisonableBuilding.getNumOccupants(this) >= MAX_OCCUPANTS; }
}
