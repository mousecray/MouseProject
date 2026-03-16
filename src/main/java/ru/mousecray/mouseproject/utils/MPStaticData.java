/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.utils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;

public class MPStaticData {
    @SideOnly(Side.CLIENT)
    public static final ResourceLocation CONTROLS_TEXTURES      = new ResourceLocation(Tags.MOD_ID, "textures/gui/controls.png");
    @SideOnly(Side.CLIENT)
    public static final GuiVector        CONTROLS_TEXTURES_SIZE = GuiVector.of(256);

    private MPStaticData() { throw new UnsupportedOperationException("Cannot create utility class"); }
}