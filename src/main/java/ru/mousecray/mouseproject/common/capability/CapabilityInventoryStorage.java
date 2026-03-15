/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class CapabilityInventoryStorage<T extends ICapabilityInventory<T>> implements Capability.IStorage<ICapabilityInventory<T>> {
    @Override
    public NBTBase writeNBT(Capability<ICapabilityInventory<T>> capability, ICapabilityInventory<T> instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        instance.getInventory().writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityInventory<T>> capability, ICapabilityInventory<T> instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) instance.getInventory().readFromNBT(((NBTTagCompound) nbt));
    }
}