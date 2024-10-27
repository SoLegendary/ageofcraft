package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.buildings.monsters.Laboratory;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

import java.util.List;

public class CallLightning extends Ability {

    private static final int CD_MAX = 60 * ResourceCost.TICKS_PER_SECOND;
    private static final int RANGE = 25;

    private final Laboratory lab;

    public CallLightning(Laboratory lab) {
        super(
            UnitAction.CALL_LIGHTNING,
            CD_MAX,
            RANGE,
            0,
            false
        );
        this.lab = lab;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Call Lightning",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/lightbulb_on.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.CALL_LIGHTNING,
            () -> !lab.isUpgraded(),
            () -> lab.getLightningRodPos() != null,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.CALL_LIGHTNING),
            null,
            List.of(
                    FormattedCharSequence.forward("Call Lightning", Style.EMPTY.withBold(true)),
                    FormattedCharSequence.forward("\uE004  " + CD_MAX/20 + "s  \uE005  " + RANGE, MyRenderer.iconStyle),
                    FormattedCharSequence.forward("", Style.EMPTY),
                    FormattedCharSequence.forward("Summon a bolt of lightning at the target location.", Style.EMPTY),
                    FormattedCharSequence.forward("Can be used to charge creepers and damage enemies.", Style.EMPTY)
            ),
            this
        );
    }

    @Override
    public void use(Level level, Building buildingUsing, BlockPos targetBp) {

        if (!level.isClientSide() && buildingUsing instanceof Laboratory lab) {
            BlockPos rodPos = lab.getLightningRodPos();

            if (lab.isAbilityOffCooldown(UnitAction.CALL_LIGHTNING) && rodPos != null) {
                BlockPos limitedBp = MyMath.getXZRangeLimitedBlockPos(rodPos, targetBp, range);
                // getXZRangeLimitedBlockPos' Y value is always the same as rodPos, but we want the first sky-exposed block
                limitedBp = MiscUtil.getHighestNonAirBlock(level, limitedBp);

                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(limitedBp.getX(), limitedBp.getY(), limitedBp.getZ());
                    level.addFreshEntity(bolt);
                }
                LightningBolt bolt2 = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt2 != null) {
                    bolt2.moveTo(rodPos.getX(), rodPos.getY(), rodPos.getZ());
                    level.addFreshEntity(bolt2);
                }
            }
        }
        this.setToMaxCooldown();
    }
}
