/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.event.MPGuiEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiNumberField extends MPGuiSimpleField {
    public MPGuiNumberField(
            GuiShape elementShape, @Nullable MPGuiString placeholder,
            FontRenderer fontRenderer, MPFontSize fontSize,
            Consumer<MPGuiTextTypedEvent<MPGuiSimpleField>> onTextTyped
    ) {
        super(fontRenderer, placeholder, null, elementShape, fontSize, onTextTyped);
        if (placeholder != null) setPlaceholder(placeholder);
    }

    public MPGuiNumberField(
            GuiShape elementShape, @Nullable String placeholder,
            FontRenderer fontRenderer, MPFontSize fontSize,
            Consumer<MPGuiTextTypedEvent<MPGuiSimpleField>> onTextTyped
    ) {
        super(fontRenderer, placeholder, null, elementShape, fontSize, onTextTyped);
    }

    @Override
    protected void onAnyEventFire(MPGuiEvent<MPGuiSimpleField> event) {
        if (event instanceof MPGuiTextTypedEvent) {
            MPGuiTextTypedEvent<MPGuiSimpleField> e       = (MPGuiTextTypedEvent<MPGuiSimpleField>) event;
            String                                newText = e.getNewText();

            if (newText == null || newText.isEmpty()) {
                super.onAnyEventFire(event);
                return;
            }

            if (!StringUtils.isNumeric(newText) || (newText.length() > 1 && newText.startsWith("0"))) {
                e.setCancelled(true);
            } else super.onAnyEventFire(event);
        }
    }

    public long getNumberText()           { return StringUtils.isEmpty(getText()) ? 0 : Long.parseLong(getText()); }
    public void setNumberText(long value) { setText(String.valueOf(value)); }
}