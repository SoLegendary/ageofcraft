package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchLingeringPotions;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.villagers.WitchUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.List;

public class ThrowLingeringHarmingPotion extends Ability {

    public static final int CD_MAX_SECONDS = 10;

    private final WitchUnit witchUnit;

    public ThrowLingeringHarmingPotion(WitchUnit witchUnit) {
        super(
            UnitAction.THROW_LINGERING_HARMING_POTION,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            witchUnit.getPotionThrowRange(),
            0,
            true
        );
        this.witchUnit = witchUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Lingering Harming Potion",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/lingering_potion_harming.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.THROW_LINGERING_HARMING_POTION,
            () -> false, //!ResearchClient.hasResearch(ResearchLingeringPotions.itemName),
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.THROW_LINGERING_HARMING_POTION),
            null,
            List.of(
                FormattedCharSequence.forward("Lingering Harming Potion", Style.EMPTY.withBold(true)),
                FormattedCharSequence.forward("\uE006  3  " + "\uE004  " + CD_MAX_SECONDS + "s  \uE005  " + witchUnit.getPotionThrowRange(), MyRenderer.iconStyle),
                FormattedCharSequence.forward("Throw a potion that leaves a cloud of deadly gas.", Style.EMPTY)
            ),
            this
        );
    }

    // lingering vs splash is set in WitchUnit.throwPotion
    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        ((WitchUnit) unitUsing).getThrowPotionGoal().setPotion(Potions.STRONG_HARMING);
        ((WitchUnit) unitUsing).getThrowPotionGoal().setAbility(this);
        ((WitchUnit) unitUsing).getThrowPotionGoal().setTarget(targetBp);
    }

    @Override
    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) {
        ((WitchUnit) unitUsing).getThrowPotionGoal().setPotion(Potions.STRONG_HARMING);
        ((WitchUnit) unitUsing).getThrowPotionGoal().setAbility(this);
        ((WitchUnit) unitUsing).getThrowPotionGoal().setTarget(targetEntity);
    }
}
