/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.economy.capacity;

import ru.mousecray.mouseproject.common.economy.CoinHelper;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;

public class NormalWalletCapacity implements WalletCapacity<NormalCoinType> {
    private final long bronzeCapacity;

    public NormalWalletCapacity(long bronzeCapacity) {
        this.bronzeCapacity = bronzeCapacity;
    }

    public static NormalWalletCapacity createInfinite() {
        return new NormalWalletCapacity(Long.MAX_VALUE);
    }

    public static NormalWalletCapacity create(long bronzeCapacity) {
        return new NormalWalletCapacity(bronzeCapacity);
    }

    @Override
    public long getCapacity(CoinType type) {
        return CoinHelper.fromBronzeToType((NormalCoinType) type, bronzeCapacity);
    }

    @Override public Class<NormalCoinType> getSupportedCoins() { return NormalCoinType.class; }
    @Override public boolean isInfinite(CoinType type)         { return getCapacity(type) == Long.MAX_VALUE; }
}