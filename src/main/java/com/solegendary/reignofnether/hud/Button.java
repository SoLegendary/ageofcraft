package com.solegendary.reignofnether.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.healthbars.HealthBarClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Class for creating buttons that consist of an icon inside of a frame which is selectable
 * All functionality that occurs on click/hover/etc. is enforced by HudClientEvents
 */

public class Button {

    public static final int itemIconSize = 14;

    public String name;
    public int x; // top left
    public int y;
    int iconSize;
    public static int iconFrameSize = 22;
    public static int iconFrameSelectedSize = 24;

    public ResourceLocation iconResource;
    public ResourceLocation bgIconResource = null; // for rendering a background icon (eg. for mounted unit passengers)
    public ResourceLocation frameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame.png");

    public Keybinding hotkey = null; // for action/ability buttons
    public LivingEntity entity = null; // for selected unit buttons
    public Building building = null; // for selected building buttons

    /** https://stackoverflow.com/questions/29945627/java-8-lambda-void-argument
     * Supplier       ()    -> x
     * Consumer       x     -> ()
     * Runnable       ()    -> ()
     * Predicate      x     -> boolean
     */
    public Supplier<Boolean> isSelected; // controls selected frame rendering
    public Supplier<Boolean> isHidden; // special highlighting for an on-state (eg. auto-cast/auto-producing)
    public Supplier<Boolean> isEnabled; // is the button allowed to be used right now? (eg. off cooldown)
    public Runnable onLeftClick;
    public Runnable onRightClick;
    public List<FormattedCharSequence> tooltipLines;

    // used for cooldown indication, productionItem progress, etc.
    // @ 0.0, appears clear and normal
    // @ 0.5, bottom half is greyed out
    // @ 1.0, whole button is greyed out
    public float greyPercent = 0.0f;

    Minecraft MC = Minecraft.getInstance();

    // constructor for ability/action/production buttons
    public Button(String name, int iconSize, ResourceLocation iconRl, @Nullable Keybinding hotkey, Supplier<Boolean> isSelected,
                  Supplier<Boolean> isHidden, Supplier<Boolean> isEnabled, @Nullable Runnable onLeftClick,
                  @Nullable Runnable onRightClick, @Nullable List<FormattedCharSequence> tooltipLines) {
        this.name = name;
        this.iconResource = iconRl;
        this.iconSize = iconSize;
        this.hotkey = hotkey;
        this.isSelected = isSelected;
        this.isHidden = isHidden;
        this.isEnabled = isEnabled;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.tooltipLines = tooltipLines;
    }

    // constructor for ability/action/production buttons with non-default frame
    public Button(String name, int iconSize, ResourceLocation iconRl, ResourceLocation frameRl, @Nullable Keybinding hotkey, Supplier<Boolean> isSelected,
                  Supplier<Boolean> isHidden, Supplier<Boolean> isEnabled, @Nullable Runnable onLeftClick,
                  @Nullable Runnable onRightClick, @Nullable List<FormattedCharSequence> tooltipLines) {
        this.name = name;
        this.iconResource = iconRl;
        this.frameResource = frameRl;
        this.iconSize = iconSize;
        this.hotkey = hotkey;
        this.isSelected = isSelected;
        this.isHidden = isHidden;
        this.isEnabled = isEnabled;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.tooltipLines = tooltipLines;
    }

    // constructor for unit selection buttons
    public Button(String name, int iconSize, ResourceLocation iconRl, LivingEntity entity, Supplier<Boolean> isSelected,
                  Supplier<Boolean> isHidden, Supplier<Boolean> isEnabled, @Nullable Runnable onLeftClick,
                  @Nullable Runnable onRightClick, @Nullable List<FormattedCharSequence> tooltipLines) {
        this.name = name;
        this.iconResource = iconRl;
        this.iconSize = iconSize;
        this.entity = entity;
        this.isSelected = isSelected;
        this.isHidden = isHidden;
        this.isEnabled = isEnabled;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.tooltipLines = tooltipLines;
    }

