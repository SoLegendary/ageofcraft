package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.*;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.ability.abilities.CallLightning;
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

public class Laboratory extends ProductionBuilding {

    public final static String buildingName = "Laboratory";
    public final static String structureName = "laboratory";
    public final static String upgradedStructureName = "laboratory_lightning";
    public final static ResourceCost cost = ResourceCosts.LABORATORY;

    public Laboratory(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.BREWING_STAND;
        this.icon = new ResourceLocation("minecraft", "textures/block/brewing_stand.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.85f;

        this.canSetRallyPoint = false;

        this.startingBlockTypes.add(Blocks.SPRUCE_PLANKS);
        this.startingBlockTypes.add(Blocks.BLACKSTONE);

        Ability callLightning = new CallLightning(this);
        this.abilities.add(callLightning);

        if (level.isClientSide()) {
            this.productionButtons = Arrays.asList(
                ResearchHusks.getStartButton(this, Keybindings.keyQ),
                ResearchDrowned.getStartButton(this, Keybindings.keyW),
                ResearchStrays.getStartButton(this, Keybindings.keyE),
                ResearchSpiderJockeys.getStartButton(this, Keybindings.keyR),
                ResearchPoisonSpiders.getStartButton(this, Keybindings.keyT),
                ResearchLabLightningRod.getStartButton(this, Keybindings.keyY),
                ResearchSilverfish.getStartButton(this, Keybindings.keyU),
                ResearchSculkAmplifiers.getStartButton(this, Keybindings.keyI)
            );
            this.abilityButtons.add(callLightning.getButton(Keybindings.keyL));
        }
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    // return the lightning rod is built based on existing placed blocks
    // returns null if it is not build or is damaged
    // also will return null if outside of render range, but shouldn't matter since it'd be out of ability range anyway
    public BlockPos getLightningRodPos() {
        for (BuildingBlock block : blocks) {
            if (this.getLevel().getBlockState(block.getBlockPos()).getBlock() == Blocks.LIGHTNING_ROD &&
                this.getLevel().getBlockState(block.getBlockPos().below()).getBlock() == Blocks.WAXED_COPPER_BLOCK)
                return block.getBlockPos();
        }
        return null;
    }

    // check that the lightning rod is built based on existing placed blocks
    @Override
    public boolean isUpgraded() {
        for (BuildingBlock block : blocks)
            if (block.getBlockState().getBlock() == Blocks.LIGHTNING_ROD)
                return true;
        return false;
    }

    public void changeStructure(String newStructureName) {
        ArrayList<BuildingBlock> newBlocks = BuildingBlockData.getBuildingBlocks(newStructureName, this.getLevel());
        this.blocks = getAbsoluteBlockData(newBlocks, this.getLevel(), originPos, rotation);
        super.refreshBlocks();
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
            Laboratory.buildingName,
            new ResourceLocation("minecraft", "textures/block/brewing_stand.png"),
            hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == Laboratory.class,
            () -> false,
            () -> BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                    ResearchClient.hasCheat("modifythephasevariance"),
            () -> BuildingClientEvents.setBuildingToPlace(Laboratory.class),
            null,
            List.of(
                FormattedCharSequence.forward(Laboratory.buildingName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("A sinister lab that can research new technologies", Style.EMPTY),
                FormattedCharSequence.forward("based on the other buildings that have been built.", Style.EMPTY)
            ),
            null
        );
    }
}
