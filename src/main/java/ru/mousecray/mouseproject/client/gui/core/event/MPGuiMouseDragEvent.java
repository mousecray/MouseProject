/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;

@SideOnly(Side.CLIENT)
public class MPGuiMouseDragEvent<T extends MPGuiElement<T>> extends MPGuiMouseMoveEvent<T> {
    private int diffX, diffY;
    private int tickDown;

    void setDiffX(int diffX)       { this.diffX = diffX; }
    void setDiffY(int diffY)       { this.diffY = diffY; }
    void setTickDown(int tickDown) { this.tickDown = tickDown; }

    public int getDiffX()          { return diffX; }
    public int getDiffY()          { return diffY; }
    public int getTickDown()       { return tickDown; }
}