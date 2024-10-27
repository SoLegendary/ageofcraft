package com.solegendary.reignofnether.unit.units.monsters;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchStrays;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkeletonProd extends ProductionItem {

    public final static String itemName = "Skeleton";
    public final static ResourceCost cost = ResourceCosts.SKELETON;

    public SkeletonProd(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (!level.isClientSide()) {
                if (ResearchServerEvents.playerHasResearch(this.building.ownerName, ResearchStrays.itemName))
                    building.produceUnit((ServerLevel) level, EntityRegistrar.STRAY_UNIT.get(), building.ownerName, true);
                else
                    building.produceUnit((ServerLevel) level, EntityRegistrar.SKELETON_UNIT.get(), building.ownerName, true);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popCost = cost.population;
    }

    public String getItemName() {
        return SkeletonProd.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            SkeletonProd.itemName,
            14,
            ResearchClient.hasResearch(ResearchStrays.itemName) ?
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/stray.png") :
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/skeleton.png"),
            hotkey,
            () -> false,
            () -> ResearchClient.hasResearch(ResearchStrays.itemName),
            () -> true,
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                FormattedCharSequence.forward(SkeletonProd.itemName, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedPopAndTime(cost),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("An undead soldier with a bow and arrows.", Style.EMPTY),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Skeletons will burn under sunlight.", Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
            ResearchClient.hasResearch(ResearchStrays.itemName) ? "Stray" : "Skeleton",
            14,
            ResearchClient.hasResearch(ResearchStrays.itemName) ?
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/stray.png") :
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/skeleton.png"),
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
