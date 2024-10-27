package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class GotoPortal extends Ability {

    private static final int CD_MAX = 0;
    private static final int RANGE = 0;

    Building building;

    public GotoPortal(Building building) {
        super(
            UnitAction.GOTO_PORTAL,
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
                "Go to connected portal",
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/map.png"),
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
                () -> UnitClientEvents.sendUnitCommand(UnitAction.GOTO_PORTAL),
                null,
                List.of(
                        FormattedCharSequence.forward("Go to connected portal", Style.EMPTY.withBold(true))
                ),
                this
        );
    }

    @Override
    public void use(Level level, Building buildingUsing, BlockPos targetBp) {
        if (level.isClientSide() && building instanceof Portal portal &&
            portal.portalType == Portal.PortalType.TRANSPORT &&
            portal.destination != null) {
            Building targetBuilding = BuildingUtils.findBuilding(level.isClientSide(), portal.destination);
            if (targetBuilding instanceof Portal targetPortal && portal.portalType == Portal.PortalType.TRANSPORT)
                OrthoviewClientEvents.centreCameraOnPos(targetPortal.centrePos.getX(), targetPortal.centrePos.getZ());
        }
    }
}
