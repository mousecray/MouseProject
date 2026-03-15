/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.dim;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPadding extends GuiMargin {
    public GuiPadding(float all)                                        { super(all); }
    public GuiPadding(float horizontal, float vertical)                 { super(horizontal, vertical); }
    public GuiPadding(float left, float top, float right, float bottom) { super(left, top, right, bottom); }
}