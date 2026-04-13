/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control.base;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.SELECTED;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public abstract class MPGuiSelectableButton<T extends MPGuiSelectableButton<T>> extends MPGuiBaseButton<T> {
    public MPGuiSelectableButton(MPGuiShape shape, MPGuiString text) {
        super(shape, text);
    }

    @Override
    public void onClick(MPGuiMouseClickEvent<T> event) {
        if (stateManager.has(SELECTED)) stateManager.remove(SELECTED);
        else stateManager.add(SELECTED);
        super.onClick(event);
    }
}