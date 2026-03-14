/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.inventory.slot.WalletSlot;
import ru.mousecray.mouseproject.network.MPPacketFactory;
import ru.mousecray.mouseproject.registry.MPPackets;

@Mixin(GuiContainerCreative.class)
public class MixinWalletGuiContainerCreative {
    @Shadow
    public boolean clearSearch;

    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode" })
    @Inject(method = "setCurrentCreativeTab", at = @At("TAIL"))
    private void adjustWalletSlotPosition(CreativeTabs tab, CallbackInfo ci) {
        GuiContainerCreative gui = (GuiContainerCreative) (Object) this;
        if (tab == CreativeTabs.INVENTORY) {
            for (Slot slot : gui.inventorySlots.inventorySlots) {
                if (slot instanceof GuiContainerCreative.CreativeSlot && ((GuiContainerCreative.CreativeSlot) slot).slot instanceof WalletSlot) {
                    slot.xPos = 173;
                    slot.yPos = 90;
                }
            }
        }
    }

    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode" })
    @Inject(method = "handleMouseClick", at = @At("HEAD"), cancellable = true)
    private void handleWalletSlotClick(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        GuiContainerCreative gui = (GuiContainerCreative) (Object) this;
        if (slot instanceof GuiContainerCreative.CreativeSlot && ((GuiContainerCreative.CreativeSlot) slot).slot instanceof WalletSlot) {
            clearSearch = true;
            EntityPlayer       player           = gui.mc.player;
            ItemStack          cursorStack      = player.inventory.getItemStack();
            ItemStack          stack            = slot.getStack();
            int                serverSlotNumber = ((GuiContainerCreative.CreativeSlot) slot).slot.slotNumber;
            PlayerControllerMP playerController = gui.mc.playerController;
            ItemStack          newStack         = ItemStack.EMPTY;

            if (type == ClickType.PICKUP) {
                if (cursorStack.isEmpty() && !stack.isEmpty()) {
                    newStack = ItemStack.EMPTY;
                    player.inventory.setItemStack(stack.copy());
                    slot.putStack(newStack);
                    playerController.sendSlotPacket(player.inventory.getItemStack(), serverSlotNumber);
                } else if (!cursorStack.isEmpty() && slot.isItemValid(cursorStack)) {
                    newStack = cursorStack.copy();
                    player.inventory.setItemStack(stack.copy());
                    slot.putStack(newStack);
                    playerController.sendSlotPacket(newStack, serverSlotNumber);
                }
            } else if (type == ClickType.QUICK_MOVE && !stack.isEmpty()) {
                newStack = ItemStack.EMPTY;
                playerController.sendSlotPacket(newStack, serverSlotNumber);
                player.inventory.addItemStackToInventory(stack.copy());
                slot.putStack(newStack);
            } else if (type == ClickType.THROW && slot.getHasStack()) {
                newStack = ItemStack.EMPTY;
                ItemStack thrown = stack.copy();
                thrown.setCount(mouseButton == 0 ? 1 : stack.getMaxStackSize());
                player.dropItem(thrown, true);
                gui.mc.playerController.sendSlotPacket(newStack, serverSlotNumber);
                slot.putStack(newStack);
            } else if (type == ClickType.SWAP && !stack.isEmpty() && mouseButton >= 0 && mouseButton < 9) {
                ItemStack hotbarStack = player.inventory.getStackInSlot(mouseButton);
                if (slot.isItemValid(hotbarStack)) {
                    newStack = hotbarStack.copy();
                    slot.putStack(newStack);
                    player.inventory.setInventorySlotContents(mouseButton, stack.copy());
                    gui.mc.playerController.sendSlotPacket(newStack, serverSlotNumber);
                }
            } else if (type == ClickType.CLONE && cursorStack.isEmpty() && !stack.isEmpty()) {
                newStack = stack.copy();
                newStack.setCount(newStack.getMaxStackSize());
                player.inventory.setItemStack(newStack);
                gui.mc.playerController.sendSlotPacket(newStack, serverSlotNumber);
            }

            MPPackets.INSTANCE.sendToServer(MPPacketFactory.WALLET_SYNC(newStack));
            gui.mc.player.inventoryContainer.detectAndSendChanges();
            ci.cancel();
        }
    }

    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode", "LocalMayBeArgsOnly" })
    @Inject(method = "drawGuiContainerBackgroundLayer(FII)V", at = @At("TAIL"))
    private void drawCustomSlotBackground(float partialTicks, int mouseX, int mouseY, CallbackInfo ci, @Local CreativeTabs creativetabs) {
        GuiContainerCreative gui = (GuiContainerCreative) (Object) this;
        if (creativetabs == CreativeTabs.INVENTORY) {
            for (Slot slot : gui.inventorySlots.inventorySlots) {
                if (slot instanceof GuiContainerCreative.CreativeSlot && ((GuiContainerCreative.CreativeSlot) slot).slot instanceof WalletSlot) {
                    int slotX = gui.getGuiLeft() + slot.xPos - 1;
                    int slotY = gui.getGuiTop() + slot.yPos - 1;

                    //Рендер фона слота
                    gui.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/container/slot/wallet_slot_background.png"));
                    Gui.drawModalRectWithCustomSizedTexture(slotX, slotY, 0, 0, 18, 18, 18, 18); //Фон слота 18x18

                    if (!slot.getHasStack()) {
                        //Рендер пустого слота
                        gui.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/container/slot/wallet_slot_empty.png"));
                        Gui.drawModalRectWithCustomSizedTexture(slotX, slotY, 0, 0, 18, 18, 18, 18); //Фон слота 16x16
                    }
                }
            }
        }
    }
}