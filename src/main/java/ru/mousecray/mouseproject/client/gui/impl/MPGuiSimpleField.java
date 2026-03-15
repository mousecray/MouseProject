/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiTextField;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiSimpleField extends MPGuiTextField<MPGuiSimpleField> {
    private final Consumer<MPGuiTextTypedEvent<MPGuiSimpleField>> onTextTyped;

    public MPGuiSimpleField(
            FontRenderer fontRenderer, @Nullable MPGuiString placeholder, @Nullable MPGuiString defaultText,
            GuiShape elementShape,
            MPFontSize fontSize, Consumer<MPGuiTextTypedEvent<MPGuiSimpleField>> onTextTyped
    ) {
        super(fontRenderer,
                placeholder == null ? "" : placeholder.get(), defaultText == null ? "" : defaultText.get(),
                elementShape,
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.of(104, 0), GuiVector.of(80, 10)
                        )
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 0)
                        .addTexture(GuiButtonActionState.PRESSED, 0)
                        .build(),
                null, fontSize);
        this.onTextTyped = onTextTyped;
        if (placeholder != null) setPlaceholder(placeholder);
    }

    public MPGuiSimpleField(
            FontRenderer fontRenderer, @Nullable String placeholder, @Nullable String defaultText,
            GuiShape elementShape,
            MPFontSize fontSize, Consumer<MPGuiTextTypedEvent<MPGuiSimpleField>> onTextTyped
    ) {
        super(fontRenderer, placeholder, defaultText, elementShape,
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.of(104, 0), GuiVector.of(80, 10)
                        )
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 0)
                        .addTexture(GuiButtonActionState.PRESSED, 0)
                        .build()
                , null, fontSize);
        this.onTextTyped = onTextTyped;
    }

    @Override
    protected void onAnyEventFire(MPGuiEvent<MPGuiSimpleField> event) {
        if (event instanceof MPGuiTextTypedEvent) {
            MPGuiTextTypedEvent<MPGuiSimpleField> e = (MPGuiTextTypedEvent<MPGuiSimpleField>) event;

            if (onTextTyped != null) onTextTyped.accept(e);
        }
    }
}