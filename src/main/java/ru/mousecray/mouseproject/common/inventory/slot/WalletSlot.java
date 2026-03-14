/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.mousecray.mouseproject.common.item.wallet.ItemWallet;

import javax.annotation.Nonnull;

public class WalletSlot extends Slot {
    public WalletSlot(IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override public boolean isItemValid(@Nonnull ItemStack stack) { return stack.getItem() instanceof ItemWallet; }
}