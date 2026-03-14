/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.item.wallet;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.capacity.WalletCapacity;
import ru.mousecray.mouseproject.common.economy.wallet.LeakWalletController;
import ru.mousecray.mouseproject.common.economy.wallet.MagicWalletController;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;
import ru.mousecray.mouseproject.common.item.coin.ICoin;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemLeakMagicWallet extends ItemWallet {
    public ItemLeakMagicWallet(WalletType type, WalletType normalType, WalletCapacity<?>... capacities) {
        super(type, null, normalType, capacities);
    }

    @Override
    public void randomTick(World world, EntityLiving entity, ItemStack wallet) {
        super.randomTick(world, entity, wallet);

        if (!world.isRemote) {
            Random rand = entity.getRNG();
            Item   item = wallet.getItem();
            if (item instanceof IWallet) LeakWalletController.fireDrop(world, entity, wallet, rand, ((IWallet) item));
        }
    }

    @Nullable @Override
    public CoinValue putCoin(World world, EntityLivingBase entity, ItemStack wallet, ItemStack coin) {
        CoinValue coinValue = super.putCoin(world, entity, wallet, coin);
        if (coinValue != null && ((coinValue.isNull() && coin.getCount() > 0) || coinValue.getValue() < coin.getCount())) {
            if (!world.isRemote) {
                if (!(coin.getItem() instanceof ICoin) || wallet.getItem() != this) return null;

                if (MouseProjectNBT.get(coin).getCoinPipe().loadIsNew()) {
                    MagicWalletController.onPutCoin(world, entity);
                }
            }
        }
        return coinValue;
    }
}