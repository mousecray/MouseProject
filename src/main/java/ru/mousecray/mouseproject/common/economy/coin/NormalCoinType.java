package ru.mousecray.mouseproject.common.economy.coin;

import net.minecraft.item.Item;
import ru.mousecray.mouseproject.registry.MPItems;
import ru.mousecray.mouseproject.registry.constants.CoinNames;

import java.util.function.Supplier;

public enum NormalCoinType implements CoinType {
    BRONZE(() -> MPItems.BRONZE_COIN, CoinNames.BRONZE_NAME),
    SILVER(() -> MPItems.SILVER_COIN, CoinNames.SILVER_NAME),
    GOLD(() -> MPItems.GOLD_COIN, CoinNames.GOLD_NAME),
    DIAMOND(() -> MPItems.DIAMOND_COIN, CoinNames.DIAMOND_NAME),
    EMERALD(() -> MPItems.EMERALD_COIN, CoinNames.EMERALD_NAME),
    RUBY(() -> MPItems.RUBY_COIN, CoinNames.RUBY_NAME),
    AMETHYST(() -> MPItems.AMETHYST_COIN, CoinNames.AMETHYST_NAME);

    private final Supplier<Item> item;
    private final String         name;

    NormalCoinType(Supplier<Item> item, String name) {
        this.item = item;
        this.name = name;
    }

    @Override public Item getItem()             { return item.get(); }
    @Override public int getID()                { return ordinal(); }
    @Override public String getTranslationKey() { return name; }
}
