package ru.mousecray.mouseproject.common.economy.wallet;

import net.minecraft.item.Item;

import javax.annotation.Nullable;

public interface WalletType {
    Item getItem();
    int getID();
    String getTranslationKey();
    boolean isLeaked();
    boolean canTransformToLeak();

    @Nullable
    static WalletType fromID(int id) {
        switch (id) {
            case 0:
                return DefaultWalletType.SMALL;
            case 1:
                return DefaultWalletType.LEATHER;
            case 2:
                return DefaultWalletType.FAT;
            case 3:
                return DefaultWalletType.BIG;
            case 4:
                return DefaultWalletType.HUGE;
            case 5:
                return DefaultWalletType.MAGIC;
            case 6:
                return DefaultWalletType.CHEST;
            case 7:
                return DefaultWalletType.FOREST;
            case 8:
                return DefaultWalletType.LEAK_SMALL;
            case 9:
                return DefaultWalletType.LEAK_LEATHER;
            case 10:
                return DefaultWalletType.LEAK_FAT;
            case 11:
                return DefaultWalletType.LEAK_BIG;
            case 12:
                return DefaultWalletType.LEAK_HUGE;
            case 13:
                return DefaultWalletType.LEAK_CHEST;
            case 14:
                return DefaultWalletType.LEAK_MAGIC;
            case 15:
                return DefaultWalletType.LEAK_FOREST;
            case 16:
                return DefaultWalletType.EPIC;
            case 17:
                return DefaultWalletType.LEGENDARY;
            case 18:
                return DefaultWalletType.INFINITY;
            default:
                return null;
        }
    }
}