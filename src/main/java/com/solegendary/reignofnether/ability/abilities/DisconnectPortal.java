package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class DisconnectPortal extends Ability {

    private static final int CD_MAX = 0;
    private static final int RANGE = 0;

    Building building;

    public DisconnectPortal(Building building) {
        super(
            UnitAction.DISCONNECT_PORTAL,
            CD_MAX,
            RANGE,
            0,
            true
        );
        this.building = building;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Sever Connection",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/barrier.png"),
            hotkey,
            () -> false,
            () -> {
                // hidden if the portal does not have a connection Or isn't a transport portal
                if (building instanceof Portal portal) {
                    if (portal.portalType != Portal.PortalType.TRANSPORT)
                        return true;
                    return portal.destination == null;
                }
                return true;
            },
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.DISCONNECT_PORTAL),
            null,
            List.of(
                    FormattedCharSequence.forward("Sever Connection", Style.EMPTY.withBold(true))
            ),
            this
        );
    }

    @Override
    public void use(Level level, Building buildingUsing, BlockPos targetBp) {
        if (building instanceof Portal portal)
            portal.disconnectPortal();
    }
}
