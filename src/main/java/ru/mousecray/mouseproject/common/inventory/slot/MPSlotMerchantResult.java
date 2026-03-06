package ru.mousecray.mouseproject.common.inventory.slot;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.village.MerchantRecipe;
import ru.mousecray.mouseproject.common.inventory.MPInventoryMerchant;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MPSlotMerchantResult extends Slot {
    private final MPInventoryMerchant merchantInv;
    private final EntityPlayer        player;
    private       int                 removeCount;
    private final IMerchant           merchant;

    public MPSlotMerchantResult(EntityPlayer player, IMerchant merchant, MPInventoryMerchant merchantInv, int slotIndex, int xPosition, int yPosition) {
        super(merchantInv, slotIndex, xPosition, yPosition);
        this.player = player;
        this.merchant = merchant;
        this.merchantInv = merchantInv;
    }

    @Override public boolean isItemValid(ItemStack stack) { return false; }

    @Override
    public ItemStack decrStackSize(int amount) {
        if (getHasStack()) removeCount += Math.min(amount, getStack().getCount());
        return super.decrStackSize(amount);
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        removeCount += amount;
        onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack) {
        stack.onCrafting(player.world, player, removeCount);
        removeCount = 0;
    }

    @Override public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        onCrafting(stack);
        MerchantRecipe merchantrecipe = merchantInv.getCurrentRecipe();

        ItemStack itemstack  = merchantInv.getStackInSlot(0);
        ItemStack itemstack1 = merchantInv.getStackInSlot(1);

//        if (doTrade(merchantrecipe, itemstack, itemstack1)
//                || doTrade(merchantrecipe, itemstack1, itemstack)) {
        {
            merchant.useRecipe(merchantrecipe);
            thePlayer.addStat(StatList.TRADED_WITH_VILLAGER);
            merchantInv.setInventorySlotContents(0, itemstack);
            merchantInv.setInventorySlotContents(1, itemstack1);
        }

        return stack;
    }

    private boolean doTrade(MerchantRecipe trade, ItemStack customItem) {
        ItemStack itemstack  = trade.getItemToBuy();
        ItemStack itemstack1 = trade.getSecondItemToBuy();

        if (customItem.getItem() == itemstack.getItem() && customItem.getCount() >= itemstack.getCount()) {
//            if (!itemstack1.isEmpty() && itemstack1.getItem() == customItem.getItem() && secondItem.getCount() >= itemstack1.getCount()) {
//                customItem.shrink(itemstack.getCount());
//                secondItem.shrink(itemstack1.getCount());
//                return true;
//            }

//            if (itemstack1.isEmpty() && secondItem.isEmpty()) {
//                customItem.shrink(itemstack.getCount());
//                return true;
//            }
        }

        return false;
    }
}