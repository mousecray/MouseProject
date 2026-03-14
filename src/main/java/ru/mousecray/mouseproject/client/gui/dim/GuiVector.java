/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.dim;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiVector implements IGuiVector {
    public static final GuiVector ZERO = new GuiVector(0f, 0f);

    private final float x;
    private final float y;

    public GuiVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public GuiVector(IGuiVector v)                            { this(v.x(), v.y()); }

    @Override public MutableGuiVector toMutable()             { return new MutableGuiVector(this); }
    @Override public GuiVector toImmutable()                  { return this; }

    @Override public float x()                                { return x; }
    @Override public float y()                                { return y; }

    @Override public GuiVector withX(float newX)              { return new GuiVector(newX, y); }
    @Override public GuiVector withY(float newY)              { return new GuiVector(x, newY); }
    @Override public IGuiVector withVector(IGuiVector newVec) { return new GuiVector(x, y); }
    @Override public GuiVector add(IGuiVector other)          { return new GuiVector(x + other.x(), y + other.y()); }
    @Override public GuiVector sub(IGuiVector other)          { return new GuiVector(x - other.x(), y - other.y()); }
    @Override public GuiVector mul(float scalar)              { return new GuiVector(x * scalar, y * scalar); }

    @Override
    public GuiVector div(float scalar) {
        if (scalar == 0f) throw new ArithmeticException("Division by zero");
        return new GuiVector(x / scalar, y / scalar);
    }

    @Override public GuiVector mul(IGuiVector other) { return new GuiVector(x * other.x(), y * other.y()); }

    @Override
    public GuiVector div(IGuiVector other) {
        if (other.x() == 0f || other.y() == 0f) throw new ArithmeticException("Division by zero");
        return new GuiVector(x / other.x(), y / other.y());
    }

    public GuiVector add(float dx, float dy) { return new GuiVector(x + dx, y + dy); }
    public GuiVector sub(float dx, float dy) { return new GuiVector(x - dx, y - dy); }

    @Override public GuiVector copy()        { return new GuiVector(x, y); }

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