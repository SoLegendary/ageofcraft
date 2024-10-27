package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingBlockData;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.buildings.monsters.Mausoleum;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.monsters.SpruceBridge;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Minecraft;
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

public class OakBridge extends AbstractBridge {

    public final static String buildingName = "Oak Bridge";
    public final static String structureNameOrthogonal = "bridge_oak_orthogonal";
    public final static String structureNameDiagonal = "bridge_oak_diagonal";
    public final static ResourceCost cost = ResourceCosts.OAK_BRIDGE;

    public OakBridge(Level level, BlockPos originPos, Rotation rotation, String ownerName, boolean diagonal) {
        super(level, originPos, rotation, ownerName, diagonal,
                getCulledBlocks(getAbsoluteBlockData(getRelativeBlockData(level, diagonal), level, originPos, rotation), level));

        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.OAK_FENCE;
        this.icon = new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/oak_fence.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 1.0f;

        this.startingBlockTypes.add(Blocks.OAK_LOG);
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level, boolean diagonal) {
        return BuildingBlockData.getBuildingBlocks(diagonal ? structureNameDiagonal : structureNameOrthogonal, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        Minecraft MC = Minecraft.getInstance();
        return new AbilityButton(
                OakBridge.buildingName,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/oak_fence.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == OakBridge.class,
                () -> !TutorialClientEvents.isAtOrPastStage(TutorialStage.BUILD_BRIDGE),
                () -> TutorialClientEvents.isAtOrPastStage(TutorialStage.BUILD_BRIDGE) &&
                       (BuildingClientEvents.hasFinishedBuilding(TownCentre.buildingName) ||
                        BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance")),
                () -> BuildingClientEvents.setBuildingToPlace(OakBridge.class),
                null,
                List.of(
                        FormattedCharSequence.forward(SpruceBridge.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A bridge built to traverse water.", Style.EMPTY),
                        FormattedCharSequence.forward("Must be connected to land or another bridge.", Style.EMPTY),
                        FormattedCharSequence.forward("Can be built over lava (but isn't fireproof!)", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Bridges can be repaired or attacked by anyone.", Style.EMPTY)
                ),
                null
        );
    }
}
