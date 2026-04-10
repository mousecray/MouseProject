/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.dim;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public final class MPMutableGuiShape implements IGuiShape {
    @Nonnull private final MPMutableGuiVector pos, size;

    public MPMutableGuiShape() { this(0f, 0f, 0f, 0f); }

    public MPMutableGuiShape(float x, float y, float width, float height) {
        this(new MPMutableGuiVector(x, y), new MPMutableGuiVector(width, height));
    }

    public MPMutableGuiShape(IGuiShape shape) { this(shape.pos(), shape.size()); }

    public MPMutableGuiShape(IGuiVector pos, IGuiVector size) {
        this.pos = pos.toMutable();
        this.size = size.toMutable();
    }

    @Override public float x()                     { return pos.x(); }
    @Override public float y()                     { return pos.y(); }
    @Override public float width()                 { return size.x(); }
    @Override public float height()                { return size.y(); }

    @Override public MPMutableGuiShape toMutable() { return this; }
    @Override public MPGuiShape toImmutable()      { return new MPGuiShape(this); }

    @Override public MPMutableGuiVector pos()      { return pos; }
    @Override public MPMutableGuiVector size()     { return size; }

    @Override
    public MPMutableGuiShape withX(float x) {
        pos.withX(x);
        return this;
    }

    @Override
    public MPMutableGuiShape withY(float y) {
        pos.withY(y);
        return this;
    }

    @Override
    public MPMutableGuiShape withWidth(float w) {
        size.withX(w);
        return this;
    }

    @Override
    public MPMutableGuiShape withHeight(float h) {
        size.withY(h);
        return this;
    }

    @Override
    public MPMutableGuiShape withPos(IGuiVector p) {
        pos.withVector(p);
        return this;
    }

    @Override
    public MPMutableGuiShape withSize(IGuiVector s) {
        size.withVector(s);
        return this;
    }
    @Override
    public MPMutableGuiShape withShape(IGuiShape sh) {
        withPos(sh.pos());
        withSize(sh.size());
        return this;
    }

    @Override
    public MPMutableGuiShape offset(float dx, float dy) {
        pos.add(dx, dy);
        return this;
    }

    @Override
    public MPMutableGuiShape grow(float left, float top, float right, float bottom) {
        pos.add(-left, -top);
        size.add(right, bottom);
        return this;
    }

    @Override public MPMutableGuiShape copy() { return new MPMutableGuiShape(pos.copy(), size.copy()); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MPMutableGuiShape)) return false;
        MPMutableGuiShape that = (MPMutableGuiShape) o;
        return Objects.equals(pos, that.pos) && Objects.equals(size, that.size);
    }

    @Override public int hashCode() { return Objects.hash(pos, size); }

    @Override
    public String toString() {
        return String.format("MutShape[%.1f, %.1f, %.1f×%.1f]", pos.x(), pos.y(), size.x(), size.y());
    }
}