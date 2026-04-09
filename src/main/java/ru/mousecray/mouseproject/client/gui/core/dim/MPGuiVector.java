/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.dim;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MPGuiVector implements IGuiVector {
    public static final MPGuiVector ZERO = new MPGuiVector(0f, 0f);

    public static MPGuiVector of(float x, float y) {
        if (x == 0f && y == 0f) return ZERO;
        return new MPGuiVector(x, y);
    }

    @SuppressWarnings("SuspiciousNameCombination") public static MPGuiVector of(float x) { return of(x, x); }

    private final float x;
    private final float y;

    private MPGuiVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public MPGuiVector(IGuiVector v)                          { this(v.x(), v.y()); }

    @Override public MutableGuiVector toMutable()             { return new MutableGuiVector(this); }
    @Override public MPGuiVector toImmutable()                { return this; }

    @Override public float x()                                { return x; }
    @Override public float y()                                { return y; }

    @Override public MPGuiVector withX(float newX)            { return MPGuiVector.of(newX, y); }
    @Override public MPGuiVector withY(float newY)            { return MPGuiVector.of(x, newY); }
    @Override public IGuiVector withVector(IGuiVector newVec) { return MPGuiVector.of(x, y); }
    @Override public MPGuiVector add(IGuiVector other)        { return MPGuiVector.of(x + other.x(), y + other.y()); }
    @Override public MPGuiVector sub(IGuiVector other)        { return MPGuiVector.of(x - other.x(), y - other.y()); }
    @Override public MPGuiVector mul(float scalar)            { return MPGuiVector.of(x * scalar, y * scalar); }

    @Override
    public MPGuiVector div(float scalar) {
        if (scalar == 0f) throw new ArithmeticException("Division by zero");
        return MPGuiVector.of(x / scalar, y / scalar);
    }

    @Override public MPGuiVector mul(IGuiVector other) { return MPGuiVector.of(x * other.x(), y * other.y()); }

    @Override
    public MPGuiVector div(IGuiVector other) {
        if (other.x() == 0f || other.y() == 0f) throw new ArithmeticException("Division by zero");
        return MPGuiVector.of(x / other.x(), y / other.y());
    }

    public MPGuiVector add(float dx, float dy) { return MPGuiVector.of(x + dx, y + dy); }
    public MPGuiVector sub(float dx, float dy) { return MPGuiVector.of(x - dx, y - dy); }

    @Override public MPGuiVector copy()        { return MPGuiVector.of(x, y); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IGuiVector)) return false;
        IGuiVector v = (IGuiVector) o;
        return Float.compare(v.x(), x) == 0 && Float.compare(v.y(), y) == 0;
    }

    @Override public int hashCode()    { return Float.floatToIntBits(x) * 31 + Float.floatToIntBits(y); }
    @Override public String toString() { return String.format("Vec(%.2f, %.2f)", x, y); }
}