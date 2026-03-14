/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.item.coin;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;

import java.util.Random;

public interface ICoin {
    void onPickup(World world, Entity entity, ItemStack stack, Random rand);
    void onDropWhenDeath(World world, Entity entity, ItemStack stack, Random rand);
    void onToss(World world, Entity entity, ItemStack stack, Random rand);

    CoinType getCoinType();
    int getCoinID();
}