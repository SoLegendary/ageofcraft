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
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchResourceCapacity extends ProductionItem {

    public final static String itemName = "Worker Carry Bags";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_RESOURCE_CAPACITY;

    public ResearchResourceCapacity(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide()) {
                ResearchClient.addResearch(this.building.ownerName, ResearchResourceCapacity.itemName);
                for (LivingEntity unit : UnitClientEvents.getAllUnits())
                    if (unit instanceof WorkerUnit)
                        ((Unit) unit).setupEquipmentAndUpgradesClient();
            }
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchResourceCapacity.itemName);
                for (LivingEntity unit : UnitServerEvents.getAllUnits())
                    if (unit instanceof WorkerUnit)
                        ((Unit) unit).setupEquipmentAndUpgradesServer();
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchResourceCapacity.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
                ResearchResourceCapacity.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/chest.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                hotkey,
                () -> false,
                () -> ProductionItem.itemIsBeingProduced(ResearchResourceCapacity.itemName, prodBuilding.ownerName) ||
                        ResearchClient.hasResearch(ResearchResourceCapacity.itemName),
                () -> true,
                () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
                null,
                List.of(
                        FormattedCharSequence.forward(ResearchResourceCapacity.itemName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedTime(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Raises the resource capacity of workers from 100 to 200", Style.EMPTY),
                        FormattedCharSequence.forward("Workers still auto-return resources when holding above 50", Style.EMPTY)
                )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchResourceCapacity.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/chest.png"),
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
