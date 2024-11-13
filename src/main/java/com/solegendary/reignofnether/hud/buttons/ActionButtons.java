package com.solegendary.reignofnether.hud.buttons;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import static com.solegendary.reignofnether.unit.UnitClientEvents.sendUnitCommand;
import java.util.function.Supplier;

import java.util.ArrayList;

import java.util.function.Consumer;

public class ActionButtons {

    private static final String MOD_ID = ReignOfNether.MOD_ID;
    private static final List<Button> BUTTONS = new ArrayList<>();
    private static final List<Consumer<Button>> buttonAddedListeners = new ArrayList<>();
    private static final List<Consumer<Button>> buttonRemovedListeners = new ArrayList<>();

    public interface ButtonFactory {
        Button createButton();
    }

    private static Button createButton(
            String name,
            String texturePath,
            Keybinding keybinding,
            Supplier<Boolean> isActive,
            Runnable onClick,
            String i18nKey
    ) {
        return new Button(
                name,
                Button.itemIconSize,
                texturePath != null ? new ResourceLocation(MOD_ID, texturePath) : null,
                keybinding,
                isActive,
                () -> false,
                () -> true,
                onClick,
                null,
                List.of(FormattedCharSequence.forward(I18n.get(i18nKey), Style.EMPTY))
        );
    }

    public static void addButton(ButtonFactory buttonFactory) {
        Button button = buttonFactory.createButton();
        BUTTONS.add(button);
        buttonAddedListeners.forEach(listener -> listener.accept(button));
    }

    public static void removeButton(Button button) {
        BUTTONS.remove(button);
        buttonRemovedListeners.forEach(listener -> listener.accept(button));
    }

    public static List<Button> getButtons() {
        return BUTTONS;
    }

    public static Button getButtonByName(String name) {
        return BUTTONS.stream()
                .filter(button -> button.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static void addButtonAddedListener(Consumer<Button> listener) {
        buttonAddedListeners.add(listener);
    }

    public static void addButtonRemovedListener(Consumer<Button> listener) {
        buttonRemovedListeners.add(listener);
    }

    // Predefined buttons registered at initialization
    static {
        addButton(() -> createButton(
                "Build/Repair",
                "textures/icons/items/shovel.png",
                Keybindings.build,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.BUILD_REPAIR,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.BUILD_REPAIR),
                "hud.actionbuttons.reignofnether.build_repair"
        ));

        addButton(() -> createButton(
                "Gather",
                null, // Dynamic icon based on gather target
                Keybindings.gather,
                () -> UnitClientEvents.getSelectedUnitResourceTarget() != ResourceName.NONE,
                () -> sendUnitCommand(UnitAction.TOGGLE_GATHER_TARGET),
                "hud.actionbuttons.reignofnether.gather"
        ));

        addButton(() -> createButton(
                "Attack",
                "textures/icons/items/sword.png",
                Keybindings.attack,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.ATTACK),
                "hud.actionbuttons.reignofnether.attack"
        ));

        addButton(() -> createButton(
                "Stop",
                "textures/icons/items/barrier.png",
                Keybindings.stop,
                () -> false,
                () -> sendUnitCommand(UnitAction.STOP),
                "hud.actionbuttons.reignofnether.stop"
        ));

        addButton(() -> createButton(
                "Hold Position",
                "textures/icons/items/chestplate.png",
                Keybindings.hold,
                () -> {
                    LivingEntity entity = HudClientEvents.hudSelectedEntity;
                    return entity instanceof Unit unit && unit.getHoldPosition();
                },
                () -> sendUnitCommand(UnitAction.HOLD),
                "hud.actionbuttons.reignofnether.hold_position"
        ));

        addButton(() -> createButton(
                "Move",
                "textures/icons/items/boots.png",
                Keybindings.move,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.MOVE,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.MOVE),
                "hud.actionbuttons.reignofnether.move"
        ));

        addButton(() -> createButton(
                "Garrison",
                "textures/block/ladder.png",
                Keybindings.garrison,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.GARRISON,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.GARRISON),
                "hud.actionbuttons.reignofnether.garrison"
        ));

        addButton(() -> createButton(
                "Ungarrison",
                "textures/block/oak_trapdoor.png",
                Keybindings.garrison,
                () -> false,
                () -> sendUnitCommand(UnitAction.UNGARRISON),
                "hud.actionbuttons.reignofnether.ungarrison"
        ));
    }
}
