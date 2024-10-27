package com.solegendary.reignofnether.guiscreen;


import com.solegendary.reignofnether.registrars.ContainerRegistrar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Class which overrides the inventory GUI with an RTS-like one where the mouse is always visible and objects on screen
 * can be clicked on for selection
 *
 * @author SoLegendary
 */

public class TopdownGuiContainer extends AbstractContainerMenu {

    public static final Component TITLE = Component.literal("topdowngui_container");

    public static TopdownGuiContainer createContainerClientSide(int windowID, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf extraData) {
        return new TopdownGuiContainer(windowID, playerInventory);
    }

    public static MenuConstructor getServerContainerProvider()
    {
        return (id, playerInventory, serverPlayer) -> new TopdownGuiContainer(id, playerInventory);
    }

    public TopdownGuiContainer(int id, Inventory playerInventory)
    {
        super(ContainerRegistrar.TOPDOWNGUI_CONTAINER.get(), id);
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return null;
    }

    @Override
    // equivalent of old isWithinUsableDistance() && canInteractWith()
    public boolean stillValid(Player p_75145_1_) {
        return true;
    }
}
