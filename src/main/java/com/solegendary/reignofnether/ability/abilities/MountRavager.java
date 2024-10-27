package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchRavagerCavalry;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.MountGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.SkeletonUnit;
import com.solegendary.reignofnether.unit.units.monsters.StrayUnit;
import com.solegendary.reignofnether.unit.units.piglins.HeadhunterUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class MountRavager extends Ability {

    private final LivingEntity entity;

    public MountRavager(LivingEntity entity) {
        super(
            UnitAction.MOUNT_RAVAGER,
            0,
            0,
            0,
            true
        );
        this.entity = entity;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
            "Mount Ravager",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/ravager.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.MOUNT_RAVAGER,
            () -> entity.isPassenger() || !ResearchClient.hasResearch(ResearchRavagerCavalry.itemName),
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.MOUNT_RAVAGER),
            () -> UnitClientEvents.sendUnitCommand(UnitAction.MOUNT_RAVAGER),
            List.of(
                    FormattedCharSequence.forward("Mount Ravager (Right click to auto-find)", Style.EMPTY),
                    FormattedCharSequence.forward("Mounted pillagers gain explosive arrows", Style.EMPTY)
            ),
            this
        );
    }

    private MountGoal getMountGoal() {
        if (entity instanceof PillagerUnit pillagerUnit)
            return pillagerUnit.getMountGoal();
        if (entity instanceof StrayUnit strayUnit)
            return strayUnit.getMountGoal();
        if (entity instanceof SkeletonUnit skeletonUnit)
            return skeletonUnit.getMountGoal();
        if (entity instanceof HeadhunterUnit headhunterUnit)
            return headhunterUnit.getMountGoal();
        return null;
    }


    // right click
    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        getMountGoal().autofind = true;
    }

    @Override
    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) {
        if (targetEntity instanceof RavagerUnit) {
            getMountGoal().setTarget(targetEntity);
        }
        else if (level.isClientSide())
            HudClientEvents.showTemporaryMessage("Invalid target!");
    }
}
