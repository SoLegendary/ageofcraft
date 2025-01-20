package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.neutral.EndermanUnit;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class Teleport extends Ability {

    public static final int CD_MAX_SECONDS = 8;
    public static final int RANGE = 10;

    private final EndermanUnit unit;

    public Teleport(EndermanUnit unit) {
        super(UnitAction.TELEPORT, unit.level, CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND, RANGE, 0, false);
        this.unit = unit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton("Teleport",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/enderpearl.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.TELEPORT,
            () -> false,
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.TELEPORT),
            null,
            List.of(
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.teleport"), Style.EMPTY.withBold(true)),
                FormattedCharSequence.forward(
                    I18n.get("abilities.reignofnether.teleport.tooltip1", CD_MAX_SECONDS) + RANGE,
                    MyRenderer.iconStyle
                ),
                FormattedCharSequence.forward(I18n.get("abilities.reignofnether.teleport.tooltip2"), Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        BlockPos limitedBp = MyMath.getXZRangeLimitedBlockPos(((LivingEntity) unitUsing).getOnPos(), targetBp, range);
        ((EndermanUnit) unitUsing).teleport(limitedBp);

        this.setToMaxCooldown();
    }
}
