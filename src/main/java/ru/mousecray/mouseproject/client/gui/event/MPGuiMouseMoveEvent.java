/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;

@SideOnly(Side.CLIENT)
public class MPGuiMouseMoveEvent<T extends MPGuiElement<T>> extends MPGuiMouseEvent<T> {
    private MoveDirection moveDirection;

    void setMoveDirection(MoveDirection moveDirection) { this.moveDirection = moveDirection; }
    public MoveDirection getMoveDirection()            { return moveDirection; }
}