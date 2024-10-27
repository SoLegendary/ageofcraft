package com.solegendary.reignofnether.guiscreen;

import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handler for TopdownGui, the GUI screen that allows for cursor movement on screen
 * Doing stuff like initialising it and controlling where it comes up in the game client
 *
 * @author SoLegendary
 */

public class TopdownGuiClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    private static int noScreenTicks = 0; // ticks that no screen has been opened
    private static boolean shouldPause = false;
    private static final int GUI_SCALE_MOD = 3;

    // if no other screen is open and we've got orthoview enabled, open a screen based on shouldPause
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent evt) {
        if (OrthoviewClientEvents.isEnabled() && Minecraft.getInstance().screen == null) {
            noScreenTicks += 1;
            if (noScreenTicks >= 3) {
                if (shouldPause) {
                    shouldPause = false;
                    MC.setScreen(new PauseScreen(true));
                }
                else
                    TopdownGuiServerboundPacket.openTopdownGui(MC.player.getId());
                noScreenTicks = 0;
            }
        }
    }

    // allow closing by pressing escape
    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing evt) {
        if (evt.getScreen().isPauseScreen())
            shouldPause = false;

        int i = MC.getWindow().calculateScale(MC.options.guiScale().get(), MC.isEnforceUnicode());
        MC.getWindow().setGuiScale(i);
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening evt) {
        if (evt.getScreen() instanceof TopdownGui) {
            int i = MC.getWindow().calculateScale(GUI_SCALE_MOD, MC.isEnforceUnicode());
            MC.getWindow().setGuiScale(i);
        } else {
            int i = MC.getWindow().calculateScale(MC.options.guiScale().get(), MC.isEnforceUnicode());
            MC.getWindow().setGuiScale(i);
        }
    }

    @SubscribeEvent
    public static void beforeGuiRender(ScreenEvent.Render.Pre evt) {
        // cancel drawing the GUI
        if (evt.getScreen() instanceof TopdownGui)
            evt.setCanceled(true);
    }

    // prevent opening inventory with E or advancements with L
    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyPressed.Pre evt) {
        if (OrthoviewClientEvents.isEnabled()) {
            if (evt.getKeyCode() == Keybindings.pause.key)
                shouldPause = true;
            else if (evt.getKeyCode() == MC.options.keyInventory.getKey().getValue())
                evt.setCanceled(true);
            else if (evt.getKeyCode() == MC.options.keyAdvancements.getKey().getValue())
                evt.setCanceled(true);
        }
    }
}