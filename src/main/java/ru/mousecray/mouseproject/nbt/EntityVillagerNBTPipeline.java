/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.nbt;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.common.values.DayValue;
import ru.mousecray.mouseproject.common.values.Poi;
import ru.mousecray.mouseproject.common.values.TradeTable;

import javax.annotation.Nullable;

public class EntityVillagerNBTPipeline {
    static final String TAG_POI_FOLLOW_KEY          = "PoiFollow";
    static final String TAG_POI_VISITED_PER_DAY_KEY = "PoiVisitedPerDay";
    static final String TAG_POI_KEY                 = "Poi";
    static final String TAG_DAY_KEY                 = "Day";
    static final String TAG_DAY_NUMBER_KEY          = "DayNumber";
    static final String TAG_TIME_KEY                = "Time";
    static final String TAG_TRADE_TABLE_KEY         = "TradeTable";
    static final String TAG_BLOCK_KEY               = "Block";
    static final String TAG_POS_KEY                 = "Pos";

    private final MouseProjectNBT.MouseProjectNBTEntity container;

    private EntityVillagerNBTPipeline(MouseProjectNBT.MouseProjectNBTEntity container) { this.container = container; }

    static EntityVillagerNBTPipeline get(MouseProjectNBT.MouseProjectNBTEntity base) {
        return new EntityVillagerNBTPipeline(base);
    }

    public boolean saveTradeTableVal(TradeTable table, int val) {
        if (container.getModTag().hasKey(TAG_TRADE_TABLE_KEY, 9)) {
            NBTTagList tag = container.getModTag().getTagList(TAG_TRADE_TABLE_KEY, 10);
            tag.set(val, table.valToNBT(val));
            return true;
        }
        return false;
    }

    public void loadTradeTableVal(TradeTable tradeTable, int val) {
        tradeTable.valFromNbt(container.getModTag().getTagList(TAG_TRADE_TABLE_KEY, 10), val);
    }

    public void saveTradeTable(TradeTable table) {
        container.getModTag().setTag(TAG_TRADE_TABLE_KEY, table.toNBTTagList());
    }

    public void loadTradeTable(TradeTable tradeTable) {
        tradeTable.fromNBTTagList(container.getModTag().getTagList(TAG_TRADE_TABLE_KEY, 10));
    }

    public void savePoiVisitedPerDay(int flag) {
        container.getModTag().setInteger(TAG_POI_VISITED_PER_DAY_KEY, flag);
    }

    /**
     * @return -1 don't contains tag
     */
    public int loadPoiVisitedPerDay() {
        return container.getModTag().hasKey(TAG_POI_VISITED_PER_DAY_KEY) ? container.getModTag().getInteger(TAG_POI_VISITED_PER_DAY_KEY) : -1;
    }

    public void savePoiFollow(boolean flag) {
        container.getModTag().setBoolean(TAG_POI_FOLLOW_KEY, flag);
    }

    public boolean loadPoiFollow() {
        return container.getModTag().hasKey(TAG_POI_FOLLOW_KEY) && container.getModTag().getBoolean(TAG_POI_FOLLOW_KEY);
    }

    public void saveDayValue(DayValue value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(TAG_DAY_NUMBER_KEY, value.getDay());
        tag.setInteger(TAG_TIME_KEY, value.getValue());
        container.getModTag().setTag(EntityVillagerNBTPipeline.TAG_DAY_KEY, tag);
    }

    public DayValue loadDayValue() {
        if (container.getModTag().hasKey(TAG_DAY_KEY)) {
            NBTTagCompound tag = container.getModTag().getCompoundTag(TAG_DAY_KEY);
            if (tag.hasKey(TAG_DAY_NUMBER_KEY) && tag.hasKey(TAG_TIME_KEY)) {
                return new DayValue(tag.getInteger(TAG_DAY_NUMBER_KEY), tag.getInteger(TAG_TIME_KEY));
            }
        }
        return null;
    }

    public boolean savePoi(Poi poi) {
        NBTTagCompound tag = new NBTTagCompound();
        if (!saveBlock(tag, poi.getBlock())) {
            MouseProject.LOGGER.error("POI block can't saving");
            return false;
        }
        savePos(tag, poi.getPos());
        container.getModTag().setTag(TAG_POI_KEY, tag);
        return true;
    }

    public Poi loadPoi() {
        if (container.getModTag().hasKey(TAG_POI_KEY)) {
            NBTTagCompound tag = container.getModTag().getCompoundTag(TAG_POI_KEY);
            Block          poi = loadBlock(tag);
            if (poi != null) {
                BlockPos pos = loadPos(tag);
                return pos != null ? new Poi(poi, pos) : null;
            }
        } else MouseProject.LOGGER.error("Attempt to load non-existent POI");
        return null;
    }

    private void savePos(NBTTagCompound nbt, BlockPos pos) {
        nbt.setLong(TAG_POS_KEY, pos.toLong());
    }

    @Nullable
    private BlockPos loadPos(NBTTagCompound nbt) {
        return nbt.hasKey(TAG_POS_KEY) ? BlockPos.fromLong(nbt.getLong(TAG_POS_KEY)) : null;
    }

    private boolean saveBlock(NBTTagCompound nbt, Block block) {
        ResourceLocation name = block.getRegistryName();
        if (name != null) {
            nbt.setString(TAG_BLOCK_KEY, name.toString());
            return true;
        }
        return false;
    }

    @Nullable
    private Block loadBlock(NBTTagCompound nbt) {
        String rl = nbt.getString(TAG_BLOCK_KEY);
        if (!rl.isEmpty()) return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(rl));
        return null;
    }
}