    // constructor for building selection buttons
    public Button(String name, int iconSize, ResourceLocation iconRl, Building building, Supplier<Boolean> isSelected,
                  Supplier<Boolean> isHidden, Supplier<Boolean> isEnabled, @Nullable Runnable onLeftClick,
                  @Nullable Runnable onRightClick, @Nullable List<FormattedCharSequence> tooltipLines) {
        this.name = name;
        this.iconResource = iconRl;
        this.iconSize = iconSize;
        this.building = building;
        this.isSelected = isSelected;
        this.isHidden = isHidden;
        this.isEnabled = isEnabled;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.tooltipLines = tooltipLines;
    }

    public void renderHealthBar(PoseStack poseStack) {
        if (entity != null)
            HealthBarClientEvents.renderForEntity(poseStack, entity,
                    x + ((float) iconFrameSize / 2), y - 5,
                    iconFrameSize - 1,
                    HealthBarClientEvents.RenderMode.GUI_ICON);
        else if (building != null)
            HealthBarClientEvents.renderForBuilding(poseStack, building,
                    x + ((float) iconFrameSize / 2), y - 5,
                    iconFrameSize - 1,
                    HealthBarClientEvents.RenderMode.GUI_ICON);
    }

    public void render(PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        if (this.frameResource != null)
            MyRenderer.renderIconFrameWithBg(poseStack, this.frameResource, x, y, iconFrameSize, 0x64000000);

        if (bgIconResource != null) {
            MyRenderer.renderIcon(
                    poseStack,
                    bgIconResource,
                    x+4 + (7 - iconSize/2), y+4 + (7 - iconSize/2),
                    iconSize
            );
        }
        // item/unit icon
        if (iconResource != null) {
            MyRenderer.renderIcon(
                    poseStack,
                    iconResource,
                    x+4 + (7 - iconSize/2), y+4 + (7 - iconSize/2),
                    iconSize
            );
        }
        // hotkey letter
        if (this.hotkey != null) {
            String hotkeyStr = hotkey.buttonLabel;
            hotkeyStr = hotkeyStr.substring(0,Math.min(3, hotkeyStr.length()));
            GuiComponent.drawCenteredString(poseStack, MC.font,
                    hotkeyStr,
                    x + iconSize + 8 - (hotkeyStr.length() * 4),
                    y + iconSize - 1,
                    0xFFFFFF);
        }

        // user is holding click or hotkey down over the button and render frame if so
        if (isEnabled.get() && (isSelected.get() || (hotkey != null && hotkey.isDown()) || (isMouseOver(mouseX, mouseY) && MiscUtil.isLeftClickDown(MC)))) {
            ResourceLocation iconFrameSelectedResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_selected.png");
            MyRenderer.renderIcon(
                    poseStack,
                    iconFrameSelectedResource,
                    x-1,y-1,
                    iconFrameSelectedSize
            );
        }
        // light up on hover
        if (isEnabled.get() && isMouseOver(mouseX, mouseY)) {
            GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                    x, y,
                    x + iconFrameSize,
                    y + iconFrameSize,
                    0x32FFFFFF); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255
        }

        if (greyPercent > 0 || !isEnabled.get()) {
            int greyHeightPx = Math.round(greyPercent * iconFrameSize);
            if (!isEnabled.get())
                greyHeightPx = 0;

            GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                    x, y + greyHeightPx,
                    x + iconFrameSize,
                    y + iconFrameSize,
                    0x99000000); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255
        }
    }

    public void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        MyRenderer.renderTooltip(poseStack, tooltipLines, mouseX, mouseY);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return (mouseX >= x &&
                mouseY >= y &&
                mouseX < x + iconFrameSize &&
                mouseY < y + iconFrameSize
        );
    }

    // must be done from mouse press event
    public void checkClicked(int mouseX, int mouseY, boolean leftClick) {
        if (!OrthoviewClientEvents.isEnabled() || !isEnabled.get())
            return;

        if (isMouseOver(mouseX, mouseY) && MC.player != null) {
            if (leftClick && this.onLeftClick != null) {
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
                this.onLeftClick.run();
            }
            else if (!leftClick && this.onRightClick != null) {
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
                this.onRightClick.run();
            }
        }
    }

    // must be done from key press event
    public void checkPressed(int key) {
        if (!OrthoviewClientEvents.isEnabled() || !isEnabled.get())
            return;

        if (hotkey != null && hotkey.key == key) {
            if (MC.player != null)
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
            this.onLeftClick.run();
        }
    }
}
