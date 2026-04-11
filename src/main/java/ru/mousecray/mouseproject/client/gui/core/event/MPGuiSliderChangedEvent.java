/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;

@SideOnly(Side.CLIENT)
public class MPGuiSliderChangedEvent<T extends MPGuiElement<T>> extends MPGuiEvent<T> {
    private int oldValue;
    private int newValue;

    void setOldValue(int oldValue) { this.oldValue = oldValue; }
    void setNewValue(int newValue) { this.newValue = newValue; }

    public int getOldValue()       { return oldValue; }
    public int getNewValue()       { return newValue; }
}