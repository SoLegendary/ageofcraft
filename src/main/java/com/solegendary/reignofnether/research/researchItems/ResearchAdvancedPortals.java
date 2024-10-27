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

public class ResearchAdvancedPortals extends ProductionItem {

    public final static String itemName = "Advanced Portals";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_ADVANCED_PORTALS;

    public ResearchAdvancedPortals(ProductionBuilding building) {
        super(building, ResourceCosts.RESEARCH_ADVANCED_PORTALS.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchAdvancedPortals.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchAdvancedPortals.itemName);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchAdvancedPortals.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
                ResearchAdvancedPortals.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/portal.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                hotkey,
                () -> false,
                () -> ProductionItem.itemIsBeingProduced(ResearchAdvancedPortals.itemName, prodBuilding.ownerName) ||
                        ResearchClient.hasResearch(ResearchAdvancedPortals.itemName),
                () -> true,
                () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
                null,
                List.of(
                        FormattedCharSequence.forward(ResearchAdvancedPortals.itemName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedTime(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Allows portals to be constructed on overworld terrain at full ", Style.EMPTY),
                        FormattedCharSequence.forward("speed and enables them to be upgraded into transport portals, ", Style.EMPTY),
                        FormattedCharSequence.forward("which can instantly teleport units between them.", Style.EMPTY)
                )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchAdvancedPortals.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/portal.png"),
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
