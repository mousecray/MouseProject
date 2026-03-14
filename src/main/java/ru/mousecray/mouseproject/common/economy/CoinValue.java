/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.economy;

import net.minecraft.nbt.NBTTagCompound;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;

import javax.annotation.Nullable;

public class CoinValue {
    private       long     value;
    private final CoinType type;

    private CoinValue(Number value, CoinType type) {
        this.value = value.longValue();
        this.type = type;
    }

    private CoinValue(CoinType type) {
        this.type = type;
    }

    public static CoinValue create(Number value, CoinType type) { return new CoinValue(value.longValue(), type); }
    public static CoinValue createSingle(CoinType type)         { return new CoinValue(1, type); }
    public static CoinValue createEmpty(CoinType type)          { return new CoinValue(type); }

    public void reset()                                         { value = 0; }

    public long getValue()                                      { return value; }
    public CoinType getType()                                   { return type; }

    public void setValue(Number value)                          { this.value = value.longValue(); }

    public double getAsDouble()                                 { return value; }
    public int getAsInt()                                       { return (int) value; }
    public float getAsFloat()                                   { return value; }
    public byte getAsByte()                                     { return (byte) value; }
    public short getAsShort()                                   { return (short) value; }

    public boolean isLess(CoinValue value)                      { return this.value < value.value; }
    public boolean isMore(CoinValue value)                      { return this.value > value.value; }
    public boolean isEqual(CoinValue value)                     { return this.value == value.value; }
    public boolean isLessOrEqual(CoinValue value)               { return this.value <= value.value; }
    public boolean isMoreOrEqual(CoinValue value)               { return this.value >= value.value; }

    public boolean isPositive()                                 { return value > 0; }
    public boolean isNull()                                     { return value == 0; }
    public boolean isPositiveOrNull()                           { return value >= 0; }
    public boolean isNegativeOrNull()                           { return value <= 0; }

    public CoinValue plus(CoinValue value)                      { return create(this.value + value.value, type); }
    public CoinValue minus(CoinValue value)                     { return create(this.value - value.value, type); }
    public CoinValue multiply(CoinValue value)                  { return create(this.value * value.value, type); }
    public CoinValue divide(CoinValue value)                    { return create(this.value / value.value, type); }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        getType().saveToNbt(nbt, this);
        return nbt;
    }

    @Nullable
    public static CoinValue fromNBT(NBTTagCompound nbt) {
        return CoinType.loadFromNbt(nbt);
    }

    public CoinValue copy() { return create(value, type); }

    public String getFormattedValue(FormatType format) {
        switch (format) {
            case SHORT:
                return CoinHelper.formatBalanceShort(value);
            default:
            case NORMAL:
                return CoinHelper.formatBalanceNormal(value);
            case LONG:
                return CoinHelper.formatBalanceLong(value);
        }
    }

    @Override public String toString() { return "x" + CoinHelper.formatBalanceNormal(value); }

    public enum FormatType {
        SHORT, NORMAL, LONG
    }
}