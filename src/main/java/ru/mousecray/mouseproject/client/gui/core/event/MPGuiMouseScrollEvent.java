/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.core.misc.MPScrollDirection;

@SideOnly(Side.CLIENT)
public class MPGuiMouseScrollEvent<T extends MPGuiElement<T>> extends MPGuiMouseEvent<T> {
    private MPScrollDirection scrollDirection;
    private int               scrollAmount;

    void setScrollDirection(MPScrollDirection scrollDirection) { this.scrollDirection = scrollDirection; }
    public MPScrollDirection getScrollDirection()              { return scrollDirection; }

    void setScrollAmount(int scrollAmount)                     { this.scrollAmount = scrollAmount; }
    public int getScrollAmount()                               { return scrollAmount; }
}