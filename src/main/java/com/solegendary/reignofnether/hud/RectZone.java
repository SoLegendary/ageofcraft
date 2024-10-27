package com.solegendary.reignofnether.hud;

// coordinate pairs that indicate HUD areas so we can disallow world interactions when the mouse is over them
// ie. players can't click behind the HUD
public class RectZone {

    int x1, y1, x2, y2; // TL -> BR

    RectZone(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    public static RectZone getZoneByLW(int x1, int y1, int width, int height) {
        return new RectZone(x1, y1, x1 + width, y1 + height);
    }
    public boolean isMouseOver(int mouseX, int mouseY) {
        return (mouseX >= x1 &&
                mouseY >= y1 &&
                mouseX < x2 &&
                mouseY < y2);
    }
}