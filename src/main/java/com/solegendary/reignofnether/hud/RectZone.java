package com.solegendary.reignofnether.hud;

import java.awt.*;

// coordinate pairs that indicate HUD areas so we can disallow world interactions when the mouse is over them
// ie. players can't click behind the HUD
public class RectZone {

    private final Rectangle bounds;

    // Constructor using top-left and bottom-right coordinates
    public RectZone(int x1, int y1, int x2, int y2) {
        this.bounds = new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    // Constructor using top-left and dimensions (width and height)
    public static RectZone getZoneByLW(int x1, int y1, int width, int height) {
        return new RectZone(x1, y1, x1 + width, y1 + height);
    }

    // Method to check if the mouse is over the zone
    public boolean isMouseOver(int mouseX, int mouseY) {
        return bounds.contains(mouseX, mouseY);
    }
    public int getX1() { return bounds.x; }
    public int getY1() { return bounds.y; }
    public int getX2() { return bounds.x + bounds.width; }
    public int getY2() { return bounds.y + bounds.height; }

}