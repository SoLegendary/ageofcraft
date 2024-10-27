package com.solegendary.reignofnether.keybinds;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static final Keybinding pause = new Keybinding(GLFW.GLFW_KEY_ESCAPE, "ESC", "Pause");
    public static final Keybinding zoomIn = new Keybinding(GLFW.GLFW_KEY_KP_ADD, "+", "+");
    public static final Keybinding zoomOut = new Keybinding(GLFW.GLFW_KEY_KP_SUBTRACT, "-", "+");
    public static final Keybinding panPlusX = new Keybinding(GLFW.GLFW_KEY_LEFT, "X+", "Pan map X+");
    public static final Keybinding panMinusX = new Keybinding(GLFW.GLFW_KEY_RIGHT, "X-", "Pan map X-");
    public static final Keybinding panPlusZ = new Keybinding(GLFW.GLFW_KEY_UP, "Z+", "Pan map Z+");
    public static final Keybinding panMinusZ = new Keybinding(GLFW.GLFW_KEY_DOWN, "Z-", "Pan map Z-");
    public static final Keybinding reset = new Keybinding(GLFW.GLFW_KEY_RIGHT_CONTROL, "RC", "Reset");
    public static final Keybinding shiftMod = new Keybinding(GLFW.GLFW_KEY_LEFT_SHIFT, "SHFT", "Shift");
    public static final Keybinding ctrlMod = new Keybinding(GLFW.GLFW_KEY_LEFT_CONTROL, "LC", "Ctrl");
    public static final Keybinding altMod = new Keybinding(GLFW.GLFW_KEY_LEFT_ALT, "ALT", "Alt");
    public static final Keybinding cancelBuild = new Keybinding(GLFW.GLFW_KEY_DELETE, "DEL", "Cancel");
    public static final Keybinding keyQ = new Keybinding(GLFW.GLFW_KEY_Q, "Q", "Q");
    public static final Keybinding keyW = new Keybinding(GLFW.GLFW_KEY_W, "W", "W");
    public static final Keybinding keyE =  new Keybinding(GLFW.GLFW_KEY_E, "E", "E");
    public static final Keybinding keyR = new Keybinding(GLFW.GLFW_KEY_R, "R", "R");
    public static final Keybinding keyT = new Keybinding(GLFW.GLFW_KEY_T, "T", "T");
    public static final Keybinding keyY = new Keybinding(GLFW.GLFW_KEY_Y, "Y", "Y");
    public static final Keybinding keyU = new Keybinding(GLFW.GLFW_KEY_U, "U", "U");
    public static final Keybinding keyI = new Keybinding(GLFW.GLFW_KEY_I, "I", "I");
    public static final Keybinding keyO = new Keybinding(GLFW.GLFW_KEY_O, "O", "O");
    public static final Keybinding keyP = new Keybinding(GLFW.GLFW_KEY_P, "P", "P");
    public static final Keybinding keyV = new Keybinding(GLFW.GLFW_KEY_V, "V", "V");
    public static final Keybinding keyL = new Keybinding(GLFW.GLFW_KEY_L, "L", "L");
    public static final Keybinding keyM = new Keybinding(GLFW.GLFW_KEY_M, "M", "M");
    public static final Keybinding keyF = new Keybinding(GLFW.GLFW_KEY_F, "F", "F");
    public static final Keybinding keyC = new Keybinding(GLFW.GLFW_KEY_C, "C", "C");
    public static final Keybinding keyD = new Keybinding(GLFW.GLFW_KEY_D, "D", "D");
    public static final Keybinding keyJ = new Keybinding(GLFW.GLFW_KEY_J, "J", "J");
    public static final Keybinding keyK = new Keybinding(GLFW.GLFW_KEY_K, "K", "K");
    public static final Keybinding keyG = new Keybinding(GLFW.GLFW_KEY_G, "G", "G");
    public static final Keybinding attack = new Keybinding(GLFW.GLFW_KEY_A, "A", "Attack");
    public static final Keybinding stop = new Keybinding(GLFW.GLFW_KEY_S, "S", "Stop");
    public static final Keybinding hold = new Keybinding(GLFW.GLFW_KEY_H, "H", "Hold");
    public static final Keybinding move = new Keybinding(GLFW.GLFW_KEY_M, "M", "Move");
    public static final Keybinding build = new Keybinding(GLFW.GLFW_KEY_B, "B", "Build");
    public static final Keybinding gather = new Keybinding(GLFW.GLFW_KEY_G, "G", "Gather");
    public static final Keybinding garrison = new Keybinding(GLFW.GLFW_KEY_N, "N", "Garrison");
    public static final Keybinding chat = new Keybinding(GLFW.GLFW_KEY_ENTER, "ENTR", "Chat");
    public static final Keybinding deselect = new Keybinding(GLFW.GLFW_KEY_GRAVE_ACCENT, "TILDE", "Deselect");
    public static final Keybinding tab = new Keybinding(GLFW.GLFW_KEY_TAB, "TAB", "tab");

    public static final Keybinding[] nums = {
        new Keybinding(GLFW.GLFW_KEY_0, "0", "0"),
        new Keybinding(GLFW.GLFW_KEY_1, "1", "1"),
        new Keybinding(GLFW.GLFW_KEY_2, "2", "2"),
        new Keybinding(GLFW.GLFW_KEY_3, "3", "3"),
        new Keybinding(GLFW.GLFW_KEY_4, "4", "4"),
        new Keybinding(GLFW.GLFW_KEY_5, "5", "5"),
        new Keybinding(GLFW.GLFW_KEY_6, "6", "6"),
        new Keybinding(GLFW.GLFW_KEY_7, "7", "7"),
        new Keybinding(GLFW.GLFW_KEY_8, "8", "8"),
        new Keybinding(GLFW.GLFW_KEY_9, "9", "9")
    };
    public static final Keybinding[] fnums = {
        new Keybinding(GLFW.GLFW_KEY_F1, "F1", "F1"),
        new Keybinding(GLFW.GLFW_KEY_F2, "F2", "F2"),
        new Keybinding(GLFW.GLFW_KEY_F3, "F3", "F3"),
        new Keybinding(GLFW.GLFW_KEY_F4, "F4", "F4"),
        new Keybinding(GLFW.GLFW_KEY_F5, "F5", "F5"),
        new Keybinding(GLFW.GLFW_KEY_F6, "F6", "F6"),
        new Keybinding(GLFW.GLFW_KEY_F7, "F7", "F7"),
        new Keybinding(GLFW.GLFW_KEY_F8, "F8", "F8"),
        new Keybinding(GLFW.GLFW_KEY_F9, "F9", "F9"),
        new Keybinding(GLFW.GLFW_KEY_F10, "F10", "F10"),
        new Keybinding(GLFW.GLFW_KEY_F11, "F11", "F11"),
        new Keybinding(GLFW.GLFW_KEY_F12, "F12", "F12")
    };

    public static Keybinding getNum(int i) {
        return nums[i];
    }
    public static Keybinding getFnum(int i) {
        return fnums[i-1];
    }
}
