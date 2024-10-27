package com.solegendary.reignofnether.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class Keybinding {
    public final int key; // GLFW key
    public final String buttonLabel; // string shown on buttons
    public final String description; // full name shown in menu

    public Keybinding(int key, String buttonLabel, String description) {
        this.key = key;
        this.buttonLabel = buttonLabel;
        this.description = description;
    }

    public boolean isDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }
}
