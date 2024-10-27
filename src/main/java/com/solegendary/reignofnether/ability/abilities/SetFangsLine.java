package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class SetFangsLine extends Ability {

    public static final int CD_MAX_SECONDS = 8;

    private final EvokerUnit evokerUnit;

    public SetFangsLine(EvokerUnit evokerUnit) {
        super(
            UnitAction.SET_FANGS_LINE,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            EvokerUnit.FANGS_RANGE_LINE,
            0,
            true
        );
        this.evokerUnit = evokerUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Evoker Fangs (Line)",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/shears.png"),
            hotkey,
            () -> evokerUnit.isUsingLineFangs,
            () -> false,
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.SET_FANGS_LINE),
            null,
            List.of(
                FormattedCharSequence.forward("Evoker Fangs (Line)", Style.EMPTY.withBold(true)),
                FormattedCharSequence.forward("\uE006  " + EvokerUnit.FANGS_DAMAGE * 2 + "  " + "\uE004  " + CD_MAX_SECONDS + "s  \uE005  " + EvokerUnit.FANGS_RANGE_LINE, MyRenderer.iconStyle),
                FormattedCharSequence.forward("Have this evoker summon a long line of snapping", Style.EMPTY),
                FormattedCharSequence.forward("fangs around the caster when attacking.", Style.EMPTY)
            ),
            this
        );
    }

    public void setCooldownSingle(int cooldown) {
        super.setCooldown(cooldown);
    }

    @Override
    public void setCooldown(int cooldown) {
        super.setCooldown(cooldown);
        for (Ability ability : this.evokerUnit.getAbilities())
            if (ability instanceof SetFangsCircle ab)
                ab.setCooldownSingle(cooldown);
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        evokerUnit.isUsingLineFangs = true;
    }

    @Override
    public boolean canBypassCooldown() { return true; }

    @Override
    public boolean shouldResetBehaviours() { return false; }
}
