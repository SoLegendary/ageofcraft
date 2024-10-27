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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchBlazeFirewall extends ProductionItem {

    public final static String itemName = "Walls of Fire";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_BLAZE_FIRE_WALL;

    public ResearchBlazeFirewall(ProductionBuilding building) {
        super(building, ResourceCosts.RESEARCH_BLAZE_FIRE_WALL.ticks);
        this.onComplete = (Level level) -> {
            if (level.isClientSide())
                ResearchClient.addResearch(this.building.ownerName, ResearchBlazeFirewall.itemName);
            else {
                ResearchServerEvents.addResearch(this.building.ownerName, ResearchBlazeFirewall.itemName);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchBlazeFirewall.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
                ResearchBlazeFirewall.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/fire.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                hotkey,
                () -> false,
                () -> ProductionItem.itemIsBeingProduced(ResearchBlazeFirewall.itemName, prodBuilding.ownerName) ||
                        ResearchClient.hasResearch(ResearchBlazeFirewall.itemName),
                () -> true,
                () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
                null,
                List.of(
                        FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_blaze_firewall.name").getString(), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedTime(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_blaze_firewall.description1").getString(), Style.EMPTY),
                        FormattedCharSequence.forward(Component.translatable("researchitems.reignofnether.research_blaze_firewall.description2").getString(), Style.EMPTY)
                )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchBlazeFirewall.itemName,
                14,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/fire.png"),
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
