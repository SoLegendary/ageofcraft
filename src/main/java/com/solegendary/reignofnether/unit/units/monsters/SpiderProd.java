package com.solegendary.reignofnether.unit.units.monsters;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchDrowned;
import com.solegendary.reignofnether.research.researchItems.ResearchHusks;
import com.solegendary.reignofnether.research.researchItems.ResearchPoisonSpiders;
import com.solegendary.reignofnether.research.researchItems.ResearchStrays;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpiderProd extends ProductionItem {

    public final static String itemName = "Spider";
    public final static ResourceCost cost = ResourceCosts.SPIDER;

    public SpiderProd(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (!level.isClientSide()) {
                if (ResearchServerEvents.playerHasResearch(this.building.ownerName, ResearchPoisonSpiders.itemName))
                    building.produceUnit((ServerLevel) level, EntityRegistrar.POISON_SPIDER_UNIT.get(), building.ownerName, true);
                else
                    building.produceUnit((ServerLevel) level, EntityRegistrar.SPIDER_UNIT.get(), building.ownerName, true);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popCost = cost.population;
    }

    private static ResourceLocation getIcon() {
        if (ResearchClient.hasResearch(PoisonSpiderProd.itemName))
            return new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/cave_spider.png");
        else
            return new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/spider.png");
    }

    private static String getCancelName() {
        if (ResearchClient.hasResearch(PoisonSpiderProd.itemName))
            return "Poison Spider";
        else
            return "Spider";
    }

    public String getItemName() {
        return SpiderProd.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            SpiderProd.itemName,
            14,
            getIcon(),
            hotkey,
            () -> false,
            () -> ResearchClient.hasResearch(ResearchPoisonSpiders.itemName),
            () -> true,
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                FormattedCharSequence.forward(SpiderProd.itemName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedPopAndTime(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("A giant spider that is nimble but frail.", Style.EMPTY),
                FormattedCharSequence.forward("Can be upgraded to allow skeletons to ride them.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Spiders move much more slowly under sunlight.", Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchClient.hasResearch(ResearchPoisonSpiders.itemName) ? "Cave Spider" : "Spider",
                14,
                getIcon(),
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> BuildingServerboundPacket.cancelProduction(prodBuilding.originPos, itemName, first),
                null,
                null
        );
    }
}