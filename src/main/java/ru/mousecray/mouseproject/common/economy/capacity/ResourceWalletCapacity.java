/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.economy.capacity;

import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.ResourceCoinType;

public class ResourceWalletCapacity implements WalletCapacity<ResourceCoinType> {
    private final long woolCapacity, woodCapacity, coalCapacity, stoneCapacity,
            lapisCapacity, redstoneCapacity, obsidianCapacity, netheriteCapacity;

    public ResourceWalletCapacity(
            long woolCapacity, long woodCapacity, long coalCapacity, long stoneCapacity,
            long lapisCapacity, long redstoneCapacity, long obsidianCapacity, long netheriteCapacity
    ) {
        this.woolCapacity = woolCapacity;
        this.woodCapacity = woodCapacity;
        this.coalCapacity = coalCapacity;
        this.stoneCapacity = stoneCapacity;
        this.lapisCapacity = lapisCapacity;
        this.redstoneCapacity = redstoneCapacity;
        this.obsidianCapacity = obsidianCapacity;
        this.netheriteCapacity = netheriteCapacity;
    }

    public static ResourceWalletCapacity createInfinite() {
        return new ResourceWalletCapacity(
                Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE
        );
    }

    public static ResourceWalletCapacity createSingle(int singleCapacity) {
        return new ResourceWalletCapacity(
                singleCapacity, singleCapacity, singleCapacity,
                singleCapacity, singleCapacity, singleCapacity,
                singleCapacity, singleCapacity
        );
    }

    public static ResourceWalletCapacity create(
            int woolCapacity, int woodCapacity, int coalCapacity, int stoneCapacity,
            int lapisCapacity, int redstoneCapacity, int obsidianCapacity, int netheriteCapacity
    ) {
        return new ResourceWalletCapacity(
                woolCapacity, woodCapacity, coalCapacity, stoneCapacity,
                lapisCapacity, redstoneCapacity, obsidianCapacity, netheriteCapacity
        );
    }

    @Override
    public long getCapacity(CoinType type) {
        switch (((ResourceCoinType) type)) {
            case WOOL:
                return woolCapacity;
            case WOOD:
                return woodCapacity;
            case COAL:
                return coalCapacity;
            case STONE:
                return stoneCapacity;
            case LAPIS:
                return lapisCapacity;
            case REDSTONE:
                return redstoneCapacity;
            case OBSIDIAN:
                return obsidianCapacity;
            case NETHERITE:
                return netheriteCapacity;
            default:
                return 0;
        }
    }

    @Override public Class<ResourceCoinType> getSupportedCoins() { return ResourceCoinType.class; }
    @Override public boolean isInfinite(CoinType type)           { return getCapacity(type) == Long.MAX_VALUE; }
}