/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;

@SideOnly(Side.CLIENT)
public class MPGuiTickEvent<T extends MPGuiElement<T>> extends MPGuiEvent<T> {
    private float partialTick;

    void setPartialTick(float partialTick) { this.partialTick = partialTick; }
    public float getPartialTick()          { return partialTick; }
}