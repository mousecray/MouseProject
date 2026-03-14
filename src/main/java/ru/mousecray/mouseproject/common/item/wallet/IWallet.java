/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.item.wallet;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IWallet {
    WalletType getWalletType();
    int getWalletID();
    @Nullable WalletType getLeakType();
    @Nullable WalletType getNormalType();

    @Nonnull CoinValue getCapacity(CoinType type);

    void randomTick(World world, EntityLiving entity, ItemStack wallet);

    @Nullable CoinValue putCoin(World world, EntityLivingBase entity, ItemStack wallet, ItemStack coin);
    @Nullable CoinValue takeCoin(World world, EntityLivingBase entity, ItemStack wallet, CoinValue coinValue);
    @Nullable CoinValue getCoin(World world, EntityLivingBase entity, ItemStack wallet, @Nullable CoinType type);

    @Nonnull List<CoinType> getCurrentCoins(World world, EntityLivingBase entity, ItemStack wallet);

    void onEquip(World world, EntityLivingBase entity, ItemStack wallet);
    void onUnequip(World world, EntityLivingBase entity, ItemStack wallet);
}