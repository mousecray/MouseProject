/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.economy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;
import ru.mousecray.mouseproject.common.event.CoinDropEvent;
import ru.mousecray.mouseproject.common.event.MPEventPipeline;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.registry.MPDamageSources;
import ru.mousecray.mouseproject.registry.MPSounds;
import ru.mousecray.mouseproject.utils.MPMathUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class CoinHelper {
    private static final long[]           DIVISORS      = { 1L, 64L, 4096L, 262144L, 16777216L, 1073741824L, 68719476736L };
    private static final String[]         SUFFIXES      = { "K", "M", "B", "T", "Q", "Qi" };
    private static final NormalCoinType[] COIN_TYPES    = NormalCoinType.values();
    private static       long             lastRemaining = 0;

    public static String formatBalanceLong(long value) {
        if (value == 0) return "0";
        boolean negative = value < 0;
        long    absValue = Math.abs(value);
        if (absValue < 1000) return (negative ? "-" : "") + absValue;
        @SuppressWarnings("UnnecessaryLocalVariable") double dValue    = absValue;
        int                                                  magnitude = (int) Math.floor(Math.log10(dValue) / 3);
        double                                               divisor   = Math.pow(10, magnitude * 3);
        double                                               shortened = dValue / divisor;
        DecimalFormat                                        df        = new DecimalFormat("#.###");
        String                                               formatted = df.format(shortened);
        String                                               suffix    = SUFFIXES[magnitude - 1];
        return (negative ? "-" : "") + formatted + suffix;
    }

    public static String formatBalanceNormal(long value) {
        if (value == 0) return "0";
        boolean negative = value < 0;
        long    absValue = Math.abs(value);
        if (absValue < 1000) return (negative ? "-" : "") + absValue;
        @SuppressWarnings("UnnecessaryLocalVariable") double dValue    = absValue;
        int                                                  magnitude = (int) Math.floor(Math.log10(dValue) / 3);
        double                                               divisor   = Math.pow(10, magnitude * 3);
        double                                               shortened = dValue / divisor;
        DecimalFormat                                        df        = new DecimalFormat("#.##");
        String                                               formatted = df.format(shortened);
        String                                               suffix    = SUFFIXES[magnitude - 1];
        return (negative ? "-" : "") + formatted + suffix;
    }

    public static String formatBalanceShort(long value) {
        if (value == 0) return "0";
        boolean negative = value < 0;
        long    absValue = Math.abs(value);
        if (absValue < 1000) return (negative ? "-" : "") + absValue;
        @SuppressWarnings("UnnecessaryLocalVariable") double dValue    = absValue;
        int                                                  magnitude = (int) Math.floor(Math.log10(dValue) / 3);
        double                                               divisor   = Math.pow(10, magnitude * 3);
        double                                               shortened = dValue / divisor;
        DecimalFormat                                        df        = new DecimalFormat("#.#");
        String                                               formatted = df.format(shortened);
        String                                               suffix    = SUFFIXES[magnitude - 1];
        return (negative ? "-" : "") + formatted + suffix;
    }

    public static float getDifficultyMultiplier(EnumDifficulty difficulty) {
        switch (difficulty) {
            case PEACEFUL:
                return 0.25f;
            case EASY:
                return 0.5f;
            case HARD:
                return 1.5f;
            case NORMAL:
            default:
                return 1.0f;
        }
    }
    public static void dropInWorld(World world, DamageSource cause, EntityLivingBase entity,
                                   Random rand,
                                   List<CoinValue> normal, List<CoinValue> specific, List<CoinValue> resource,
                                   List<CoinValue> other,
                                   boolean isNew,
                                   boolean playSound
    ) {
        ArrayList<EntityItem> capturedDrops = new ArrayList<>();

        NonNullList<CoinValue> finalCoins = NonNullList.create();
        if (normal != null && !normal.isEmpty()) {
            long sum = normal
                    .stream()
                    .mapToLong(val -> fromTypeToBronze(((NormalCoinType) val.getType()), val.getValue()))
                    .sum();
            CoinValue fullNormal = CoinValue.create(sum, NormalCoinType.BRONZE);
            if (fullNormal.isPositive()) {
                EnumMap<NormalCoinType, Long> displayCoins = getDisplayCoins(
                        getMaxCoin(fullNormal.getValue()), fullNormal.getValue()
                );
                displayCoins.put(NormalCoinType.BRONZE, displayCoins.get(NormalCoinType.BRONZE) + getDisplayRemainingBronze());
                displayCoins.forEach((type, value) -> {
                    int i = rand.nextInt(4) + 2;
                    if (fullNormal.isLessOrEqual(CoinValue.create((long) i, type))) i = fullNormal.getAsInt();
                    long[] groups = MPMathUtils.distributeNumber(value, i);
                    if (groups != null) {
                        for (long group : groups) {
                            if (group > 0) {
                                long[] stacks = MPMathUtils.distributeNumber(group, 64);
                                if (stacks != null) {
                                    for (long stack : stacks) if (stack > 0) finalCoins.add(CoinValue.create(stack, type));
                                }
                            }
                        }
                    }
                });
            }
        }

        if (specific != null && !specific.isEmpty()) {
            for (CoinValue spec : specific) {
                if (spec != null && spec.isPositive()) {
                    int i = rand.nextInt(4) + 2;
                    if (spec.isLessOrEqual(CoinValue.create(i, spec.getType()))) i = spec.getAsInt();
                    int[] array = MPMathUtils.distributeNumber(spec.getAsInt(), i);
                    if (array != null) for (int i1 : array) if (i1 > 0) finalCoins.add(CoinValue.create(i1, spec.getType()));
                }
            }
        }

        if (resource != null && !resource.isEmpty()) {
            for (CoinValue res : resource) {
                if (res != null && res.isPositive()) {
                    int i = rand.nextInt(4) + 2;
                    if (res.isLessOrEqual(CoinValue.create(i, res.getType()))) i = res.getAsInt();
                    int[] array = MPMathUtils.distributeNumber(res.getAsInt(), i);
                    if (array != null) for (int i1 : array) if (i1 > 0) finalCoins.add(CoinValue.create(i1, res.getType()));
                }
            }
        }

        if (other != null && !other.isEmpty()) {
            for (CoinValue oth : other) {
                if (oth != null && oth.isPositive()) {
                    int i = rand.nextInt(4) + 2;
                    if (oth.isLessOrEqual(CoinValue.create(i, oth.getType()))) i = oth.getAsInt();
                    long[] array = MPMathUtils.distributeNumber(oth.getValue(), i);
                    if (array != null) for (long i1 : array) if (i1 > 0) finalCoins.add(CoinValue.create(i1, oth.getType()));
                }
            }
        }

        Entity trueSource = cause.getTrueSource();
        CoinDropEvent coinDropEvent = MPEventPipeline.instance().fireCoinDropEvent(
                trueSource instanceof EntityPlayer ? ((EntityPlayer) trueSource) : null, entity, cause, finalCoins
        );
        if (coinDropEvent.isCanceled() || finalCoins.isEmpty()) return;

        for (CoinValue finalCoin : finalCoins) {
            ItemStack stack = new ItemStack(finalCoin.getType().getItem(), finalCoin.getAsInt());
            if (isNew) MouseProjectNBT.get(stack).getCoinPipe().setIsNew();
            capturedDrops.add(new EntityItem(world, entity.posX, entity.posY, entity.posZ, stack));
        }

        int i = ForgeHooks.getLootingLevel(entity, cause.getTrueSource(), MPDamageSources.ON_DROPPED_COIN);
        if (!ForgeHooks.onLivingDrops(entity, MPDamageSources.ON_DROPPED_COIN, capturedDrops, i, entity.recentlyHit > 0)) {
            for (EntityItem item : capturedDrops) {
                float scatter    = 0.4F;
                float upVelocity = 0.5F;

                item.motionX = (rand.nextFloat() - 0.5F) * scatter;
                item.motionY = rand.nextFloat() * upVelocity;
                item.motionZ = (rand.nextFloat() - 0.5F) * scatter;
                item.setPickupDelay(40);
                if (playSound) {
                    world.playSound(null, entity.getPosition(), MPSounds.COIN_DROP, SoundCategory.PLAYERS, 1f, 1f + (rand.nextFloat() - 0.5f));
                }
                world.spawnEntity(item);
            }
        }
    }
    public static long fromBronzeToType(NormalCoinType toType, long bronze) {
        return bronze / DIVISORS[toType.getID()];
    }
    public static long fromTypeToBronze(NormalCoinType fromType, long value) {
        return value * DIVISORS[fromType.getID()];
    }
    public static NormalCoinType getMaxCoin(long bronze) {
        for (int i = COIN_TYPES.length - 1; i >= 0; --i) {
            NormalCoinType type = COIN_TYPES[i];
            if (fromBronzeToType(type, bronze) > 0) return type;
        }
        return NormalCoinType.BRONZE;
    }
    public static EnumMap<NormalCoinType, Long> getDisplayCoins(NormalCoinType startType, long bronze) {
        EnumMap<NormalCoinType, Long> display   = new EnumMap<>(NormalCoinType.class);
        long                          remaining = bronze;

        int startIndex = startType.getID();

        long divisor = DIVISORS[startIndex];
        long coins   = remaining / divisor;

        if (coins > 0) {
            display.put(COIN_TYPES[startIndex], coins);
            remaining -= coins * divisor;
        }

        for (int i = startIndex - 1; i >= 0; --i) {
            divisor /= 64;
            long displayCoins = Math.min(remaining / divisor, 64);
            if (displayCoins > 0) {
                display.put(COIN_TYPES[i], displayCoins);
                remaining -= displayCoins * divisor;
            }
        }

        lastRemaining = remaining;

        return display;
    }
    public static long getDisplayRemainingBronze() { return lastRemaining; }
}