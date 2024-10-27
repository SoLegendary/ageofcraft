package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchSpiderJockeys;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.goals.MountGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.*;
import com.solegendary.reignofnether.unit.units.piglins.HeadhunterUnit;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class MountSpider extends Ability {

    private final LivingEntity entity;

    public MountSpider(LivingEntity entity) {
        super(
            UnitAction.MOUNT_SPIDER,
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
            "Mount Spider",
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/spider.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.MOUNT_SPIDER,
            () -> entity.isPassenger() || !ResearchClient.hasResearch(ResearchSpiderJockeys.itemName),
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.MOUNT_SPIDER),
            () -> UnitClientEvents.sendUnitCommand(UnitAction.MOUNT_SPIDER),
            List.of(
                FormattedCharSequence.forward("Mount Spider (Right click to auto-find)", Style.EMPTY)
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
        if (targetEntity instanceof SpiderUnit || targetEntity instanceof PoisonSpiderUnit) {
            getMountGoal().setTarget(targetEntity);
        }
        else if (level.isClientSide())
            HudClientEvents.showTemporaryMessage("Invalid target!");
    }
}
