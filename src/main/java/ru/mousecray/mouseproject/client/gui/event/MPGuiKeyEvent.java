/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;

@SideOnly(Side.CLIENT)
public class MPGuiKeyEvent<T extends MPGuiElement<T>> extends MPGuiEvent<T> {
    private char typedChar;
    private int  keyCode;

    public void setKeyData(char typedChar, int keyCode) {
        this.typedChar = typedChar;
        this.keyCode = keyCode;
    }

    public char getTypedChar() { return typedChar; }
    public int getKeyCode()    { return keyCode; }
}