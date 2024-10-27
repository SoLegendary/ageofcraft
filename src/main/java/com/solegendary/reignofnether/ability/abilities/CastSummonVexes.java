package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchEvokerVexes;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class CastSummonVexes extends Ability {

    public static final int CD_MAX_SECONDS = 80;
    public static final int VEX_DURATION_SECONDS = 30;

    private final EvokerUnit evokerUnit;

    public CastSummonVexes(EvokerUnit evokerUnit) {
        super(
            UnitAction.CAST_SUMMON_VEXES,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            0,
            0,
            true
        );
        this.evokerUnit = evokerUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Summon Vexes",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/vex.png"),
            hotkey,
            () -> {
                if (this.evokerUnit.getCastSummonVexesGoal() != null)
                    return this.evokerUnit.getCastSummonVexesGoal().isCasting();
                return false;
            },
            () -> !ResearchClient.hasResearch(ResearchEvokerVexes.itemName),
            () -> true,
            () -> UnitClientEvents.sendUnitCommand(UnitAction.CAST_SUMMON_VEXES),
            null,
            List.of(
                FormattedCharSequence.forward("Summon Vexes", Style.EMPTY.withBold(true)),
                FormattedCharSequence.forward("\uE004  " + CD_MAX_SECONDS + "s", MyRenderer.iconStyle),
                FormattedCharSequence.forward("After a long delay, summon " + EvokerUnit.SUMMON_VEXES_AMOUNT + " flying vexes that attack", Style.EMPTY),
                FormattedCharSequence.forward("the evoker's target. Vexes start to die off after " + VEX_DURATION_SECONDS + " seconds.", Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        ((EvokerUnit) unitUsing).getCastSummonVexesGoal().setAbility(this);
        ((EvokerUnit) unitUsing).getCastSummonVexesGoal().startCasting();
    }

    @Override
    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) {
        ((EvokerUnit) unitUsing).getCastSummonVexesGoal().setAbility(this);
        ((EvokerUnit) unitUsing).getCastSummonVexesGoal().startCasting();
    }
}
