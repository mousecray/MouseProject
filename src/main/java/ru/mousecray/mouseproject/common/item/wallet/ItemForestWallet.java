package ru.mousecray.mouseproject.common.item.wallet;

import mcp.MethodsReturnNonnullByDefault;
import ru.mousecray.mouseproject.common.economy.capacity.WalletCapacity;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemForestWallet extends ItemWallet {
    public ItemForestWallet(WalletType type, WalletType leakType, WalletCapacity<?>... capacities) {
        super(type, leakType, null, capacities);
    }

//    @Override
//    public CoinValue<?> putCoin(World world, EntityLiving entity, ItemStack wallet, ItemStack coin.json) {
//        super.putCoin(world, entity, wallet, coin.json);
//        //TODO: При добавлении монеты кошелёк будет добавлять максимальное здоровье. Максимальное здоровье - 150%
//    }
//
//    @Override
//    public CoinValue<?> takeCoin(World world, EntityLiving entity, ItemStack wallet, CoinValue coinValue) {
//        super.takeCoin(world, entity, wallet, coinValue);
//        //TODO: При удалении монеты кошелёк будет уменьшать здоровье. Минимальное здоровье 100%
//    }
//
//    @Override
//    public void onEquip(World world, EntityLiving entity, ItemStack wallet) {
//        super.onEquip(world, entity, wallet);
//        //TODO: При экипировке устанавливает задоровье относительно баланса. Максимально 150%
//    }
//
//    @Override
//    public void onUnequip(World world, EntityLiving entity, ItemStack wallet) {
//        super.onUnequip(world, entity, wallet);
//        //TODO: При экипировке устанавливает задоровье здоровье по умолчанию.
//        // По идее, нужно хранить здоровье, которое было до модификации
//    }
}