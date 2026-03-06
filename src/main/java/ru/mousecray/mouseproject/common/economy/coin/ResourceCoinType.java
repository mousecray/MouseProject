package ru.mousecray.mouseproject.common.economy.coin;

import net.minecraft.item.Item;
import ru.mousecray.mouseproject.registry.MPItems;
import ru.mousecray.mouseproject.registry.constants.CoinNames;

import java.util.function.Supplier;

public enum ResourceCoinType implements CoinType {
    WOOL(() -> MPItems.WOOL_COIN, CoinNames.WOOL_NAME),
    WOOD(() -> MPItems.WOOD_COIN, CoinNames.WOOD_NAME),
    STONE(() -> MPItems.STONE_COIN, CoinNames.STONE_NAME),
    COAL(() -> MPItems.COAL_COIN, CoinNames.COAL_NAME),
    LAPIS(() -> MPItems.LAPIS_COIN, CoinNames.LAPIS_NAME),
    REDSTONE(() -> MPItems.REDSTONE_COIN, CoinNames.REDSTONE_NAME),
    OBSIDIAN(() -> MPItems.OBSIDIAN_COIN, CoinNames.OBSIDIAN_NAME),
    NETHERITE(() -> MPItems.NETHERITE_COIN, CoinNames.NETHERITE_NAME),
    ;

    private final Supplier<Item> item;
    private final String         name;

    ResourceCoinType(Supplier<Item> item, String name) {
        this.item = item;
        this.name = name;
    }

    @Override public Item getItem() { return item.get(); }

    @Override
    public int getID() {
        return ordinal() + SpecificCoinType.values().length + NormalCoinType.values().length;
    }

    @Override public String getTranslationKey() { return name; }
}