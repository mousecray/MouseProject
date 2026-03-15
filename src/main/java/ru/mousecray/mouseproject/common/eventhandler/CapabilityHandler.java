/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.eventhandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import ru.mousecray.mouseproject.common.capability.ICapabilityInventory;
import ru.mousecray.mouseproject.common.capability.impl.CapabilityWalletInventory;
import ru.mousecray.mouseproject.network.MPPacketFactory;
import ru.mousecray.mouseproject.registry.MPCapabilities;
import ru.mousecray.mouseproject.registry.MPPackets;

public class CapabilityHandler {
    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            MPCapabilities.INSTANCE.getInvsToSave().forEach(provider -> event.addCapability(provider.getName(), provider.createNew()));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        MPCapabilities.INSTANCE.getInvsToSave().forEach(provider -> {
            ICapabilityInventory newCap = player.getCapability(provider.getCapabilityInventory(), null);
            ICapabilityInventory oldCap = event.getOriginal().getCapability(provider.getCapabilityInventory(), null);
            if (newCap != null) newCap.copyInventory(oldCap);
        });
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;

        if (event.player instanceof EntityPlayerMP) {
            syncWalletInventory((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onGameModeChange(PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) return;

        if (event.player instanceof EntityPlayerMP) {
            syncWalletInventory((EntityPlayerMP) event.player);
        }
    }

    private void syncWalletInventory(EntityPlayerMP player) {
        ICapabilityInventory<CapabilityWalletInventory> walletCap = player.getCapability(CapabilityWalletInventory.InventoryProvider.WALLET_INVENTORY, null);
        if (walletCap != null) {
            ItemStack stack = walletCap.getInventory().getStackInSlot(0);
            MPPackets.INSTANCE.sendTo(MPPacketFactory.WALLET_SYNC(stack), player);
            player.inventoryContainer.detectAndSendChanges();
        }
    }
}