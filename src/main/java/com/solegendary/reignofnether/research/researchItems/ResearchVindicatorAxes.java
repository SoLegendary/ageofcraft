package com.solegendary.reignofnether.research.researchItems;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.units.villagers.VindicatorUnit;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchVindicatorAxes extends ProductionItem {

    public final static String itemName = "Diamond Axes";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_VINDICATOR_AXES;

    public ResearchVindicatorAxes(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchVindicatorAxes.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchVindicatorAxes.itemName);
                for (LivingEntity unit : UnitServerEvents.getAllUnits())
                    if (unit instanceof VindicatorUnit vUnit && vUnit.getOwnerName().equals(building.ownerName))
                        vUnit.setupEquipmentAndUpgradesServer();
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchVindicatorAxes.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
                ResearchVindicatorAxes.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/diamond_axe.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                hotkey,
                () -> false,
                () -> ProductionItem.itemIsBeingProduced(ResearchVindicatorAxes.itemName, prodBuilding.ownerName) ||
                        ResearchClient.hasResearch(ResearchVindicatorAxes.itemName),
                () -> true,
                () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
                null,
                List.of(
                        FormattedCharSequence.forward(ResearchVindicatorAxes.itemName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedTime(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Upgrades the axes of all vindicators to diamond (+2 damage)", Style.EMPTY)
                )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchVindicatorAxes.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/diamond_axe.png"),
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
