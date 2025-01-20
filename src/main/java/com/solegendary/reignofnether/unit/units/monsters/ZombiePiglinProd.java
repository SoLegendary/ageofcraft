package com.solegendary.reignofnether.unit.units.monsters;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.sandbox.SandboxAction;
import com.solegendary.reignofnether.sandbox.SandboxClientEvents;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ZombiePiglinProd extends ProductionItem {

    public final static String itemName = "Zombie Piglin";
    public final static ResourceCost cost = ResourceCosts.ZOMBIE_PIGLIN;

    public ZombiePiglinProd(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (!level.isClientSide()) {
                building.produceUnit((ServerLevel) level, EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get(), building.ownerName, true);
            }
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popCost = cost.population;
    }

    public String getItemName() {
        return ZombiePiglinProd.itemName;
    }

    public static AbilityButton getPlaceButton() {
        return new AbilityButton(
                itemName,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/zombie_piglin.png"),
                null,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    CursorClientEvents.setLeftClickSandboxAction(SandboxAction.SPAWN_UNIT);
                    SandboxClientEvents.spawnUnitName = itemName;
                },
                null,
                List.of(
                        FormattedCharSequence.forward(I18n.get("units.monsters.reignofnether.zombie_piglin"), Style.EMPTY.withBold(true))
                ),
                null
        );
    }
}
