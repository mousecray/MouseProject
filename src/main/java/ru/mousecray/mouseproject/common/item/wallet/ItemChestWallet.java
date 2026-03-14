/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.item.wallet;

import mcp.MethodsReturnNonnullByDefault;
import ru.mousecray.mouseproject.common.economy.capacity.NormalWalletCapacity;
import ru.mousecray.mouseproject.common.economy.capacity.ResourceWalletCapacity;
import ru.mousecray.mouseproject.common.economy.capacity.SpecificWalletCapacity;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemChestWallet extends ItemWallet {
    public ItemChestWallet(WalletType type, WalletType leakType) {
        super(type, leakType, null,
                NormalWalletCapacity.createInfinite(),
                SpecificWalletCapacity.createInfinite(),
                ResourceWalletCapacity.createInfinite());
    }

//    @Override
//    public CoinValue<?> putCoin(World world, EntityLiving entity, ItemStack wallet, ItemStack coin.json) {
//        super.putCoin(world, entity, wallet, coin.json);
//
//        //TODO: Кошелёк будет отправлять монеты в специальную копилку, к которой привязан.
//        // При его открытии он будет считать монеты из этой копилки
//    }
}