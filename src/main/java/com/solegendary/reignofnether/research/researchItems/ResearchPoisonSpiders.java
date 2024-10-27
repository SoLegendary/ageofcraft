package com.solegendary.reignofnether.research.researchItems;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
import com.solegendary.reignofnether.building.buildings.monsters.SpiderLair;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.units.monsters.SpiderUnit;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchPoisonSpiders extends ProductionItem {

    public final static String itemName = "Cave Spiders";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_POISON_SPIDERS;

    public ResearchPoisonSpiders(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchPoisonSpiders.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchPoisonSpiders.itemName);

                // convert all spiders into poison spiders with the same stats/inventory/etc.
                UnitServerEvents.convertAllToUnit(
                    this.building.ownerName,
                    (ServerLevel) level,
                    (LivingEntity entity) ->
                        entity instanceof SpiderUnit zUnit &&
                        zUnit.getOwnerName().equals(building.ownerName),
                    EntityRegistrar.POISON_SPIDER_UNIT.get()
                );
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchPoisonSpiders.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            ResearchPoisonSpiders.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/cave_spider.png"),
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
            hotkey,
            () -> false,
            () -> ProductionItem.itemIsBeingProduced(ResearchPoisonSpiders.itemName, prodBuilding.ownerName) ||
                    ResearchClient.hasResearch(ResearchPoisonSpiders.itemName),
            () -> BuildingClientEvents.hasFinishedBuilding(SpiderLair.buildingName),
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                FormattedCharSequence.forward(ResearchPoisonSpiders.itemName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedTime(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Transforms your Spiders into Cave Spiders, ", Style.EMPTY),
                FormattedCharSequence.forward("granting them poisonous attacks.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Requires a Spider Lair.", Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
            ResearchPoisonSpiders.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/cave_spider.png"),
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
