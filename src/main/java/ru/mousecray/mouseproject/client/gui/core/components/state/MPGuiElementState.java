/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.components.state;

public enum MPGuiElementState {
    HOVERED,
    PRESSED,
    FOCUSED,
    SELECTED,
    FAIL,
    DISABLED,
    HIDDEN;

    public final int mask = 1 << ordinal();
}