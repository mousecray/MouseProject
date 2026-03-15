/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.eventhandler;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.mousecray.mouseproject.common.capability.ICapabilityInventory;
import ru.mousecray.mouseproject.common.capability.impl.CapabilityWalletInventory;
import ru.mousecray.mouseproject.common.economy.CoinHelper;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;
import ru.mousecray.mouseproject.common.economy.coin.ResourceCoinType;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;
import ru.mousecray.mouseproject.common.item.coin.ICoin;
import ru.mousecray.mouseproject.common.item.wallet.IWallet;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.registry.MPDamageSources;
import ru.mousecray.mouseproject.registry.MPSounds;
import ru.mousecray.mouseproject.utils.MPMathUtils;
import ru.mousecray.mouseproject.utils.MPRandomUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class CoinHandler {
    @SubscribeEvent
    public void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityLiving)) return;
        if (event.getSource() == MPDamageSources.ON_DROPPED_COIN) return;

        EntityLiving mob   = (EntityLiving) event.getEntity();
        World        world = mob.world;
        if (world.isRemote) return;

        if (!world.getGameRules().getBoolean("doMobLoot")) return;

        float  maxHealth            = mob.getMaxHealth();
        float  difficultyMultiplier = CoinHelper.getDifficultyMultiplier(world.getDifficulty());
        Random rng                  = mob.getRNG();

        List<CoinValue> normal   = new ArrayList<>();
        List<CoinValue> specific = new ArrayList<>();
        List<CoinValue> resource = new ArrayList<>();

        long bronze = 0;
        if (mob.isNonBoss()) {
            if (mob instanceof EntityAnimal || mob instanceof EntityVillager) {
                bronze = (long) (maxHealth * 0.25 * difficultyMultiplier);
            } else if (mob instanceof EntityMob) {
                bronze = (long) (maxHealth * 1 * difficultyMultiplier);
            }
        } else {
            bronze = (long) (maxHealth * 150 * difficultyMultiplier);
            bronze = Math.min(bronze, CoinHelper.fromTypeToBronze(NormalCoinType.GOLD, 15));
            normal.add(CoinValue.create(bronze, NormalCoinType.BRONZE));
            specific.add(CoinValue.create(rng.nextInt(3) + 1, SpecificCoinType.MYTHIC));
        }
        if (bronze > 0) {
            bronze += MPRandomUtils.normalPercentFrom(rng, bronze, 25, 1);
            if (mob.isChild()) bronze /= 2;
            if (bronze > 0) normal.add(CoinValue.create(bronze, NormalCoinType.BRONZE));
        }

        CoinHelper.dropInWorld(world, event.getSource(), mob, rng, normal, specific, resource, null, true, true);
    }

    @SubscribeEvent
    public void onEntityDrops(LivingDropsEvent event) {
        event.getDrops().forEach(i -> {
            ItemStack stack = i.getItem();
            Item      item  = stack.getItem();
            if (item instanceof ICoin) {
                ((ICoin) item).onDropWhenDeath(i.world, event.getEntityLiving(), stack, event.getEntityLiving().getRNG());
            }
        });
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        event.getDrops().forEach(i -> {
            ItemStack stack = i.getItem();
            Item      item  = stack.getItem();
            if (item instanceof ICoin) {
                ((ICoin) item).onDropWhenDeath(i.world, player, stack, player.getRNG());
            }
        });

        if (!player.world.getGameRules().getBoolean("doMobLoot")) return;

        ICapabilityInventory<CapabilityWalletInventory> walletCap = player.getCapability(CapabilityWalletInventory.InventoryProvider.WALLET_INVENTORY, null);
        if (walletCap != null) {
            ItemStack stack = walletCap.getInventory().getStackInSlot(0);
            if (stack.getItem() instanceof IWallet) {
                IWallet         wallet   = (IWallet) stack.getItem();
                World           world    = player.world;
                List<CoinValue> normal   = new ArrayList<>();
                List<CoinValue> specific = new ArrayList<>();
                List<CoinValue> resource = new ArrayList<>();

                CoinValue bronze = wallet.getCoin(world, player, stack, NormalCoinType.BRONZE);
                Random    rand   = player.getRNG();
                if (bronze != null) {
                    bronze = bronze.plus(
                            CoinValue.create(
                                    MPRandomUtils.normalPercentFrom(rand, bronze.getValue(), 25, 1),
                                    NormalCoinType.BRONZE)
                    );
                    CoinValue actualBronze = wallet.takeCoin(world, player, stack, bronze);
                    if (actualBronze != null && actualBronze.isPositive()) normal.add(actualBronze);
                }

                for (SpecificCoinType value : SpecificCoinType.values()) {
                    CoinValue spec = wallet.getCoin(world, player, stack, value);
                    if (spec != null) {
                        spec = spec.plus(
                                CoinValue.create(
                                        MPRandomUtils.normalPercentFrom(rand, spec.getAsInt(), 25, 1),
                                        value)
                        );
                        CoinValue actualSpec = wallet.takeCoin(world, player, stack, spec);
                        if (actualSpec != null && actualSpec.isPositive()) specific.add(actualSpec);
                    }
                }

                for (ResourceCoinType value : ResourceCoinType.values()) {
                    CoinValue res = wallet.getCoin(world, player, stack, value);
                    if (res != null) {
                        res = res.plus(
                                CoinValue.create(
                                        MPRandomUtils.normalPercentFrom(rand, res.getAsInt(), 25, 1),
                                        value)
                        );
                        CoinValue actualRes = wallet.takeCoin(world, player, stack, res);
                        if (actualRes != null && actualRes.isPositive()) resource.add(actualRes);
                    }
                }

                CoinHelper.dropInWorld(
                        world, event.getSource(), player, rand,
                        normal, specific, resource, null,
                        true, true
                );
            }
        }
    }

    @SubscribeEvent
    public void onThrowCoin(ItemTossEvent event) {
        if (event.getEntity().world.isRemote) return;

        EntityItem entityItem = event.getEntityItem();
        ItemStack  stack      = entityItem.getItem();
        Item       item       = stack.getItem();
        if (item instanceof ICoin) ((ICoin) item).onToss(entityItem.world, event.getEntity(), stack, event.getPlayer().getRNG());
    }

    @SubscribeEvent
    public void onPickupCoin(EntityItemPickupEvent event) {
        //Игнорируем клиентский мир
        if (event.getEntity().world.isRemote) return;

        ItemStack stack = event.getItem().getItem();
        Item      item  = stack.getItem();

        //Игнорируем предметы, не наследующие ICoin
        if (!(item instanceof ICoin)) return;

        EntityLivingBase entity = event.getEntityLiving();
        World            world  = entity.world;
        Random           rand   = entity.getRNG();

        //Обрабатываем предметы ICoin
        ICapabilityInventory<CapabilityWalletInventory> walletCap   = event.getEntity().getCapability(CapabilityWalletInventory.InventoryProvider.WALLET_INVENTORY, null);
        List<ItemStack>                                 stackToDrop = new ArrayList<>(); // Список для дропа остатков

        if (walletCap != null) {
            //Есть кошелёк (IWallet)
            ItemStack walletStack = walletCap.getInventory().getStackInSlot(0);
            if (walletStack.getItem() instanceof IWallet) {
                IWallet   wallet     = (IWallet) walletStack.getItem();
                ItemStack stackToAdd = stack.copy();
                CoinValue remain     = wallet.putCoin(world, entity, walletStack, stackToAdd);

                //Проигрываем звук, если что-то добавлено в кошелёк
                if (remain == null || remain.getValue() < stackToAdd.getCount()) {
                    world.playSound(null, entity.getPosition(), MPSounds.COIN_PICKUP, SoundCategory.PLAYERS, 1f, 1f + (rand.nextFloat() - 0.5f));
                }

                if (remain != null && remain.isPositive()) {
                    //Адаптируем остаток для NormalCoinType
                    if (remain.getType() instanceof NormalCoinType) {
                        EnumMap<NormalCoinType, Long> displayCoins = CoinHelper.getDisplayCoins(CoinHelper.getMaxCoin(remain.getValue()), remain.getValue());
                        displayCoins.forEach((type, val) -> {
                            int count = val.intValue();
                            if (count > 0) stackToDrop.add(new ItemStack(type.getItem(), count));
                        });
                    } else {
                        //Для других типов разбиваем на группы по 64
                        int[] groups = MPMathUtils.toGroupWithMax(remain.getValue(), 64);
                        if (groups != null) {
                            for (int group : groups) {
                                if (group > 0) stackToDrop.add(new ItemStack(remain.getType().getItem(), group));
                            }
                        }
                    }
                } else {
                    //Всё помещается в кошелёк
                    ((ICoin) item).onPickup(world, entity, stack, rand);
                    event.setCanceled(true);
                    event.getItem().setItem(ItemStack.EMPTY);
                    event.getItem().setDead();
                    return; //Завершаем, если всё добавлено
                }
            } else {
                //Кошелёк не IWallet, обрабатываем как без кошелька
                stackToDrop.add(stack.copy());
            }
        } else {
            //Нет кошелька
            stackToDrop.add(stack.copy());
        }

        //Пытаемся добавить в инвентарь
        EntityPlayer player = event.getEntityPlayer();
        if (!stackToDrop.isEmpty()) {
            List<ItemStack> inventoryResult = new ArrayList<>();
            for (ItemStack itemStack : stackToDrop) {
                ItemStack remain = canAddStackToInventory(player, itemStack);

                if (remain.isEmpty()) {
                    ((ICoin) item).onPickup(world, player, itemStack.copy(), rand); //Вызываем onPickup для добавленного
                    world.playSound(null, entity.getPosition(), MPSounds.COIN_PICKUP, SoundCategory.PLAYERS, 1f, 1f + (rand.nextFloat() - 0.5f));
                    player.inventory.addItemStackToInventory(itemStack.copy());
                    player.inventory.markDirty();
                } else {
                    int canBeAdded = itemStack.getCount() - remain.getCount();

                    if (canBeAdded > 0) {
                        ItemStack toAdd = itemStack.copy();
                        toAdd.setCount(canBeAdded);
                        ((ICoin) item).onPickup(world, player, toAdd, rand); //Вызываем onPickup для добавленного
                        player.inventory.addItemStackToInventory(toAdd);
                        player.inventory.markDirty();

                        //Проигрываем звук для каждого добавленного стака в инвентарь
                        world.playSound(null, entity.getPosition(), MPSounds.COIN_PICKUP, SoundCategory.PLAYERS, 1f, 1f + (rand.nextFloat() - 0.5f));
                    }

                    if (remain.getCount() > 0) {
                        inventoryResult.add(remain);
                    } else if (canBeAdded == 0) {
                        inventoryResult.add(itemStack); //Если ничего не добавлено, оставляем как есть
                    }
                }
            }

            //Дропаем остаток с использованием dropInWorld
            if (!inventoryResult.isEmpty()) {
                boolean         firstSimilar = false;
                List<CoinValue> normal       = new ArrayList<>();
                List<CoinValue> specific     = new ArrayList<>();
                List<CoinValue> resource     = new ArrayList<>();
                List<CoinValue> other        = new ArrayList<>();

                for (ItemStack resultStack : inventoryResult) {
                    Item resultItem = resultStack.getItem();
                    if (resultItem instanceof ICoin) {
                        CoinType resultType = ((ICoin) resultItem).getCoinType();
                        if (!firstSimilar && resultType == ((ICoin) item).getCoinType()) {
                            //Обновляем текущий стек, если тип совпадает
                            if (resultStack.getCount() < stack.getCount()) {
                                stack.setCount(resultStack.getCount());
                            }
                            firstSimilar = true;
                        } else {
                            //Собираем данные для dropInWorld
                            if (resultType instanceof NormalCoinType) {
                                normal.add(CoinValue.create(resultStack.getCount(), resultType));
                            } else if (resultType instanceof SpecificCoinType) {
                                specific.add(CoinValue.create(resultStack.getCount(), resultType));
                            } else if (resultType instanceof ResourceCoinType) {
                                resource.add(CoinValue.create(resultStack.getCount(), resultType));
                            } else if (resultType != null) {
                                other.add(CoinValue.create(resultStack.getCount(), resultType));
                            }
                        }
                    }
                }

                if (!firstSimilar || (!normal.isEmpty() || !specific.isEmpty() || !resource.isEmpty() || !other.isEmpty())) {
                    CoinHelper.dropInWorld(
                            world, MPDamageSources.ON_DROPPED_COIN, entity, rand,
                            normal, specific, resource, other,
                            MouseProjectNBT.get(stack).getCoinPipe().loadIsNew(), false
                    );
                }

                if (!firstSimilar) {
                    //Если не нашли совпадающий тип, отменяем событие и убираем EntityItem
                    event.setCanceled(true);
                    event.getItem().setItem(ItemStack.EMPTY);
                    event.getItem().setDead();
                }
            } else {
                event.setCanceled(true);
                event.getItem().setItem(ItemStack.EMPTY);
                event.getItem().setDead();
            }
        }
    }

    private ItemStack canAddStackToInventory(@Nonnull EntityPlayer player, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        if (player.isCreative()) return ItemStack.EMPTY;

        InventoryPlayer inventory    = player.inventory;
        int             totalSpace   = 0;
        int             stackSize    = stack.getCount();
        Item            item         = stack.getItem();
        int             maxStackSize = Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit());

        //Проверяем наличие свободного слота
        if (inventory.getFirstEmptyStack() != -1) return ItemStack.EMPTY; //Возвращаем пустой стак, если есть место

        int sizeInventory = inventory.getSizeInventory();
        for (int i = 0; i < sizeInventory; ++i) {
            ItemStack slotStack = inventory.getStackInSlot(i);
            //Считаем только стаки с тем же item, тегами и без повреждений/чар
            if (slotStack.getItem() == item
                    && ItemStack.areItemStackTagsEqual(slotStack, stack)
                    && !slotStack.isItemDamaged()
                    && !slotStack.isItemEnchanted()) {
                totalSpace += maxStackSize - slotStack.getCount();
            }
        }

        if (totalSpace >= stackSize) return ItemStack.EMPTY;

        int remaining = stackSize - totalSpace;
        if (remaining > 0) return new ItemStack(item, remaining, stack.getMetadata(), stack.getTagCompound());

        return ItemStack.EMPTY;
    }
}