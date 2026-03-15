/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.inventory;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import ru.mousecray.mouseproject.Tags;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MPInventoryMerchant implements IInventory {
    private final IMerchant              merchant;
    private final NonNullList<ItemStack> slots = NonNullList.withSize(3, ItemStack.EMPTY);
    private final EntityPlayer           player;
    private       MerchantRecipe         currentRecipe;
    private       int                    currentRecipeIndex;

    public MPInventoryMerchant(EntityPlayer player, IMerchant merchant) {
        this.player = player;
        this.merchant = merchant;
    }

    @Override public int getSizeInventory() { return slots.size(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : slots) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getStackInSlot(int index) { return slots.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = slots.get(index);

        if (index == 2 && !stack.isEmpty()) return ItemStackHelper.getAndSplit(slots, index, stack.getCount());
        else {
            ItemStack stack1 = ItemStackHelper.getAndSplit(slots, index, count);
            if (!stack1.isEmpty() && inventoryResetNeededOnSlotChange(index)) resetRecipeAndSlots();
            return stack1;
        }
    }

    private boolean inventoryResetNeededOnSlotChange(int slot) { return slot == 0 || slot == 1; }

    @Override public ItemStack removeStackFromSlot(int index)  { return ItemStackHelper.getAndRemove(slots, index); }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        slots.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) stack.setCount(getInventoryStackLimit());
        if (inventoryResetNeededOnSlotChange(index)) resetRecipeAndSlots();
    }

    @Override public String getName()                                       { return Tags.MOD_ID + ".mob.villager"; }
    @Override public boolean hasCustomName()                                { return false; }
    @Override public ITextComponent getDisplayName()                        { return new TextComponentTranslation(getName()); }
    @Override public int getInventoryStackLimit()                           { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player)          { return merchant.getCustomer() == player; }
    @Override public void openInventory(EntityPlayer player)                { }
    @Override public void closeInventory(EntityPlayer player)               { }
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return true; }

    @Override public void markDirty()                                       { resetRecipeAndSlots(); }

    public void resetRecipeAndSlots() {
        currentRecipe = null;
        ItemStack stack = slots.get(0);

        if (stack.isEmpty()) setInventorySlotContents(2, ItemStack.EMPTY);
        else {
            MerchantRecipeList recipeList = merchant.getRecipes(player);

            if (recipeList != null) {
                if (currentRecipeIndex > 0 && currentRecipeIndex < recipeList.size()) {
                    MerchantRecipe recipe = recipeList.get(currentRecipeIndex);
                    if (recipe != null) {
                        ItemStack itemToBuy       = recipe.getItemToBuy();
                        ItemStack secondItemToBuy = recipe.getSecondItemToBuy();
                        ItemStack itemToSell      = recipe.getItemToSell();

                        //TODO: economy
                        ItemStack price      = ItemStack.EMPTY;
                        ItemStack itemPrice1 = ItemStack.EMPTY;
                        ItemStack itemPrice2 = ItemStack.EMPTY;
                        ItemStack result     = ItemStack.EMPTY;

                        if (itemToBuy.getItem() == Items.EMERALD && !itemToBuy.hasTagCompound()) {
//                            price = new ItemStack()
                        } else if (secondItemToBuy.getItem() == Items.EMERALD && !secondItemToBuy.hasTagCompound()) {

                        } else {

                        }
                    }
                }
                MerchantRecipe recipe = recipeList.canRecipeBeUsed(stack, null, currentRecipeIndex);

                if (recipe != null && !recipe.isRecipeDisabled()) {
                    currentRecipe = recipe;
                    setInventorySlotContents(2, recipe.getItemToSell().copy());
                } else setInventorySlotContents(2, ItemStack.EMPTY);
            }

            merchant.verifySellingItem(getStackInSlot(2));
        }
    }

    public MerchantRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipeIndex(int recipeIndex) {
        currentRecipeIndex = recipeIndex;
        resetRecipeAndSlots();
    }

    @Override public int getField(int id)             { return 0; }
    @Override public void setField(int id, int value) { }
    @Override public int getFieldCount()              { return 0; }
    @Override public void clear()                     { slots.clear(); }
}