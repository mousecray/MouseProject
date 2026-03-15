/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.item.wallet;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.economy.CoinHelper;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.capacity.WalletCapacity;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;
import ru.mousecray.mouseproject.common.economy.coin.ResourceCoinType;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;
import ru.mousecray.mouseproject.common.item.MPDefaultItem;
import ru.mousecray.mouseproject.common.item.coin.ICoin;
import ru.mousecray.mouseproject.nbt.ItemStackWalletNBTPipeline;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.registry.MPTriggers;
import ru.mousecray.mouseproject.registry.constants.GuiIdentifiers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemWallet extends MPDefaultItem implements IWallet {
    private final           WalletType             type;
    @Nullable private final WalletType             leakType;
    @Nullable private final WalletType             normalType;
    private final           Set<WalletCapacity<?>> capacities;

    public ItemWallet(WalletType type, @Nullable WalletType leakType, @Nullable WalletType normalType, WalletCapacity<?>... capacities) {
        super(type.getTranslationKey());
        this.type = type;
        this.leakType = leakType;
        this.normalType = normalType;
        this.capacities = new HashSet<>();
        Collections.addAll(this.capacities, capacities);
        setMaxStackSize(1);
    }

    @Nonnull @Override
    public CoinValue getCapacity(@Nonnull CoinType type) {
        for (WalletCapacity<?> capacity : capacities) {
            if (capacity.isCoinSupported(type)) {
                return CoinValue.create(capacity.getCapacity(type), type);
            }
        }
        return CoinValue.createEmpty(type);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        BlockPos pos = player.getPosition();
        player.openGui(MouseProject.INSTANCE, GuiIdentifiers.WALLET_OPEN_GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
        player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override public void randomTick(World world, EntityLiving entity, ItemStack wallet) { }

    @Override @Nullable
    public CoinValue putCoin(World world, EntityLivingBase entity, ItemStack wallet, ItemStack coin) {
        Item itemCoin = coin.getItem();
        if (!(itemCoin instanceof ICoin) || wallet.getItem() != this) return null;

        ItemStackWalletNBTPipeline walletPipe = MouseProjectNBT.get(wallet).getWalletPipe();
        CoinType                   coinType   = ((ICoin) itemCoin).getCoinType();

        CoinValue result;

        if (coinType instanceof NormalCoinType) {
            CoinValue bronzeBalance = walletPipe.loadBronzeBalance();
            if (bronzeBalance == null) bronzeBalance = CoinValue.createEmpty(NormalCoinType.BRONZE);
            CoinValue newBronzeBalance = bronzeBalance.plus(
                    CoinValue.create(
                            CoinHelper.fromTypeToBronze((NormalCoinType) coinType, coin.getCount()),
                            NormalCoinType.BRONZE
                    )
            );
            if (newBronzeBalance != null) {
                CoinValue capacity = getCapacity(NormalCoinType.BRONZE);
                if (newBronzeBalance.isMore(capacity)) {
                    walletPipe.saveBronzeBalance(capacity);
                    result = newBronzeBalance.minus(capacity);
                } else {
                    walletPipe.saveBronzeBalance(newBronzeBalance);
                    result = CoinValue.createEmpty(NormalCoinType.BRONZE);
                }
            } else return CoinValue.createEmpty(NormalCoinType.BRONZE);
        } else if (coinType instanceof SpecificCoinType) {
            SpecificCoinType specificCoinType = (SpecificCoinType) coinType;
            CoinValue        specificBalance  = walletPipe.loadSpecificBalance(specificCoinType);
            if (specificBalance == null) specificBalance = CoinValue.createEmpty(specificCoinType);
            CoinValue newSpecificBalance = specificBalance.plus(
                    CoinValue.create(coin.getCount(), specificCoinType)
            );
            if (newSpecificBalance != null) {
                CoinValue capacity = getCapacity(specificCoinType);
                if (newSpecificBalance.isMore(capacity)) {
                    walletPipe.saveSpecificBalance(capacity);
                    result = newSpecificBalance.minus(capacity);
                } else {
                    walletPipe.saveSpecificBalance(newSpecificBalance);
                    result = CoinValue.createEmpty(specificCoinType);
                }
            } else result = CoinValue.createEmpty(specificCoinType);
        } else if (coinType instanceof ResourceCoinType) {
            ResourceCoinType resourceCoinType = (ResourceCoinType) coinType;
            CoinValue        resourceBalance  = walletPipe.loadResourceBalance(resourceCoinType);
            if (resourceBalance == null) resourceBalance = CoinValue.createEmpty(resourceCoinType);
            CoinValue newResourceBalance = resourceBalance.plus(
                    CoinValue.create(coin.getCount(), resourceCoinType)
            );
            if (newResourceBalance != null) {
                CoinValue capacity = getCapacity(resourceCoinType);
                if (newResourceBalance.isMore(capacity)) {
                    walletPipe.saveResourceBalance(capacity);
                    result = newResourceBalance.minus(capacity);
                } else {
                    walletPipe.saveResourceBalance(newResourceBalance);
                    result = CoinValue.createEmpty(resourceCoinType);
                }
            } else return CoinValue.createEmpty(resourceCoinType);
        } else if (coinType != null) {
            CoinValue otherBalance = walletPipe.loadOtherBalance(coinType);
            if (otherBalance == null) otherBalance = CoinValue.createEmpty(coinType);
            CoinValue newOtherBalance = otherBalance.plus(CoinValue.create(coin.getCount(), coinType));
            if (newOtherBalance != null) {
                CoinValue capacity = getCapacity(coinType);
                if (newOtherBalance.isMore(capacity)) {
                    walletPipe.saveOtherBalance(capacity);
                    result = newOtherBalance.minus(capacity);
                } else {
                    walletPipe.saveOtherBalance(newOtherBalance);
                    result = CoinValue.createEmpty(coinType);
                }
            } else result = CoinValue.createEmpty(coinType);
        } else result = CoinValue.createEmpty(coinType);

        if (!world.isRemote && entity instanceof EntityPlayer) {
            boolean        isFull    = true;
            List<CoinType> coinTypes = walletPipe.loadAllBalanceTypes();
            for (CoinType finalBalType : coinTypes) {
                CoinValue capacity = getCapacity(finalBalType);
                CoinValue balance;

                if (finalBalType instanceof NormalCoinType) balance = walletPipe.loadBronzeBalance();
                else if (finalBalType instanceof SpecificCoinType) balance = walletPipe.loadSpecificBalance(finalBalType);
                else if (finalBalType instanceof ResourceCoinType) balance = walletPipe.loadResourceBalance(finalBalType);
                else balance = walletPipe.loadOtherBalance(finalBalType);

                if (balance == null || !capacity.isLess(balance)) {
                    isFull = false;
                    break;
                }
            }

            if (isFull) MPTriggers.FULL_WALLET.trigger(((EntityPlayerMP) entity), getWalletType());
        }

        return result;
    }

    @Override @Nullable
    public CoinValue takeCoin(World world, EntityLivingBase entity, ItemStack wallet, CoinValue coinValue) {
        if (wallet.getItem() != this) return null;

        ItemStackWalletNBTPipeline walletPipe = MouseProjectNBT.get(wallet).getWalletPipe();
        CoinType                   coinType   = coinValue.getType();

        if (coinType instanceof NormalCoinType) {
            CoinValue bronzeBalance = walletPipe.loadBronzeBalance();
            if (bronzeBalance == null) bronzeBalance = CoinValue.createEmpty(NormalCoinType.BRONZE);
            CoinValue value = CoinValue.create(
                    CoinHelper.fromTypeToBronze((NormalCoinType) coinType, coinValue.getValue()),
                    NormalCoinType.BRONZE
            );
            CoinValue newBronzeBalance = bronzeBalance.minus(value);
            if (newBronzeBalance != null) {
                if (newBronzeBalance.isPositiveOrNull()) {
                    walletPipe.saveBronzeBalance(newBronzeBalance);
                    return CoinValue.createEmpty(NormalCoinType.BRONZE);
                } else {
                    walletPipe.saveBronzeBalance(CoinValue.createEmpty(NormalCoinType.BRONZE));
                    return value.minus(bronzeBalance);
                }
            } else return coinValue;
        } else if (coinType instanceof SpecificCoinType) {
            SpecificCoinType specificCoinType = (SpecificCoinType) coinType;
            CoinValue        specificBalance  = walletPipe.loadSpecificBalance(specificCoinType);
            if (specificBalance == null) specificBalance = CoinValue.createEmpty(specificCoinType);
            CoinValue newSpecificBalance = specificBalance.minus(coinValue);
            if (newSpecificBalance != null) {
                if (newSpecificBalance.isPositiveOrNull()) {
                    walletPipe.saveSpecificBalance(newSpecificBalance);
                    return CoinValue.createEmpty(specificCoinType);
                } else {
                    walletPipe.saveSpecificBalance(CoinValue.createEmpty(specificCoinType));
                    return coinValue.minus(specificBalance);
                }
            } else return coinValue;
        } else if (coinType instanceof ResourceCoinType) {
            ResourceCoinType resourceCoinType = (ResourceCoinType) coinType;
            CoinValue        resourceBalance  = walletPipe.loadResourceBalance(resourceCoinType);
            if (resourceBalance == null) resourceBalance = CoinValue.createEmpty(resourceCoinType);
            CoinValue newResourceBalance = resourceBalance.plus(resourceBalance.minus(coinValue));
            if (newResourceBalance != null) {
                if (newResourceBalance.isPositiveOrNull()) {
                    walletPipe.saveResourceBalance(newResourceBalance);
                    return CoinValue.createEmpty(resourceCoinType);
                } else {
                    walletPipe.saveResourceBalance(CoinValue.createEmpty(resourceCoinType));
                    return coinValue.minus(resourceBalance);
                }
            } else return coinValue;
        } else if (coinType != null) {
            CoinValue otherBalance = walletPipe.loadOtherBalance(coinType);
            if (otherBalance == null) otherBalance = CoinValue.createEmpty(coinType);
            CoinValue newResourceBalance = otherBalance.plus(otherBalance.minus(coinValue));
            if (newResourceBalance != null) {
                if (newResourceBalance.isPositiveOrNull()) {
                    walletPipe.saveOtherBalance(newResourceBalance);
                    return CoinValue.createEmpty(coinType);
                } else {
                    walletPipe.saveOtherBalance(CoinValue.createEmpty(coinType));
                    return coinValue.minus(otherBalance);
                }
            } else return coinValue;
        } else return coinValue;
    }

    @Nullable @Override
    public CoinValue getCoin(World world, EntityLivingBase entity, ItemStack wallet, @Nullable CoinType type) {
        if (wallet.getItem() != this) return null;

        ItemStackWalletNBTPipeline walletPipe = MouseProjectNBT.get(wallet).getWalletPipe();
        if (type instanceof NormalCoinType) {
            CoinValue bronzeBalance = walletPipe.loadBronzeBalance();
            if (bronzeBalance == null) bronzeBalance = CoinValue.createEmpty(NormalCoinType.BRONZE);
            return CoinValue.create(
                    CoinHelper.fromBronzeToType(
                            ((NormalCoinType) type), bronzeBalance.getValue()
                    ),
                    type
            );
        } else if (type instanceof SpecificCoinType) {
            CoinValue specificBalance = walletPipe.loadSpecificBalance(type);
            if (specificBalance == null) specificBalance = CoinValue.createEmpty(type);
            return specificBalance;
        } else if (type instanceof ResourceCoinType) {
            CoinValue resourceBalance = walletPipe.loadResourceBalance(type);
            if (resourceBalance == null) resourceBalance = CoinValue.createEmpty(type);
            return resourceBalance;
        } else if (type != null) {
            CoinValue otherBalance = walletPipe.loadOtherBalance(type);
            if (otherBalance == null) otherBalance = CoinValue.createEmpty(type);
            return otherBalance;
        } else return null;
    }

    @Nonnull @Override
    public List<CoinType> getCurrentCoins(World world, EntityLivingBase entity, ItemStack wallet) {
        if (wallet.getItem() != this) return Collections.emptyList();
        ItemStackWalletNBTPipeline walletPipe = MouseProjectNBT.get(wallet).getWalletPipe();
        return walletPipe.loadAllBalanceTypes();
    }

    @Override public void onEquip(World world, EntityLivingBase entity, ItemStack wallet)   { }
    @Override public void onUnequip(World world, EntityLivingBase entity, ItemStack wallet) { }

    @Override public WalletType getWalletType()                                             { return type; }
    @Override public int getWalletID()                                                      { return type.getID(); }

    @Nullable @Override public WalletType getLeakType()                                     { return leakType; }
    @Nullable @Override public WalletType getNormalType()                                   { return normalType; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        if (type.isLeaked() && normalType != null) {
            tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + "." + normalType.getTranslationKey() + ".desc"));
        } else {
            tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + "." + type.getTranslationKey() + ".desc"));
        }

        if (type.isLeaked()) {
            tooltip.add("");
            tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".leak_wallet.desc"));
        }

        tooltip.add("");

        if (Minecraft.getMinecraft().player != null) {
            if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
                //Получаем доступ к NBT pipeline
                ItemStackWalletNBTPipeline walletPipe = MouseProjectNBT.get(stack).getWalletPipe();

                //Загружаем все типы балансов
                List<CoinType> balanceTypes = walletPipe.loadAllBalanceTypes();

                tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.balance_title"));

                //Если балансов нет, показываем пустой кошелёк
                if (balanceTypes.isEmpty()) {
                    tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.empty"));
                    return;
                }

                //Строки для разных типов монет
                StringBuilder normalLine   = new StringBuilder();
                StringBuilder resourceLine = new StringBuilder();
                StringBuilder specificLine = new StringBuilder();
                StringBuilder otherLine    = new StringBuilder();

                for (CoinType balanceType : balanceTypes) {
                    CoinValue balance;
                    if (balanceType instanceof NormalCoinType) {
                        balance = walletPipe.loadBronzeBalance();
                        if (balance != null && balance.isPositive()) {
                            EnumMap<NormalCoinType, Long> displayCoins = CoinHelper.getDisplayCoins(
                                    CoinHelper.getMaxCoin(balance.getValue()), balance.getValue()
                            );
                            long remaining = CoinHelper.getDisplayRemainingBronze();
                            if (remaining > 0) {
                                displayCoins.merge(NormalCoinType.BRONZE, remaining, Long::sum);
                                if (normalLine.length() > 0) normalLine.append(" ");
                                appendCoinWithIcon(normalLine, NormalCoinType.BRONZE, remaining, this);
                            }
                            displayCoins.forEach((t, val) -> {
                                if (normalLine.length() > 0) normalLine.append(" ");
                                appendCoinWithIcon(normalLine, t, val, this);
                            });
                        }
                    } else if (balanceType instanceof ResourceCoinType) {
                        balance = walletPipe.loadResourceBalance(balanceType);
                        if (balance != null && balance.isPositive()) {
                            if (resourceLine.length() > 0) resourceLine.append(" ");
                            appendCoinWithIcon(resourceLine, balance.getType(), balance.getValue(), this);
                        }
                    } else if (balanceType instanceof SpecificCoinType) {
                        balance = walletPipe.loadSpecificBalance(balanceType);
                        if (balance != null && balance.isPositive()) {
                            if (specificLine.length() > 0) specificLine.append(" ");
                            appendCoinWithIcon(specificLine, balance.getType(), balance.getValue(), this);
                        }
                    } else if (balanceType != null) {
                        balance = walletPipe.loadOtherBalance(balanceType);
                        if (balance != null && balance.isPositive()) {
                            if (specificLine.length() > 0) specificLine.append(" ");
                            appendCoinWithIcon(otherLine, balance.getType(), balance.getValue(), this);
                        }
                    }
                }

                boolean hasAnyBal = false;

                //Добавляем строки с балансом, если есть содержимое
                if (normalLine.length() > 0) {
                    tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.normal"));
                    tooltip.add(normalLine.toString());
                    hasAnyBal = true;
                }
                if (resourceLine.length() > 0) {
                    tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.resource"));
                    tooltip.add(resourceLine.toString());
                    hasAnyBal = true;
                }
                if (specificLine.length() > 0) {
                    tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.specific"));
                    tooltip.add(specificLine.toString());
                    hasAnyBal = true;
                }
                if (otherLine.length() > 0) {
                    tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.other"));
                    tooltip.add(otherLine.toString());
                    hasAnyBal = true;
                }

                if (!hasAnyBal) tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.empty"));
            } else {
                String sneakKeyName = Keyboard.getKeyName(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
                tooltip.add(I18n.format("tooltip." + Tags.MOD_ID + ".wallet.show_balance", sneakKeyName));
            }
        }
    }

    //Вспомогательный метод для добавления иконки и количества с проверкой максимума
    private void appendCoinWithIcon(StringBuilder builder, CoinType type, long value, IWallet wallet) {
        Item coinItem = type.getItem();
        if (coinItem != null) {
            ResourceLocation name = coinItem.getRegistryName();
            if (name != null) {
                String    colorCode = "";
                CoinValue capacity  = wallet.getCapacity(type);
                if (value >= capacity.getValue()) colorCode = TextFormatting.DARK_RED.toString();//Красный при максимуме
                builder
                        .append("@").append(name).append("@")
                        .append(colorCode).append("x").append(value).append(TextFormatting.RESET);
            }
        }
    }
}