/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface ICapabilityInventoryProvider<T extends ICapabilityInventory<T>> extends ICapabilitySerializable<NBTTagCompound> {
    ResourceLocation getName();
    <D extends Capability<T>> D getCapabilityInventory();
    ICapabilityInventoryProvider<T> createNew();
}