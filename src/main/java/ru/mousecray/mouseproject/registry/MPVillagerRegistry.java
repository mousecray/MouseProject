/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;


public class MPVillagerRegistry {
    public static final VillagerRegistry.VillagerProfession cartographer = new VillagerRegistry.VillagerProfession(
            Tags.MOD_ID + ":cartographer",
            Tags.MOD_ID + ":textures/entity/villager/cratographer.png",
            Tags.MOD_ID + ":textures/entity/zombie_villager/cratographer"
    );

    public static void clearVanilla() {
//        Field careers = MPReflectionUtils.getField(VillagerRegistry.VillagerProfession.class, "careers");
//        Field trades  = MPReflectionUtils.getField(VillagerRegistry.VillagerCareer.class, "trades");
        ForgeRegistries.VILLAGER_PROFESSIONS.getValuesCollection().forEach(profession -> {
//            List<VillagerRegistry.VillagerCareer> list = MPReflectionUtils.getFieldValue(careers, profession);
//            list.forEach(c -> {
//                List<List<EntityVillager.ITradeList>> recipes = MPReflectionUtils.getFieldValue(trades, c);
//                recipes.clear();
//            });
        });
    }

    public static void register() {
        ForgeRegistries.VILLAGER_PROFESSIONS.register(cartographer);
        VillagerRegistry.VillagerCareer cartographer = new VillagerRegistry.VillagerCareer(
                MPVillagerRegistry.cartographer, Tags.MOD_ID + ":cartographer"
        );

        cartographer.addTrade(1,
                new Sell(
                        Items.PAPER, new CountInfo(15, 24),
                        Items.EMERALD, new CountInfo(1, 2),
                        100
                ),
                new Sell(
                        Items.EMERALD, new CountInfo(7, 10),
                        Items.MAP, new CountInfo(1, 1),
                        100
                )
        );

        cartographer.addTrade(2,
                new Sell(
                        Item.getItemFromBlock(Blocks.GLASS_PANE), new CountInfo(9, 11),
                        Items.EMERALD, new CountInfo(1, 2),
                        50
                ),
                new Sell(
                        Items.COMPASS, new CountInfo(1, 1),
                        Items.EMERALD, new CountInfo(12, 15),
                        MPItems.UNEXPLORED_TREASURE_MAP, new CountInfo(1, 1),
                        100
                )
        );

        cartographer.addTrade(3,
                new Sell(
                        Items.COMPASS, new CountInfo(1, 2),
                        Items.EMERALD, new CountInfo(3, 4),
                        67
                ),
                new Sell(
                        Items.COMPASS, new CountInfo(1, 1),
                        Items.EMERALD, new CountInfo(14, 17),
                        MPItems.UNEXPLORED_STRUCTURE_MAP, new CountInfo(1, 1),
                        67
                )
        );

        cartographer.addTrade(4,
                new Sell(
                        Items.ITEM_FRAME, new CountInfo(1, 2),
                        Items.EMERALD, new CountInfo(6, 9),
                        12
                ),
                new SellBanner(
                        Items.EMERALD, new CountInfo(2, 4),
                        new CountInfo(1, 1),
                        100
                )
        );

        cartographer.addTrade(5,
                new Sell(
                        Items.COMPASS, new CountInfo(1, 1),
                        Items.EMERALD, new CountInfo(5, 10),
                        MPItems.UNEXPLORED_TREASURE_MAP, new CountInfo(1, 1),
                        100
                ),
                new Sell(
                        Items.COMPASS, new CountInfo(1, 1),
                        Items.EMERALD, new CountInfo(7, 12),
                        MPItems.UNEXPLORED_STRUCTURE_MAP, new CountInfo(1, 1),
                        100
                )
        );
    }

    public static class Sell implements EntityVillager.ITradeList {
        protected final Item buy1, buy2, sell;
        protected final CountInfo count1, count2, price;
        protected final int chance;

        public Sell(Item buy, CountInfo count, Item sell, CountInfo price, int chance) {
            buy1 = buy;
            count1 = count;
            buy2 = null;
            count2 = null;
            this.sell = sell;
            this.price = price;
            this.chance = chance;
        }

        public Sell(Item buy1, CountInfo count1, Item buy2, CountInfo count2, Item sell, CountInfo price, int chance) {
            this.buy1 = buy1;
            this.buy2 = buy2;
            this.count1 = count1;
            this.count2 = count2;
            this.sell = sell;
            this.price = price;
            this.chance = chance;
        }

        @Override
        public void addMerchantRecipe(@Nonnull IMerchant merchant, @Nonnull MerchantRecipeList recipeList, @Nonnull Random random) {
            if (random.nextInt(100) < chance) {
                if (buy2 != null) {
                    assert count2 != null;
                    recipeList.add(new MerchantRecipe(
                            new ItemStack(buy1, count1.getCount(random)),
                            new ItemStack(buy2, count2.getCount(random)),
                            new ItemStack(sell, price.getCount(random))
                    ));
                } else {
                    recipeList.add(new MerchantRecipe(
                            new ItemStack(buy1, count1.getCount(random)),
                            new ItemStack(sell, price.getCount(random))
                    ));
                }
            }
        }
    }

    public static class SellBanner extends Sell {
        public SellBanner(Item buy, CountInfo count, CountInfo price, int chance) {
            super(buy, count, Items.BANNER, price, chance);
        }

        @Override
        public void addMerchantRecipe(@Nonnull IMerchant merchant, @Nonnull MerchantRecipeList recipeList, @Nonnull Random random) {
            if (random.nextInt(100) < chance) {
                ItemStack stack = new ItemStack(sell, price.getCount(random));
                MouseProjectNBT.get(stack).getDefaultPipe().saveBase(EnumDyeColor.values()[random.nextInt(16)].getMetadata());
                recipeList.add(new MerchantRecipe(new ItemStack(buy1, count1.getCount(random)), stack));
            }
        }
    }

    public static class CountInfo extends Tuple<Integer, Integer> {
        public CountInfo(int min, int max) { super(min, max); }

        public int getCount(Random rand) {
            return getFirst() >= getSecond() ?
                    getFirst() :
                    getFirst() + rand.nextInt(getSecond() - getFirst() + 1);
        }
    }

    @Nullable
    public static VillagerRegistry.VillagerProfession getProfessionForBlock(Block block) {
        if (block == Blocks.ANVIL) return cartographer;
        else return null;
    }
}