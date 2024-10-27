package com.solegendary.reignofnether.research.researchItems;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
import com.solegendary.reignofnether.building.buildings.monsters.Dungeon;
import com.solegendary.reignofnether.building.buildings.monsters.Laboratory;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchLabLightningRod extends ProductionItem {

    public final static String itemName = "Lightning Rod";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_LAB_LIGHTNING_ROD;

    public ResearchLabLightningRod(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (this.building instanceof Laboratory lab)
                lab.changeStructure(Laboratory.upgradedStructureName);
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchLabLightningRod.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            ResearchLabLightningRod.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/lightbulb_off.png"),
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
            hotkey,
            () -> false,
            () -> ProductionItem.itemIsBeingProduced(ResearchLabLightningRod.itemName, prodBuilding.ownerName) ||
                    (prodBuilding instanceof Laboratory lab && lab.isUpgraded()),
            () -> BuildingClientEvents.hasFinishedBuilding(Dungeon.buildingName),
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                    FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_lab_lightning_rod.name").getString(), Style.EMPTY.withBold(true)),
                    ResourceCosts.getFormattedCost(cost),
                    ResourceCosts.getFormattedTime(cost),
                    FormattedCharSequence.forward("", Style.EMPTY),
                    FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_lab_lightning_rod.description1").getString(), Style.EMPTY),
                    FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_lab_lightning_rod.description2").getString(), Style.EMPTY),
                    FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_lab_lightning_rod.requires_dungeon").getString(), Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
            ResearchLabLightningRod.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/lightbulb_off.png"),
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
