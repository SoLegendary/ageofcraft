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

    public final String name; // Made `name` final to prevent changes after instantiation
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

    public Supplier<Boolean> isSelected;
    public Supplier<Boolean> isHidden;
    public Supplier<Boolean> isEnabled;
    public Runnable onLeftClick;
    public Runnable onRightClick;
    public List<FormattedCharSequence> tooltipLines;

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

    // New getter for `name` for use in `getButtonByName`
    public String getName() {
        return name;
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

        if (iconResource != null) {
            MyRenderer.renderIcon(
                    poseStack,
                    iconResource,
                    x+4 + (7 - iconSize/2), y+4 + (7 - iconSize/2),
                    iconSize
            );
        }

        if (this.hotkey != null) {
            String hotkeyStr = hotkey.buttonLabel;
            hotkeyStr = hotkeyStr.substring(0, Math.min(3, hotkeyStr.length()));
            GuiComponent.drawCenteredString(poseStack, MC.font,
                    hotkeyStr,
                    x + iconSize + 8 - (hotkeyStr.length() * 4),
                    y + iconSize - 1,
                    0xFFFFFF);
        }

        if (isEnabled.get() && (isSelected.get() || (hotkey != null && hotkey.isDown()) || (isMouseOver(mouseX, mouseY) && MiscUtil.isLeftClickDown(MC)))) {
            ResourceLocation iconFrameSelectedResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_selected.png");
            MyRenderer.renderIcon(
                    poseStack,
                    iconFrameSelectedResource,
                    x-1, y-1,
                    iconFrameSelectedSize
            );
        }

        if (isEnabled.get() && isMouseOver(mouseX, mouseY)) {
            GuiComponent.fill(poseStack,
                    x, y,
                    x + iconFrameSize,
                    y + iconFrameSize,
                    0x32FFFFFF);
        }

        if (greyPercent > 0 || !isEnabled.get()) {
            int greyHeightPx = Math.round(greyPercent * iconFrameSize);
            if (!isEnabled.get())
                greyHeightPx = 0;

            GuiComponent.fill(poseStack,
                    x, y + greyHeightPx,
                    x + iconFrameSize,
                    y + iconFrameSize,
                    0x99000000);
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

    public void checkClicked(int mouseX, int mouseY, boolean leftClick) {
        if (!OrthoviewClientEvents.isEnabled() || !isEnabled.get())
            return;

        if (isMouseOver(mouseX, mouseY) && MC.player != null) {
            if (leftClick && this.onLeftClick != null) {
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
                this.onLeftClick.run();
            } else if (!leftClick && this.onRightClick != null) {
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
                this.onRightClick.run();
            }
        }
    }

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

