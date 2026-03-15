/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.values;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TradeTable {
    private static final String key = "key", multiplayer = "multiplayer";

    private final Int2FloatMap multiplayerMap;

    public TradeTable() {
        multiplayerMap = new Int2FloatOpenHashMap();
        multiplayerMap.defaultReturnValue(0);
    }

    public void resetMultiplayer(int id) {
        multiplayerMap.put(id, 0);
    }

    public void incMultiplayer(int id) {
        multiplayerMap.put(id, multiplayerMap.get(id));
    }

    public float getMultiplayer(int id) {
        return multiplayerMap.get(id);
    }

    public NBTTagList toNBTTagList() {
        NBTTagList nbtList = new NBTTagList();
        for (Int2FloatMap.Entry entry : multiplayerMap.int2FloatEntrySet()) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setInteger(key, entry.getIntKey());
            entryTag.setFloat(multiplayer, entry.getFloatValue());
            nbtList.appendTag(entryTag);
        }
        return nbtList;
    }

    public NBTTagCompound valToNBT(int id) {
        NBTTagCompound entryTag = new NBTTagCompound();
        entryTag.setInteger(key, id);
        entryTag.setFloat(multiplayer, multiplayerMap.get(id));
        return entryTag;
    }

    public void valFromNbt(NBTTagList list, int id) {
        for (NBTBase tag : list) {
            if (tag instanceof NBTTagCompound) {
                NBTTagCompound nbt = (NBTTagCompound) tag;
                if (nbt.hasKey(key, 3) && nbt.hasKey(multiplayer, 5)) {
                    if (nbt.getInteger(key) == id) {
                        multiplayerMap.put(id, nbt.getFloat(multiplayer));
                    }
                }
            }
        }
    }

    public void fromNBTTagList(NBTTagList list) {
        multiplayerMap.clear();
        for (NBTBase tag : list) {
            if (tag instanceof NBTTagCompound) {
                NBTTagCompound entryTag = (NBTTagCompound) tag;
                if (entryTag.hasKey(key, 3) && entryTag.hasKey(multiplayer, 5)) {
                    multiplayerMap.put(entryTag.getInteger(key), entryTag.getFloat(multiplayer));
                }
            }
        }
    }
}