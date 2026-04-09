/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.misc;

import javax.annotation.Nullable;

public enum MPScrollDirection {
    UP,
    DOWN;

    @Nullable
    public static MPScrollDirection getScrollDirection(int scrollAmount) {
        return scrollAmount < 0 ? UP : scrollAmount > 0 ? DOWN : null;
    }
}