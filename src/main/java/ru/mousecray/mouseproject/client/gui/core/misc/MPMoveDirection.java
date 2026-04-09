/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.misc;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public enum MPMoveDirection {
    HORIZONTAL_RIGHT,
    HORIZONTAL_LEFT,
    VERTICAL_UP,
    VERTICAL_DOWN,
    DIAGONAL_RIGHT,
    DIAGONAL_LEFT;

    @Nullable
    public static MPMoveDirection getMoveDirection(int diffX, int diffY) {
        final int THRESHOLD = 1;
        if (Math.abs(diffX) < THRESHOLD && Math.abs(diffY) < THRESHOLD) return null;
        MPMoveDirection direction = null;
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (diffX > 0) direction = MPMoveDirection.HORIZONTAL_RIGHT;
            else if (diffX < 0) direction = MPMoveDirection.HORIZONTAL_LEFT;
        } else if (Math.abs(diffY) > Math.abs(diffX)) {
            if (diffY > 0) direction = MPMoveDirection.VERTICAL_DOWN;
            else if (diffY < 0) direction = MPMoveDirection.VERTICAL_UP;
        } else if (diffX != 0 && diffY != 0) {
            if (diffX > 0) direction = MPMoveDirection.DIAGONAL_RIGHT;
            else direction = MPMoveDirection.DIAGONAL_LEFT;
        }
        return direction;
    }
}