/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.inventory;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import ru.mousecray.mouseproject.Tags;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class MPInventory implements IInventory {
    private NonNullList<ItemStack> inventory;

    private final String specificName, customName;

    public MPInventory(int invSize, String specificName, @Nullable String customName) {
        this.specificName = specificName;
        this.customName = customName;
        inventory = NonNullList.withSize(invSize, ItemStack.EMPTY);
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : Tags.MOD_ID + ":container." + specificName;
    }

    @Override public boolean hasCustomName() { return customName != null && !customName.isEmpty(); }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName()
                ? new TextComponentString(getName())
                : new TextComponentTranslation(getName(), new Object[0]);
    }

    @Override public int getSizeInventory() { return inventory.size(); }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < inventory.size() ? inventory.get(index) : ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getStacks() { return inventory; }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(inventory, index, count);
        if (!itemstack.isEmpty()) markDirty();
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (!inventory.get(index).isEmpty()) {
            ItemStack itemstack = inventory.get(index);
            inventory.set(index, ItemStack.EMPTY);
            return itemstack;
        } else return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        if (stack == null) stack = ItemStack.EMPTY;
        inventory.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) stack.setCount(getInventoryStackLimit());
        markDirty();
    }

    public ItemStack addItem(ItemStack stack) {
        ItemStack itemstack = stack.copy();

        for (int i = 0; i < getSizeInventory(); ++i) {
            ItemStack itemstack1 = getStackInSlot(i);

            if (itemstack1.isEmpty()) {
                setInventorySlotContents(i, itemstack);
                markDirty();
                return ItemStack.EMPTY;
            }

            if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
                int j = Math.min(getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

                if (k > 0) {
                    itemstack1.grow(k);
                    itemstack.shrink(k);

                    if (itemstack.isEmpty()) {
                        markDirty();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (itemstack.getCount() != stack.getCount()) markDirty();
        return itemstack;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : inventory) if (!itemstack.isEmpty()) return false;
        return true;
    }

    @Override public int getInventoryStackLimit()                                     { return 64; }
    @Override public void markDirty()                                                 { }
    @Override public boolean isUsableByPlayer(@Nullable EntityPlayer player)          { return true; }
    @Override public void openInventory(@Nullable EntityPlayer player)                { }
    @Override public void closeInventory(@Nullable EntityPlayer player)               { }

    @Override public boolean isItemValidForSlot(int index, @Nullable ItemStack stack) { return true; }

    @Override public int getField(int id)                                             { return 0; }
    @Override public void setField(int id, int value)                                 { }
    @Override public int getFieldCount()                                              { return 0; }

    @Override public void clear()                                                     { inventory.clear(); }

    public void writeToNBT(@Nullable NBTTagCompound compound) {
        if (compound != null) ItemStackHelper.saveAllItems(compound, inventory);
    }

    public void readFromNBT(@Nullable NBTTagCompound compound) {
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        if (compound != null) ItemStackHelper.loadAllItems(compound, inventory);
    }

    public void copy(@Nullable WalletInventory inv) {
        if (inv != null) {
            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                ItemStack stack = inv.getStackInSlot(i);
                inventory.set(i, (stack.isEmpty() ? ItemStack.EMPTY : stack.copy()));
            }
        }
    }
}