/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiLabel;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MPGuiStaticLabel extends MPGuiLabel<MPGuiStaticLabel> {
    public MPGuiStaticLabel(MPGuiString text, FontRenderer fontRenderer, MPGuiShape elementShape, int color, MPFontSize fontSize, @Nullable SoundEvent soundClick) {
        super(text.get(), fontRenderer, elementShape, color, fontSize, soundClick);
        setGuiString(text);
    }

    public MPGuiStaticLabel(String text, FontRenderer fontRenderer, MPGuiShape elementShape, int color, MPFontSize fontSize, @Nullable SoundEvent soundClick) {
        super(text, fontRenderer, elementShape, color, fontSize, soundClick);
    }

    public MPGuiStaticLabel(MPGuiString text, FontRenderer fontRenderer, MPGuiShape elementShape, int color, MPFontSize fontSize) {
        this(text, fontRenderer, elementShape, color, fontSize, null);
    }

    public MPGuiStaticLabel(String text, FontRenderer fontRenderer, MPGuiShape elementShape, int color, MPFontSize fontSize) {
        this(text, fontRenderer, elementShape, color, fontSize, null);
    }

    @Override public void onClick(MPGuiMouseClickEvent<MPGuiStaticLabel> event) { }
}