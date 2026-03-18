/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.misc.state;

public enum MPGuiElementState {
    HOVERED, PRESSED, FOCUSED, //Интерактивные
    SELECTED, FAIL,            //Постоянные
    DISABLED;                  //Глобальный модификатор

    public final int mask = 1 << ordinal();
}