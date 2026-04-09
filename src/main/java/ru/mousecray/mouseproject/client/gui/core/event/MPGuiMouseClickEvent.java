/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.core.misc.MPClickType;

@SideOnly(Side.CLIENT)
public class MPGuiMouseClickEvent<T extends MPGuiElement<T>> extends MPGuiMouseEvent<T> {
    private final MPClickType clickType;

    public MPGuiMouseClickEvent(MPClickType clickType) { this.clickType = clickType; }
    public MPClickType getClickType()                  { return clickType; }
}