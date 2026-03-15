/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.eventhandler;

import net.minecraft.block.BlockCrops;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;
import ru.mousecray.mouseproject.common.event.CoinDropEvent;
import ru.mousecray.mouseproject.registry.MPPotions;
import ru.mousecray.mouseproject.registry.MPSounds;

import java.util.List;

public class PotionEffectHandler {
    @SubscribeEvent
    public void onPlayerCrafting(PlayerEvent.ItemCraftedEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayer player = event.player;

        PotionEffect hasDouble = player.getActivePotionEffect(MPPotions.DOUBLE_CRAFT);
        if (hasDouble != null) {
            int count = hasDouble.getAmplifier() + 1;
            for (int i = 0; i < count; ++i) player.addItemStackToInventory(event.crafting.copy());
            player.removePotionEffect(MPPotions.DOUBLE_CRAFT);
            player.world.playSound(null, player.getPosition(), MPSounds.WALLET_EFFECT_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public void onPlayerFishing(ItemFishedEvent event) {
        if (event.getEntityPlayer().world.isRemote) return;

        EntityPlayer player = event.getEntityPlayer();

        PotionEffect hasDouble = player.getActivePotionEffect(MPPotions.DOUBLE_FISHING);
        if (hasDouble != null) {
            if (!event.isCanceled()) {
                int count = hasDouble.getAmplifier() + 1;
                for (int i = 0; i < count; ++i) {
                    NonNullList<ItemStack> drops = event.getDrops();
                    for (ItemStack drop : drops) drops.add(drop.copy());
                }
            }
            player.removePotionEffect(MPPotions.DOUBLE_FISHING);
            player.world.playSound(null, player.getPosition(), MPSounds.WALLET_EFFECT_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public void onPlayerHarvestFarm(BlockEvent.HarvestDropsEvent event) {
        if (event.getHarvester() == null || event.getHarvester().world.isRemote) return;

        EntityPlayer player = event.getHarvester();

        if (event.getState().getBlock() instanceof BlockCrops) {
            PotionEffect hasDouble = player.getActivePotionEffect(MPPotions.DOUBLE_FARM_HARVEST);
            if (hasDouble != null) {
                if (!event.isCanceled()) {
                    int count = hasDouble.getAmplifier() + 1;
                    for (int i = 0; i < count; ++i) {
                        List<ItemStack> drops = event.getDrops();
                        for (ItemStack drop : drops) drops.add(drop.copy());
                    }
                }
                player.removePotionEffect(MPPotions.DOUBLE_FARM_HARVEST);
                player.world.playSound(null, player.getPosition(), MPSounds.WALLET_EFFECT_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote) return;

        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = ((EntityPlayer) event.getEntityLiving());

            PotionEffect hasImmortality = player.getActivePotionEffect(MPPotions.IMMORTALITY);
            if (hasImmortality != null) {
                event.setCanceled(true);
                player.heal(player.getMaxHealth() + player.getHealth());
                player.removePotionEffect(MPPotions.IMMORTALITY);
                player.world.playSound(null, player.getPosition(), MPSounds.WALLET_EFFECT_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public void onDropCoin(CoinDropEvent event) {
        if (event.getTarget().world.isRemote) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player != null) {
            PotionEffect hasDouble = player.getActivePotionEffect(MPPotions.DOUBLE_MYTHIC);
            if (hasDouble != null) {
                if (!event.isCanceled()) {
                    int                    count = hasDouble.getAmplifier() + 1;
                    NonNullList<CoinValue> drops = event.getCoins();
                    for (CoinValue drop : drops) {
                        if (drop.getType() == SpecificCoinType.MYTHIC) {
                            for (int i = 0; i < count; ++i) {
                                drops.add(drop.copy());
                            }
                        }
                    }
                }
                player.removePotionEffect(MPPotions.DOUBLE_MYTHIC);
                player.world.playSound(null, player.getPosition(), MPSounds.WALLET_EFFECT_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}