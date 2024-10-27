package com.solegendary.reignofnether.guiscreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/**
 * Class which overrides the inventory GUI with an RTS-like one where the mouse is always visible and objects on screen
 * can be clicked on for selection
 *
 * @author SoLegendary
 */

public class TopdownGui extends AbstractContainerScreen<TopdownGuiContainer> {

    public TopdownGui(TopdownGuiContainer p_i51105_1_, Inventory p_i51105_2_, Component p_i51105_3_) {
        super(p_i51105_1_, p_i51105_2_, p_i51105_3_);
        passEvents = true; // enables keybindings in guievents for this menu
    }

    @Override
    protected void renderBg(PoseStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // allow to close but in ScreenOpenEvent open the pause menu, and vice versa
    }


}
