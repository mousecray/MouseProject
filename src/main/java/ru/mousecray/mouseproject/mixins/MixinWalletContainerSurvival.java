/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mousecray.mouseproject.common.capability.ICapabilityInventory;
import ru.mousecray.mouseproject.common.capability.impl.CapabilityWalletInventory;
import ru.mousecray.mouseproject.common.inventory.slot.WalletSlot;

import javax.annotation.Nonnull;

@Mixin(ContainerPlayer.class)
public abstract class MixinWalletContainerSurvival extends Container {
    @Override @Nonnull protected Slot addSlotToContainer(@Nonnull Slot slotIn) { return super.addSlotToContainer(slotIn); }

    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode" })
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player, CallbackInfo ci) {
        ContainerPlayer                                 container       = (ContainerPlayer) (Object) this;
        ICapabilityInventory<CapabilityWalletInventory> walletInventory = player.getCapability(CapabilityWalletInventory.InventoryProvider.WALLET_INVENTORY, null);
        if (walletInventory != null) {
            WalletSlot slot = new WalletSlot(walletInventory.getInventory(), 0, 152, 62);
            addSlotToContainer(slot);
            container.detectAndSendChanges();
        }
    }
}