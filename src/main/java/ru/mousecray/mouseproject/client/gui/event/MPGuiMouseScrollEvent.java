/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.misc.ScrollDirection;

@SideOnly(Side.CLIENT)
public class MPGuiMouseScrollEvent<T extends MPGuiElement<T>> extends MPGuiMouseEvent<T> {
    private ScrollDirection scrollDirection;
    private int             scrollAmount;

    void setScrollDirection(ScrollDirection scrollDirection) { this.scrollDirection = scrollDirection; }
    public ScrollDirection getScrollDirection()              { return scrollDirection; }

    void setScrollAmount(int scrollAmount)                   { this.scrollAmount = scrollAmount; }
    public int getScrollAmount()                             { return scrollAmount; }
}