package com.solegendary.reignofnether.research.researchItems;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
import com.solegendary.reignofnether.building.buildings.monsters.Stronghold;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchSculkAmplifiers extends ProductionItem {

    public final static String itemName = "Sculk Amplifiers";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_SCULK_AMPLIFIERS;

    public final static int SPLIT_BOOM_RANGE = 20;

    public ResearchSculkAmplifiers(ProductionBuilding building) {
        super(building, ResourceCosts.RESEARCH_SCULK_AMPLIFIERS.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchSculkAmplifiers.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchSculkAmplifiers.itemName);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchSculkAmplifiers.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            ResearchSculkAmplifiers.itemName,
            14,
            new ResourceLocation("minecraft", "textures/block/sculk_shrieker_side.png"),
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
            hotkey,
            () -> false,
            () -> ProductionItem.itemIsBeingProduced(ResearchSculkAmplifiers.itemName, prodBuilding.ownerName) ||
                    ResearchClient.hasResearch(ResearchSculkAmplifiers.itemName),
            () -> BuildingClientEvents.hasFinishedBuilding(Stronghold.buildingName),
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                FormattedCharSequence.forward(ResearchSculkAmplifiers.itemName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedTime(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Wardens can Sonic Boom Sculk Calaysts to split the boom ", Style.EMPTY),
                FormattedCharSequence.forward("onto 3 random nearby enemies within a range of " + SPLIT_BOOM_RANGE, Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Requires a Stronghold.", Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchSculkAmplifiers.itemName,
                14,
                new ResourceLocation("minecraft", "textures/block/sculk_shrieker_side.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                null,
                () -> false,
                () -> false,
                () -> true,
                () -> BuildingServerboundPacket.cancelProduction(prodBuilding.minCorner, itemName, first),
                null,
                null
        );
    }
}
