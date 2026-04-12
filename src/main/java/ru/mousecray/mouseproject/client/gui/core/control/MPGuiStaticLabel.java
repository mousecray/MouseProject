/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiLabel;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;

@SideOnly(Side.CLIENT)
public class MPGuiStaticLabel extends MPGuiLabel<MPGuiStaticLabel> {
    public MPGuiStaticLabel(MPGuiString text, MPGuiShape shape) {
        super(shape);
        setGuiString(text);
    }

    public MPGuiStaticLabel(String text, MPGuiShape shape) { this(MPGuiString.simple(text), shape); }
}