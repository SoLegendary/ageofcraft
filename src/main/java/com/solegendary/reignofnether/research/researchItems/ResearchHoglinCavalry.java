package com.solegendary.reignofnether.research.researchItems;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
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

public class ResearchHoglinCavalry extends ProductionItem {

    public final static String itemName = "Hoglin Cavalry";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_HOGLIN_CAVALRY;

    public ResearchHoglinCavalry(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchHoglinCavalry.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchHoglinCavalry.itemName);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchHoglinCavalry.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            ResearchHoglinCavalry.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/hoglin.png"),
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
            hotkey,
            () -> false,
            () -> ProductionItem.itemIsBeingProduced(ResearchHoglinCavalry.itemName, prodBuilding.ownerName) ||
                    ResearchClient.hasResearch(ResearchHoglinCavalry.itemName),
            () -> true,
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                FormattedCharSequence.forward(ResearchHoglinCavalry.itemName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedTime(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Unlocks the ability for headhunters to mount hoglins.", Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
            ResearchHoglinCavalry.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/hoglin.png"),
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
