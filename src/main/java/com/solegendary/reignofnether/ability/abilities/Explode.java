package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class Explode extends Ability {

    private final CreeperUnit creeperUnit;

    public Explode(CreeperUnit creeperUnit) {
        super(
            UnitAction.EXPLODE,
            0,
            0,
            0,
            false
        );
        this.creeperUnit = creeperUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Explode",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/tnt.png"),
            hotkey,
            () -> false,//CursorClientEvents.getLeftClickAction() == UnitAction.EXPLODE,
            () -> false,
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.EXPLODE),//CursorClientEvents.setLeftClickAction(UnitAction.EXPLODE),
            null,
            List.of(
                FormattedCharSequence.forward("Explode", Style.EMPTY)
            ),
            null
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        creeperUnit.startToExplode();
    }
}
