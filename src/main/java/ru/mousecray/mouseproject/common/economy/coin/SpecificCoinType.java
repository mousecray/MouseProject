/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.economy.coin;

import net.minecraft.item.Item;
import ru.mousecray.mouseproject.registry.MPItems;
import ru.mousecray.mouseproject.registry.constants.CoinNames;

import java.util.function.Supplier;

public enum SpecificCoinType implements CoinType {
    MYTHIC(() -> MPItems.MYTHIC_COIN, CoinNames.MYTHIC_NAME),
    ;

    private final Supplier<Item> item;
    private final String         name;

    SpecificCoinType(Supplier<Item> item, String name) {
        this.item = item;
        this.name = name;
    }

    @Override public Item getItem()             { return item.get(); }
    @Override public int getID()                { return ordinal() + NormalCoinType.values().length; }
    @Override public String getTranslationKey() { return name; }
}