/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.container;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiScrollPanel;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiSimpleScrollPanel extends MPGuiScrollPanel<MPGuiSimpleScrollPanel> {
    public MPGuiSimpleScrollPanel(MPGuiShape shape) { super(shape); }
}