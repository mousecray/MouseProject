/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control.base;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public abstract class MPGuiSelectedButton<T extends MPGuiSelectedButton<T>> extends MPGuiBaseButton<T> {
    public MPGuiSelectedButton(MPGuiShape shape, MPGuiString text) {
        super(shape, text);
    }

    @Override
    public void onClick(MPGuiMouseClickEvent<T> event) {
        if (stateManager.has(MPGuiElementState.SELECTED)) stateManager.remove(MPGuiElementState.SELECTED);
        else stateManager.add(MPGuiElementState.SELECTED);
        super.onClick(event);
    }
}