package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.CreeperUnit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class Roar extends Ability {

    private static final int CD_MAX_SECONDS = 20;

    private final RavagerUnit ravagerUnit;

    public Roar(RavagerUnit ravagerUnit) {
        super(
            UnitAction.ROAR,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            0,
            0,
            false
        );
        this.ravagerUnit = ravagerUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Roar",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/ravager.png"),
            hotkey,
            () -> false,
            () -> false,
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.ROAR),
            null,
            List.of(
                FormattedCharSequence.forward("Roar", Style.EMPTY),
                FormattedCharSequence.forward("\uE006  " + RavagerUnit.ROAR_DAMAGE + "   " + "\uE004  " + CD_MAX_SECONDS + "s  \uE005   " + RavagerUnit.ROAR_RANGE, MyRenderer.iconStyle),
                FormattedCharSequence.forward("", Style.EMPTY),
                FormattedCharSequence.forward("Emit a deafening roar, knocking away, damaging", Style.EMPTY),
                FormattedCharSequence.forward("and briefly slowing down all nearby enemies. ", Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        ravagerUnit.resetBehaviours();
        ravagerUnit.startToRoar();
        this.setToMaxCooldown();
    }
}
