package ru.mousecray.mouseproject.common.economy.wallet;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.economy.CoinHelper;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;
import ru.mousecray.mouseproject.common.economy.coin.ResourceCoinType;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;
import ru.mousecray.mouseproject.common.item.wallet.IWallet;
import ru.mousecray.mouseproject.registry.MPDamageSources;
import ru.mousecray.mouseproject.utils.MPRandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LeakWalletController {
    public static void fireDrop(World world, EntityLiving entity, ItemStack wallet, Random rand, IWallet provider) {
        if (MPRandomUtils.normalChance(rand, 1)) {
            List<CoinType>  coins     = provider.getCurrentCoins(world, entity, wallet);
            List<CoinValue> normal    = new ArrayList<>();
            List<CoinValue> specific  = new ArrayList<>();
            List<CoinValue> resource  = new ArrayList<>();
            List<CoinValue> other     = new ArrayList<>();
            int             typesDrop = rand.nextInt(3) + 1;
            for (int i = 0; i < typesDrop; ++i) {
                CoinType type = coins.get(rand.nextInt(coins.size()));
                if (type != null) {
                    CoinValue single    = CoinValue.createSingle(type);
                    CoinValue coinValue = provider.takeCoin(world, entity, wallet, single);
                    if (coinValue != null && coinValue.isPositive()) {
                        if (type instanceof NormalCoinType) normal.add(coinValue);
                        else if (type instanceof SpecificCoinType) specific.add(coinValue);
                        else if (type instanceof ResourceCoinType) resource.add(coinValue);
                        else other.add(coinValue);
                    }
                }
            }
            CoinHelper.dropInWorld(
                    world, MPDamageSources.ON_DROPPED_COIN, entity, rand,
                    normal, specific, resource, other,
                    true, true
            );
        }
    }
}