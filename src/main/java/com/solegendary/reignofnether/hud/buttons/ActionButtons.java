package com.solegendary.reignofnether.hud.buttons;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

import static com.solegendary.reignofnether.unit.UnitClientEvents.sendUnitCommand;

// static list of generic unit actions (build, attack, move, stop, etc.)
public class ActionButtons {

    public static final Button BUILD_REPAIR = new Button(
            "Build/Repair",
            Button.itemIconSize,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/shovel.png"),
            Keybindings.build,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.BUILD_REPAIR,
            () -> false,
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.BUILD_REPAIR),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.build_repair").getString(), Style.EMPTY))
    );
    public static final Button GATHER = new Button(
            "Gather",
            Button.itemIconSize,
            null, // changes depending on the gather target
            Keybindings.gather,
            () -> UnitClientEvents.getSelectedUnitResourceTarget() != ResourceName.NONE,
            () -> false,
            () -> true,
            () -> sendUnitCommand(UnitAction.TOGGLE_GATHER_TARGET),
            null,
            null
    );
    public static final Button ATTACK = new Button(
            "Attack",
            Button.itemIconSize,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/sword.png"),
            Keybindings.attack,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK,
            () -> false,
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.ATTACK),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.attack").getString(), Style.EMPTY))
    );
    public static final Button STOP = new Button(
            "Stop",
            Button.itemIconSize,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/barrier.png"),
            Keybindings.stop,
            () -> false, // except if currently clicked on
            () -> false,
            () -> true,
            () -> sendUnitCommand(UnitAction.STOP),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.stop").getString(), Style.EMPTY))
    );
    public static final Button HOLD = new Button(
            "Hold Position",
            Button.itemIconSize,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/chestplate.png"),
            Keybindings.hold,
            () -> {
                LivingEntity entity = HudClientEvents.hudSelectedEntity;
                return entity instanceof Unit unit && unit.getHoldPosition();
            },
            () -> false,
            () -> true,
            () -> sendUnitCommand(UnitAction.HOLD),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.hold").getString(), Style.EMPTY))
    );
    public static final Button MOVE = new Button(
            "Move",
            Button.itemIconSize,
            new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/items/boots.png"),
            Keybindings.move,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.MOVE,
            () -> false,
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.MOVE),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.move").getString(), Style.EMPTY))
    );
    public static final Button GARRISON = new Button(
            "Garrison",
            Button.itemIconSize,
            new ResourceLocation("minecraft", "textures/block/ladder.png"),
            Keybindings.garrison,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.GARRISON,
            () -> false,
            () -> true,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.GARRISON),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.garrison").getString(), Style.EMPTY))
    );
    public static final Button UNGARRISON = new Button(
            "Ungarrison",
            Button.itemIconSize,
            new ResourceLocation("minecraft", "textures/block/oak_trapdoor.png"),
            Keybindings.garrison,
            () -> false,
            () -> false,
            () -> true,
            () -> sendUnitCommand(UnitAction.UNGARRISON),
            null,
            List.of(FormattedCharSequence.forward(Component.translatable("button.reignofnether.ungarrison").getString(), Style.EMPTY))
    );
}
