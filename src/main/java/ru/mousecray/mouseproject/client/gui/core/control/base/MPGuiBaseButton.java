/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control.base;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiBaseButton<T extends MPGuiBaseButton<T>> extends MPGuiButton<T> {
    private Consumer<MPGuiMouseClickEvent<T>> onClickListener;

    public MPGuiBaseButton(MPGuiShape shape, MPGuiString text) {
        super(shape);
        setGuiString(text);
    }

    public void setOnClickListener(@Nullable Consumer<MPGuiMouseClickEvent<T>> listener) { onClickListener = listener; }
    public Consumer<MPGuiMouseClickEvent<T>> getOnClickListener()                        { return onClickListener; }

    @Override
    public void onClick(MPGuiMouseClickEvent<T> event) {
        if (onClickListener != null) onClickListener.accept(event);
    }
}