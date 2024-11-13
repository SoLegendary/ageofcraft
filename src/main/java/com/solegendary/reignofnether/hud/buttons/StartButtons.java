package com.solegendary.reignofnether.hud.buttons;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.player.PlayerClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialStage;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import java.util.ArrayList;
import java.util.function.Consumer;

public class StartButtons {

    public static final int ICON_SIZE = 14;
    private static final List<Button> BUTTONS = new ArrayList<>();
    private static final List<Consumer<Button>> buttonAddedListeners = new ArrayList<>();
    private static final List<Consumer<Button>> buttonRemovedListeners = new ArrayList<>();

    static {
        // Register default start buttons
        addButton(StartButtons::createVillagerStartButton);
        addButton(StartButtons::createMonsterStartButton);
        addButton(StartButtons::createPiglinStartButton);
    }

    private static Button createVillagerStartButton() {
        return new Button(
                "Villagers",
                ICON_SIZE,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/villager.png"),
                (Keybinding) null,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.STARTRTS_VILLAGERS,
                () -> !TutorialClientEvents.isAtOrPastStage(TutorialStage.PLACE_WORKERS_B) || !PlayerClientEvents.canStartRTS,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.STARTRTS_VILLAGERS),
                () -> {},
                List.of(
                        FormattedCharSequence.forward(I18n.get("hud.startbuttons.villagers.reignofnether.first"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("hud.startbuttons.villagers.reignofnether.second"), Style.EMPTY)
                )
        );
    }


    private static Button createMonsterStartButton() {
        return new Button(
                "Monsters",
                ICON_SIZE,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/creeper.png"),
                (Keybinding) null,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.STARTRTS_MONSTERS,
                () -> TutorialClientEvents.isEnabled() || !PlayerClientEvents.canStartRTS,
                () -> !TutorialClientEvents.isEnabled(),
                () -> CursorClientEvents.setLeftClickAction(UnitAction.STARTRTS_MONSTERS),
                () -> { },
                List.of(
                        FormattedCharSequence.forward(I18n.get("hud.startbuttons.monsters.reignofnether.first"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("hud.startbuttons.monsters.reignofnether.second"), Style.EMPTY)
                )
        );
    }

    private static Button createPiglinStartButton() {
        return new Button(
                "Piglins",
                ICON_SIZE,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/grunt.png"),
                (Keybinding) null,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.STARTRTS_PIGLINS,
                () -> TutorialClientEvents.isEnabled() || !PlayerClientEvents.canStartRTS,
                () -> !TutorialClientEvents.isEnabled(),
                () -> CursorClientEvents.setLeftClickAction(UnitAction.STARTRTS_PIGLINS),
                () -> { },
                List.of(
                        FormattedCharSequence.forward(I18n.get("hud.startbuttons.piglins.reignofnether.first"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("hud.startbuttons.piglins.reignofnether.second"), Style.EMPTY)
                )
        );
    }

    // Dynamic button management methods
    public static void addButton(ActionButtons.ButtonFactory buttonFactory) {
        Button button = buttonFactory.createButton();
        BUTTONS.add(button);
        buttonAddedListeners.forEach(listener -> listener.accept(button));
    }

    public static void removeButton(Button button) {
        BUTTONS.remove(button);
        buttonRemovedListeners.forEach(listener -> listener.accept(button));
    }

    public static Button getButtonByName(String name) {
        return BUTTONS.stream().filter(button -> button.getName().equals(name)).findFirst().orElse(null);
    }

    public static List<Button> getButtons() {
        return BUTTONS;
    }

    public static void addButtonAddedListener(Consumer<Button> listener) {
        buttonAddedListeners.add(listener);
    }

    public static void addButtonRemovedListener(Consumer<Button> listener) {
        buttonRemovedListeners.add(listener);
    }
}

