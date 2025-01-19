package com.solegendary.reignofnether.unit.units.monsters;

import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.sandbox.SandboxAction;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchDrowned;
import com.solegendary.reignofnether.research.researchItems.ResearchHusks;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ZombieProd extends ProductionItem {

    public final static String itemName = "Zombie";
    public final static ResourceCost cost = ResourceCosts.ZOMBIE;

    public ZombieProd(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (!level.isClientSide()) {
                if (ResearchServerEvents.playerHasResearch(this.building.ownerName, ResearchHusks.itemName))
                    building.produceUnit((ServerLevel) level, EntityRegistrar.HUSK_UNIT.get(), building.ownerName, true);
                else if (ResearchServerEvents.playerHasResearch(this.building.ownerName, ResearchDrowned.itemName))
                    building.produceUnit((ServerLevel) level, EntityRegistrar.DROWNED_UNIT.get(), building.ownerName, true);
                else
                    building.produceUnit((ServerLevel) level, EntityRegistrar.ZOMBIE_UNIT.get(), building.ownerName, true);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popCost = cost.population;
    }

    public String getItemName() {
        return ZombieProd.itemName;
    }

    private static ResourceLocation getIcon() {
        if (ResearchClient.hasResearch(ResearchHusks.itemName))
            return new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/husk.png");
        else if (ResearchClient.hasResearch(ResearchDrowned.itemName))
            return new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/drowned.png");
        else
            return new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/zombie.png");
    }

    private static String getCancelName() {
        if (ResearchClient.hasResearch(ResearchHusks.itemName))
            return "Husk";
        else if (ResearchClient.hasResearch(ResearchDrowned.itemName))
            return "Drowned";
        else
            return "Zombie";
    }

    public static Button getPlaceButton() {
        return new Button(
                ZombieProd.itemName,
                14,
                getIcon(),
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickSandboxAction(SandboxAction.SPAWN_ZOMBIE),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie"), Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie.tooltip2"), Style.EMPTY)
                )
        );
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
                ZombieProd.itemName,
                14,
                getIcon(),
                hotkey,
                () -> false,
                () -> ResearchClient.hasResearch(ResearchHusks.itemName) || ResearchClient.hasResearch(ResearchDrowned.itemName),
                () -> true,
                () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie"), Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedPopAndTime(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie.tooltip1"), Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie.tooltip2"), Style.EMPTY)
                )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
            getCancelName(),
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
