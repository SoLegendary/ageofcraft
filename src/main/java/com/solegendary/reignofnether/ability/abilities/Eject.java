package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class Eject extends Ability {

    private final LivingEntity entity;

    public Eject(LivingEntity entity) {
        super(
            UnitAction.EJECT,
            0,
            0,
            0,
            false
        );
        this.entity = entity;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Eject",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/barrier.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.EJECT,
            () -> entity.getPassengers().size() == 0,
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.EJECT),
            null,
            List.of(
                FormattedCharSequence.forward("Eject", Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        ((LivingEntity) unitUsing).ejectPassengers();
    }
}
