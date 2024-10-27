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

public class ResearchSilverfish extends ProductionItem {

    public final static String itemName = "Infested Defences";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_SILVERFISH;

    public final static float SILVERFISH_SPAWN_CHANCE = 0.1f;

    public ResearchSilverfish(ProductionBuilding building) {
        super(building, ResourceCosts.RESEARCH_SILVERFISH.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchSilverfish.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchSilverfish.itemName);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchSilverfish.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            ResearchSilverfish.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/silverfish.png"),
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
            hotkey,
            () -> false,
            () -> ProductionItem.itemIsBeingProduced(ResearchSilverfish.itemName, prodBuilding.ownerName) ||
                    ResearchClient.hasResearch(ResearchSilverfish.itemName),
            () -> BuildingClientEvents.hasFinishedBuilding(Stronghold.buildingName),
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                FormattedCharSequence.forward(ResearchSilverfish.itemName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedTime(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Gives your buildings a " + (int) (SILVERFISH_SPAWN_CHANCE * 100) + "% chance to spawn", Style.EMPTY),
                FormattedCharSequence.forward("a silverfish whenever a block is destroyed. ", Style.EMPTY),
                FormattedCharSequence.forward("Silverfish have limited lifespans.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Requires a Stronghold.", Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchSilverfish.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/silverfish.png"),
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
