package com.solegendary.reignofnether.tps;

import com.solegendary.reignofnether.keybinds.Keybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TPSClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    private static double tickTime = 10; // time to perform one tick in milliseconds
    private static boolean enabled = false;

    public static void updateTickTime(double tickTime) {
        TPSClientEvents.tickTime = tickTime;
    }

    @SubscribeEvent
    // can't use ScreenEvent.KeyboardKeyPressedEvent as that only happens when a screen is up
    public static void onInput(InputEvent.Key evt) {
        if (evt.getAction() == GLFW.GLFW_PRESS) {
            if (evt.getKey() == Keybindings.getFnum(7).key)
                enabled = !enabled;
        }
    }

    @SubscribeEvent
    public static void onRenderOverLay(RenderGuiOverlayEvent.Pre evt) {
        if (!enabled)
            return;

        int x = evt.getWindow().getGuiScaledWidth() - 55;
        int y = 20;

        double worldTPS = Math.min(1000.0 / tickTime, 99.99);

        // ARGB, shaded from red to green at 5-25 TPS
        double tpsForColor = worldTPS + 5;
        int r = (int) Math.round(tpsForColor / 20);
        int g = 0xFF - (int) Math.round(tpsForColor / 20);
        int b = 0;

        int col = (0xFF << 24) | (b << 16) | (g << 8) | (r);

        String tickStr = "Tick: " + String.format("%.2f", tickTime);
        GuiComponent.drawString(evt.getPoseStack(), MC.font, tickStr, x,y, col);

        // technically is bound to 20TPS but good to see the theoretical amount
        String tpsStr = "TPS: " + String.format("%.2f", worldTPS);
        GuiComponent.drawString(evt.getPoseStack(), MC.font, tpsStr, x,y + 10, col);

        String fpsStr = "FPS: " + Minecraft.getInstance().fpsString.substring(0,6).replace("fps","");
        GuiComponent.drawString(evt.getPoseStack(), MC.font, fpsStr, x,y + 20, 0xFFFFFFFF);
    }
}
