package ru.mousecray.mouseproject.common.economy.wallet;

import net.minecraft.item.Item;
import ru.mousecray.mouseproject.registry.MPItems;
import ru.mousecray.mouseproject.registry.constants.WalletNames;

import java.util.function.Supplier;

public enum DefaultWalletType implements WalletType {
    SMALL(() -> MPItems.SMALL_WALLET, WalletNames.SMALL_NAME, false, true),
    LEATHER(() -> MPItems.LEATHER_WALLET, WalletNames.LEATHER_NAME, false, true),
    FAT(() -> MPItems.FAT_WALLET, WalletNames.FAT_NAME, false, true),
    BIG(() -> MPItems.BIG_WALLET, WalletNames.BIG_NAME, false, true),
    HUGE(() -> MPItems.HUGE_WALLET, WalletNames.HUGE_NAME, false, true),

    MAGIC(() -> MPItems.MAGIC_WALLET, WalletNames.MAGIC_NAME, false, true),
    CHEST(() -> MPItems.CHEST_WALLET, WalletNames.CHEST_NAME, false, true),
    FOREST(() -> MPItems.FOREST_WALLET, WalletNames.FOREST_NAME, false, true),

    LEAK_SMALL(() -> MPItems.LEAK_SMALL_WALLET, WalletNames.LEAK_SMALL_NAME, true, false),
    LEAK_LEATHER(() -> MPItems.LEAK_LEATHER_WALLET, WalletNames.LEAK_LEATHER_NAME, true, false),
    LEAK_FAT(() -> MPItems.LEAK_FAT_WALLET, WalletNames.LEAK_FAT_NAME, true, false),
    LEAK_BIG(() -> MPItems.LEAK_BIG_WALLET, WalletNames.LEAK_BIG_NAME, true, false),
    LEAK_HUGE(() -> MPItems.LEAK_HUGE_WALLET, WalletNames.LEAK_HUGE_NAME, true, false),
    LEAK_MAGIC(() -> MPItems.LEAK_MAGIC_WALLET, WalletNames.LEAK_MAGIC_NAME, true, false),
    LEAK_CHEST(() -> MPItems.LEAK_CHEST_WALLET, WalletNames.LEAK_CHEST_NAME, true, false),
    LEAK_FOREST(() -> MPItems.LEAK_FOREST_WALLET, WalletNames.LEAK_FOREST_NAME, true, false),

    EPIC(() -> MPItems.EPIC_WALLET, WalletNames.EPIC_NAME),
    LEGENDARY(() -> MPItems.LEGENDARY_WALLET, WalletNames.LEGENDARY_NAME),

    INFINITY(() -> MPItems.INFINITY_WALLET, WalletNames.INFINITY_NAME),
    ;

    private final Supplier<Item> item;
    private final String         name;
    private final boolean        leaked;
    private final boolean        canTransformToLeak;

    DefaultWalletType(Supplier<Item> item, String name, boolean leaked, boolean canTransformToLeak) {
        this.item = item;
        this.name = name;
        this.leaked = leaked;
        this.canTransformToLeak = canTransformToLeak;
    }

    DefaultWalletType(Supplier<Item> item, String name) {
        this(item, name, false, false);
    }

    @Override public Item getItem()               { return item.get(); }
    @Override public int getID()                  { return ordinal(); }
    @Override public String getTranslationKey()   { return name; }
    @Override public boolean isLeaked()           { return leaked; }
    @Override public boolean canTransformToLeak() { return canTransformToLeak; }
}
