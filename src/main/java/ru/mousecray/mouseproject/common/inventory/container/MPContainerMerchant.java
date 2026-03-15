/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.inventory.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.inventory.MPInventoryMerchant;
import ru.mousecray.mouseproject.common.inventory.slot.MPSlotMerchantResult;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MPContainerMerchant extends Container {
    private final IMerchant           merchant;
    private final MPInventoryMerchant merchantInv;

    private final World world;

    public MPContainerMerchant(InventoryPlayer playerInv, IMerchant merchant, World world) {
        this.merchant = merchant;
        this.world = world;
        merchantInv = new MPInventoryMerchant(playerInv.player, merchant);
        addSlotToContainer(new Slot(merchantInv, 0, 36, 53));
        addSlotToContainer(new MPSlotMerchantResult(playerInv.player, merchant, merchantInv, 2, 120, 53));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    public MPInventoryMerchant getMerchantInventory() { return merchantInv; }

    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        merchantInv.resetRecipeAndSlots();
        super.onCraftMatrixChanged(inv);
    }

    public void setCurrentRecipeIndex(int currentRecipeIndex)     { merchantInv.setCurrentRecipeIndex(currentRecipeIndex); }

    @Override public boolean canInteractWith(EntityPlayer player) { return merchant.getCustomer() == player; }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot      slot  = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (index == 2) {
                if (!mergeItemStack(slotStack, 3, 39, true)) return ItemStack.EMPTY;
                slot.onSlotChange(slotStack, stack);
            } else if (index != 0 && index != 1) {
                if (index < 30) {
                    if (!mergeItemStack(slotStack, 30, 39, false)) return ItemStack.EMPTY;
                } else if (index < 39 && !mergeItemStack(slotStack, 3, 30, false)) return ItemStack.EMPTY;
            } else if (!mergeItemStack(slotStack, 3, 39, false)) return ItemStack.EMPTY;

            if (slotStack.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();

            if (slotStack.getCount() == stack.getCount()) return ItemStack.EMPTY;

            slot.onTake(player, slotStack);
        }

        return stack;
    }


    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        merchant.setCustomer(null);
        super.onContainerClosed(player);

        if (!world.isRemote) {
            ItemStack stack = merchantInv.removeStackFromSlot(0);
            if (!stack.isEmpty()) player.dropItem(stack, false);
        }
    }
}
