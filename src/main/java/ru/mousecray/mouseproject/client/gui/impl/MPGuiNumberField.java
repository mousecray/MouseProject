/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiNumberField extends MPGuiTextField<MPGuiNumberField> {
    private final Consumer<MPGuiTextTypedEvent<MPGuiNumberField>> onTextTyped;

    public MPGuiNumberField(
            FontRenderer fontRenderer, @Nullable MPGuiString placeholder,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiTextTypedEvent<MPGuiNumberField>> onTextTyped
    ) {
        super(fontRenderer, placeholder == null ? "" : placeholder.get(), "", elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 0)
                        .addTexture(GuiButtonActionState.PRESSED, 0)
                        .build()
                , null, fontSize);
        this.onTextTyped = onTextTyped;
        if (placeholder != null) setPlaceholder(placeholder);
    }

    public MPGuiNumberField(
            FontRenderer fontRenderer, @Nullable String placeholder,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiTextTypedEvent<MPGuiNumberField>> onTextTyped
    ) {
        super(fontRenderer, placeholder, "", elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 0)
                        .addTexture(GuiButtonActionState.PRESSED, 0)
                        .build()
                , null, fontSize);
        this.onTextTyped = onTextTyped;
    }

    @Override
    protected void onAnyEventFire(@Nonnull MPGuiEvent<MPGuiNumberField> event) {
        if (event instanceof MPGuiTextTypedEvent) {
            MPGuiTextTypedEvent<MPGuiNumberField> e       = (MPGuiTextTypedEvent<MPGuiNumberField>) event;
            String                                newText = e.getNewText();

            if (newText.isEmpty()) {
                if (onTextTyped != null) onTextTyped.accept(e);
                return;
            }

            if (!StringUtils.isNumeric(newText) || (newText.length() > 1 && newText.startsWith("0"))) {
                e.setCancelled(true);
            } else {
                if (onTextTyped != null) onTextTyped.accept(e);
            }
        }
    }

    public long getNumberText()           { return StringUtils.isEmpty(getText()) ? 0 : Long.parseLong(getText()); }
    public void setNumberText(long value) { setText(String.valueOf(value)); }
}