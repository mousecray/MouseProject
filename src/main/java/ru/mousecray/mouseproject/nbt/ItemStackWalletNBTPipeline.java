/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.nbt;

import net.minecraft.nbt.NBTTagCompound;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemStackWalletNBTPipeline {
    static final  String                                   TAG_BRONZE_BALANCE_PATTERN   = "BronzeBalance_";
    static final  String                                   TAG_BRONZE_BALANCE_KEY       = TAG_BRONZE_BALANCE_PATTERN + 0;
    static final  String                                   TAG_SPECIFIC_BALANCE_PATTERN = "SpecificBalance_";
    static final  Function<Integer, String>                TAG_SPECIFIC_BALANCE_KEY     = i -> TAG_SPECIFIC_BALANCE_PATTERN + i;
    static final  String                                   TAG_RESOURCE_BALANCE_PATTERN = "ResourceBalance_";
    static final  Function<Integer, String>                TAG_RESOURCE_BALANCE_KEY     = i -> TAG_RESOURCE_BALANCE_PATTERN + i;
    static final  String                                   TAG_OTHER_BALANCE_PATTERN    = "OtherBalance_";
    static final  Function<Integer, String>                TAG_OTHER_BALANCE_KEY        = i -> TAG_OTHER_BALANCE_PATTERN + i;
    private final MouseProjectNBT.MouseProjectNBTItemStack container;
    private ItemStackWalletNBTPipeline(MouseProjectNBT.MouseProjectNBTItemStack container) { this.container = container; }
    static ItemStackWalletNBTPipeline get(MouseProjectNBT.MouseProjectNBTItemStack base)   { return new ItemStackWalletNBTPipeline(base); }

    public void saveBronzeBalance(CoinValue newBalance) {
        container.getModTag().setTag(TAG_BRONZE_BALANCE_KEY, newBalance.toNBT());
    }

    @Nullable
    public CoinValue loadBronzeBalance() {
        if (container.getModTag().hasKey(TAG_BRONZE_BALANCE_KEY)) {
            NBTTagCompound tag = container.getModTag().getCompoundTag(TAG_BRONZE_BALANCE_KEY);
            return CoinValue.fromNBT(tag);
        }
        return null;
    }

    public void saveSpecificBalance(CoinValue newBalance) {
        container.getModTag().setTag(TAG_SPECIFIC_BALANCE_KEY.apply(newBalance.getType().getID()), newBalance.toNBT());
    }

    @Nullable
    public CoinValue loadSpecificBalance(CoinType type) {
        String key = TAG_SPECIFIC_BALANCE_KEY.apply(type.getID());
        if (container.getModTag().hasKey(key)) {
            NBTTagCompound tag = container.getModTag().getCompoundTag(key);
            return CoinValue.fromNBT(tag);
        }
        return null;
    }

    public void saveResourceBalance(CoinValue newBalance) {
        container.getModTag().setTag(TAG_RESOURCE_BALANCE_KEY.apply(newBalance.getType().getID()), newBalance.toNBT());
    }

    @Nullable
    public CoinValue loadResourceBalance(CoinType type) {
        String key = TAG_RESOURCE_BALANCE_KEY.apply(type.getID());
        if (container.getModTag().hasKey(key)) {
            NBTTagCompound tag = container.getModTag().getCompoundTag(key);
            return CoinValue.fromNBT(tag);
        }
        return null;
    }

    public void saveOtherBalance(CoinValue newBalance) {
        container.getModTag().setTag(TAG_OTHER_BALANCE_KEY.apply(newBalance.getType().getID()), newBalance.toNBT());
    }

    @Nullable
    public CoinValue loadOtherBalance(CoinType type) {
        String key = TAG_OTHER_BALANCE_KEY.apply(type.getID());
        if (container.getModTag().hasKey(key)) {
            NBTTagCompound tag = container.getModTag().getCompoundTag(key);
            return CoinValue.fromNBT(tag);
        }
        return null;
    }

    @Nonnull
    public List<CoinType> loadAllBalanceTypes() {
        List<CoinType> result = new ArrayList<>();
        for (String s : container.getModTag().getKeySet()) {
            String key = s.substring(0, s.lastIndexOf("_") + 1);
            switch (key) {
                case TAG_BRONZE_BALANCE_PATTERN:
                case TAG_SPECIFIC_BALANCE_PATTERN:
                case TAG_RESOURCE_BALANCE_PATTERN:
                case TAG_OTHER_BALANCE_PATTERN:
                    NBTTagCompound tag = container.getModTag().getCompoundTag(s);
                    CoinValue coinValue = CoinValue.fromNBT(tag);
                    if (coinValue != null) result.add(coinValue.getType());
            }
        }
        return result;
    }
}