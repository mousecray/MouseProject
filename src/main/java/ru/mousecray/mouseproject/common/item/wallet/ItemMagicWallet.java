package ru.mousecray.mouseproject.common.item.wallet;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.capacity.WalletCapacity;
import ru.mousecray.mouseproject.common.economy.wallet.MagicWalletController;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;
import ru.mousecray.mouseproject.common.item.coin.ICoin;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemMagicWallet extends ItemWallet {
    public ItemMagicWallet(WalletType type, WalletType leakType, WalletCapacity<?>... capacities) {
        super(type, leakType, null, capacities);
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