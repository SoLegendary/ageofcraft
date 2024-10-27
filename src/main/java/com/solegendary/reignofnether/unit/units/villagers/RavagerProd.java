package com.solegendary.reignofnether.unit.units.villagers;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class RavagerProd extends ProductionItem {

    public final static String itemName = "Ravager";
    public final static ResourceCost cost = ResourceCosts.RAVAGER;

    public RavagerProd(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (!level.isClientSide()) {
                building.produceUnit((ServerLevel) level, EntityRegistrar.RAVAGER_UNIT.get(), building.ownerName, true);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popCost = cost.population;
    }

    public String getItemName() {
        return RavagerProd.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
            RavagerProd.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/ravager.png"),
            hotkey,
            () -> false,
            () -> false,
            () -> true,
            () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
            null,
            List.of(
                    FormattedCharSequence.forward(Component.translatable("hud.reignofnether.ravager_prod.name").getString(), Style.EMPTY.withBold(true)),
                    ResourceCosts.getFormattedCost(cost),
                    ResourceCosts.getFormattedPopAndTime(cost),
                    FormattedCharSequence.forward("", Style.EMPTY),
                    FormattedCharSequence.forward(Component.translatable("hud.reignofnether.ravager_prod.description1").getString(), Style.EMPTY),
                    FormattedCharSequence.forward(Component.translatable("hud.reignofnether.ravager_prod.description2").getString(), Style.EMPTY)
            )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
            RavagerProd.itemName,
            14,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/ravager.png"),
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
