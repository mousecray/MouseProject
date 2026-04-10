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
public final class MPGuiShape implements IGuiShape {
    public static final MPGuiShape ZERO = new MPGuiShape(0f, 0f, 0f, 0f);

    @Nonnull private final MPGuiVector pos, size;

    public MPGuiShape(float x, float y, float width, float height) {
        this(MPGuiVector.of(x, y), MPGuiVector.of(width, height));
    }

    public MPGuiShape(IGuiVector pos, IGuiVector size) {
        this.pos = pos.toImmutable();
        this.size = size.toImmutable();
    }

    public MPGuiShape(IGuiShape shape)                     { this(shape.pos(), shape.size()); }

    @Override public float x()                             { return pos.x(); }
    @Override public float y()                             { return pos.y(); }
    @Override public float width()                         { return size.x(); }
    @Override public float height()                        { return size.y(); }

    @Override public MPGuiVector pos()                     { return pos; }
    @Override public MPGuiVector size()                    { return size; }

    @Override public MPMutableGuiShape toMutable()         { return new MPMutableGuiShape(this); }
    @Override public MPGuiShape toImmutable()              { return this; }

    @Override public MPGuiShape withX(float newX)          { return new MPGuiShape(newX, pos.y(), size.x(), size.y()); }
    @Override public MPGuiShape withY(float newY)          { return new MPGuiShape(pos.x(), newY, size.x(), size.y()); }
    @Override public MPGuiShape withWidth(float w)         { return new MPGuiShape(pos.x(), pos.y(), w, size.y()); }
    @Override public MPGuiShape withHeight(float h)        { return new MPGuiShape(pos.x(), pos.y(), size.x(), h); }
    @Override public MPGuiShape withPos(IGuiVector p)      { return new MPGuiShape(p.x(), p.y(), size.x(), size.y()); }
    @Override public MPGuiShape withSize(IGuiVector s)     { return new MPGuiShape(pos.x(), pos.y(), s.x(), s.y()); }
    @Override public IGuiShape withShape(IGuiShape shape)  { return new MPGuiShape(this); }
    @Override public MPGuiShape offset(float dx, float dy) { return new MPGuiShape(pos.x() + dx, pos.y() + dy, size.x(), size.y()); }

    @Override
    public MPGuiShape grow(float left, float top, float right, float bottom) {
        return new MPGuiShape(pos.x() - left, pos.y() - top, size.x() + left + right, size.y() + top + bottom);
    }

    @Override public MPGuiShape copy() { return new MPGuiShape(pos.copy(), size.copy()); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MPGuiShape)) return false;
        MPGuiShape guiShape = (MPGuiShape) o;
        return Objects.equals(pos, guiShape.pos) && Objects.equals(size, guiShape.size);
    }

    @Override public int hashCode()    { return Objects.hash(pos, size); }
    @Override public String toString() { return String.format("Shape[%.1f, %.1f, %.1f×%.1f]", pos.x(), pos.y(), size.x(), size.y()); }
}