/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.item.coin;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.item.MPDefaultItem;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.registry.MPSounds;
import ru.mousecray.mouseproject.registry.MPTriggers;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemCoin extends MPDefaultItem implements ICoin {
    private final CoinType type;

    public ItemCoin(CoinType type) {
        super(type.getTranslationKey());
        this.type = type;
    }

    @Override public CoinType getCoinType() { return type; }
    @Override public int getCoinID()        { return type.getID(); }

    @Override
    public void onPickup(World world, Entity entity, ItemStack stack, Random rand) {
        Item item = stack.getItem();
        if (item != this) return;

        MouseProjectNBT.MouseProjectNBTItemStack stackNBT = MouseProjectNBT.get(stack);
        stackNBT.getCoinPipe().removeIsNew();
        stackNBT.removeAllTagIfEmpty();

        world.playSound(null, entity.getPosition(), MPSounds.COIN_PICKUP, SoundCategory.PLAYERS, 1F, 1f + (rand.nextFloat() - 0.5f));

        if (world.isRemote) return;
        if (entity instanceof EntityPlayer) {
            MPTriggers.GET_COIN.trigger(
                    ((EntityPlayerMP) entity),
                    CoinValue.create(stack.getCount(), ((ICoin) item).getCoinType())
            );
        }
    }

    @Override
    public void onToss(World world, Entity entity, ItemStack stack, Random rand) {
        world.playSound(null, entity.getPosition(), MPSounds.COIN_DROP, SoundCategory.PLAYERS, 1F, 1f + (rand.nextFloat() - 0.5f));
    }

    @Override
    public void onDropWhenDeath(World world, Entity entity, ItemStack stack, Random rand) {
        MouseProjectNBT.get(stack).getCoinPipe().setIsNew();
    }
}