package com.solegendary.reignofnether.hud;

import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class TitleClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final Random random = new Random();
    private static Faction titleBackgroundFaction = getRandomFaction();
    private static PanoramaRenderer panorama = new PanoramaRenderer(getCubeMap());
    public static String splash = getRandomSplash();

    public static PanoramaRenderer getPanorama() { return panorama; }

    private static CubeMap getCubeMap() {
        String dir = "";

        switch (titleBackgroundFaction) {
            case VILLAGERS -> dir = "villagers";
            case MONSTERS -> dir = "monsters";
            case PIGLINS -> dir = "piglins";
        }
        return new CubeMap(new ResourceLocation("textures/gui/title/background/" + dir + "/panorama"));
    }

    private static Faction getRandomFaction() {
        Faction result = Faction.VILLAGERS;
        switch (random.nextInt(3)) {
            case 1 -> result = Faction.MONSTERS;
            case 2 -> result = Faction.PIGLINS;
        }
        return result;
    }

    private static Faction getNewRandomFaction() {
        Faction result = Faction.VILLAGERS;
        switch (titleBackgroundFaction) {
            case VILLAGERS -> result = random.nextBoolean() ? Faction.MONSTERS : Faction.PIGLINS;
            case MONSTERS -> result = random.nextBoolean() ? Faction.VILLAGERS : Faction.PIGLINS;
            case PIGLINS -> result = random.nextBoolean() ? Faction.VILLAGERS : Faction.MONSTERS;
        }
        return result;
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening evt) {
        if (evt.getScreen() instanceof TitleScreen) {
            titleBackgroundFaction = getNewRandomFaction();
            panorama = new PanoramaRenderer(getCubeMap());
        }
    }

    private static String getRandomSplash() {
        return MC.getSplashManager().getSplash();
    }

    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyPressed.KeyPressed.Pre evt) {
        if (evt.getKeyCode() == GLFW.GLFW_KEY_SPACE)
            splash = getRandomSplash();
    }
}
