package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchBruteShields;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.unit.units.piglins.BruteUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class ToggleShield extends Ability {

    private static final int CD_MAX_SECONDS = 0;

    private final BruteUnit bruteUnit;

    public ToggleShield(BruteUnit bruteUnit) {
        super(
                UnitAction.TOGGLE_SHIELD,
                CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
                0,
                0,
                false
        );
        this.bruteUnit = bruteUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Shield Stance",
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/shield.png"),
                hotkey,
                () -> bruteUnit.isHoldingUpShield,
                () -> !ResearchClient.hasResearch(ResearchBruteShields.itemName) ||
                        bruteUnit.getItemBySlot(EquipmentSlot.OFFHAND).getItem() != Items.SHIELD,
                () -> true,
                () -> UnitClientEvents.sendUnitCommand(UnitAction.TOGGLE_SHIELD),
                null,
                List.of(
                        FormattedCharSequence.forward("Shield Stance", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Raise or lower a shield - reducing projectile ", Style.EMPTY),
                        FormattedCharSequence.forward("damage taken by 67% and movement speed by 50%.", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        bruteUnit.isHoldingUpShield = !bruteUnit.isHoldingUpShield;
        if (!level.isClientSide()) {
            UnitSyncClientboundPacket.sendSyncAnimationPacket(this.bruteUnit, bruteUnit.isHoldingUpShield);
            BlockPos bp = unitUsing.getMoveGoal().getMoveTarget();
            unitUsing.getMoveGoal().stopMoving();
            unitUsing.getMoveGoal().setMoveTarget(bp);
        }
    }

    @Override
    public boolean shouldResetBehaviours() { return false; }
}
