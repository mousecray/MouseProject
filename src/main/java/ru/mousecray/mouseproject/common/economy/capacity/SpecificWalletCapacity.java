package ru.mousecray.mouseproject.common.economy.capacity;

import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;

public class SpecificWalletCapacity implements WalletCapacity<SpecificCoinType> {
    private final long mythicCapacity;

    public SpecificWalletCapacity(long mythicCapacity)              { this.mythicCapacity = mythicCapacity; }
    public static SpecificWalletCapacity createInfinite()           { return new SpecificWalletCapacity(Long.MAX_VALUE); }
    public static SpecificWalletCapacity create(int mythicCapacity) { return new SpecificWalletCapacity(mythicCapacity); }

    @SuppressWarnings("SwitchStatementWithTooFewBranches") @Override
    public long getCapacity(CoinType type) {
        switch (((SpecificCoinType) type)) {
            case MYTHIC:
                return mythicCapacity;
            default:
                return 0;
        }
    }

    @Override public Class<SpecificCoinType> getSupportedCoins() { return SpecificCoinType.class; }
    @Override public boolean isInfinite(CoinType type)           { return getCapacity(type) == Long.MAX_VALUE; }
}