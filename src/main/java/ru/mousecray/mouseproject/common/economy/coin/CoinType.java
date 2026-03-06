package ru.mousecray.mouseproject.common.economy.coin;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import ru.mousecray.mouseproject.common.economy.CoinValue;

import javax.annotation.Nullable;

public interface CoinType {
    Item getItem();
    int getID();
    String getTranslationKey();

    default void saveToNbt(NBTTagCompound nbt, CoinValue value) {
        nbt.setInteger("type", getID());
        nbt.setLong("value", value.getValue());
    }

    @Nullable
    static CoinValue loadFromNbt(NBTTagCompound nbt) {
        if (nbt.hasKey("type")) {
            CoinType type = fromID(nbt.getInteger("type"));
            return type != null ? CoinValue.create(nbt.getLong("value"), type) : null;
        }
        return null;
    }

    @Nullable
    static CoinType fromID(int id) {
        switch (id) {
            case 0:
                return NormalCoinType.BRONZE;
            case 1:
                return NormalCoinType.SILVER;
            case 2:
                return NormalCoinType.GOLD;
            case 3:
                return NormalCoinType.DIAMOND;
            case 4:
                return NormalCoinType.EMERALD;
            case 5:
                return NormalCoinType.RUBY;
            case 6:
                return NormalCoinType.AMETHYST;
            case 7:
                return SpecificCoinType.MYTHIC;
            case 8:
                return ResourceCoinType.WOOL;
            case 9:
                return ResourceCoinType.WOOD;
            case 10:
                return ResourceCoinType.COAL;
            case 11:
                return ResourceCoinType.STONE;
            case 12:
                return ResourceCoinType.LAPIS;
            case 13:
                return ResourceCoinType.REDSTONE;
            case 14:
                return ResourceCoinType.OBSIDIAN;
            case 15:
                return ResourceCoinType.NETHERITE;
            default:
                return null;
        }
    }
}