/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.dim;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MPGuiPadding extends MPGuiMargin {
    public static MPGuiPadding ZERO()                                     { return new MPGuiPadding(0); }

    public MPGuiPadding(float all)                                        { super(all); }
    public MPGuiPadding(float horizontal, float vertical)                 { super(horizontal, vertical); }
    public MPGuiPadding(float left, float top, float right, float bottom) { super(left, top, right, bottom); }
}