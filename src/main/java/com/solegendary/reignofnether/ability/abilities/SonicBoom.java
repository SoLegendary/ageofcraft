package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.WardenUnit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.solegendary.reignofnether.hud.HudClientEvents.showTemporaryMessage;

public class SonicBoom extends Ability {

    public static final int CD_MAX_SECONDS = 60;

    private final WardenUnit wardenUnit;

    public SonicBoom(WardenUnit wardenUnit) {
        super(
                UnitAction.CAST_SONIC_BOOM,
                CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
                WardenUnit.SONIC_BOOM_RANGE,
                0,
                true
        );
        this.wardenUnit = wardenUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Sonic Boom",
                new ResourceLocation("minecraft", "textures/block/note_block.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.CAST_SONIC_BOOM,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.CAST_SONIC_BOOM),
                null,
                List.of(
                        FormattedCharSequence.forward("Sonic Boom", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE006  " + WardenUnit.SONIC_BOOM_DAMAGE + "  " + "\uE004  " + CD_MAX_SECONDS + "s  \uE005  " + WardenUnit.SONIC_BOOM_RANGE, MyRenderer.iconStyle),
                        FormattedCharSequence.forward("After a short delay, fire a targeted wave of sound at the", Style.EMPTY),
                        FormattedCharSequence.forward("target, dealing heavy damage and knocking it far away.", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) {
        if (targetEntity instanceof Unit unit && unit.equals(this.wardenUnit))
            return;
        ((WardenUnit) unitUsing).getSonicBoomGoal().setAbility(this);
        ((WardenUnit) unitUsing).getSonicBoomGoal().setTarget(targetEntity);
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        Building targetBuilding = BuildingUtils.findBuilding(level.isClientSide(), targetBp);
        if (targetBuilding != null) {
            ((WardenUnit) unitUsing).getSonicBoomGoal().setAbility(this);
            ((WardenUnit) unitUsing).getSonicBoomGoal().setTarget(targetBuilding);
        } else if (level.isClientSide()) {
            HudClientEvents.showTemporaryMessage("Must target a unit or building.");
        }
    }
}
