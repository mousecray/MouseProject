package ru.mousecray.mouseproject.common.capability.impl;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.capability.ICapabilityInventory;
import ru.mousecray.mouseproject.common.capability.ICapabilityInventoryProvider;
import ru.mousecray.mouseproject.common.inventory.WalletInventory;

import javax.annotation.Nonnull;

public class CapabilityWalletInventory implements ICapabilityInventory<CapabilityWalletInventory> {
    public final WalletInventory inventory = new WalletInventory();

    @Override public WalletInventory getInventory()                          { return inventory; }

    @Override public void copyInventory(CapabilityWalletInventory inventory) { this.inventory.copy(inventory.getInventory()); }

    public static class InventoryProvider implements ICapabilityInventoryProvider<CapabilityWalletInventory> {
        @CapabilityInject(ICapabilityInventory.class)
        public static       Capability<ICapabilityInventory<CapabilityWalletInventory>> WALLET_INVENTORY = null;
        public static final ResourceLocation                                            NAME             = new ResourceLocation(Tags.MOD_ID, "wallet_inventory");
        private final       ICapabilityInventory<CapabilityWalletInventory>             instance         = new CapabilityWalletInventory();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
            return capability == WALLET_INVENTORY;
        }

        @SuppressWarnings("unchecked") @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
            return capability == WALLET_INVENTORY ? (T) instance : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return (NBTTagCompound) WALLET_INVENTORY.getStorage().writeNBT(WALLET_INVENTORY, instance, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            WALLET_INVENTORY.getStorage().readNBT(WALLET_INVENTORY, instance, null, nbt);
        }

        @Override public ResourceLocation getName()                                          { return NAME; }
        @Override public ICapabilityInventoryProvider<CapabilityWalletInventory> createNew() { return new InventoryProvider(); }

        @SuppressWarnings("unchecked") @Override
        public Capability<ICapabilityInventory<CapabilityWalletInventory>> getCapabilityInventory() {
            return WALLET_INVENTORY;
        }
    }
}