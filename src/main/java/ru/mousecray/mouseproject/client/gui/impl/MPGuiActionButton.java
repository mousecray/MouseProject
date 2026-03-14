/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiActionButton extends MPGuiButton<MPGuiActionButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiActionButton>> onClick;

    public MPGuiActionButton(
            @Nullable MPGuiString text,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiActionButton>> onClick) {
        super(
                text == null ? "" : text.get(), elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.DISABLED, 0)
                        .addTexture(GuiButtonPersistentState.NORMAL, 1)
                        .addTexture(GuiButtonActionState.HOVER, 2)
                        .addTexture(GuiButtonActionState.PRESSED, 3)
                        .addTexture(GuiButtonPersistentState.FAIL, 4)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
        if (text != null) setGuiString(text);
    }

    public MPGuiActionButton(
            @Nullable String text,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiActionButton>> onClick) {
        super(
                text, elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.DISABLED, 0)
                        .addTexture(GuiButtonPersistentState.NORMAL, 1)
                        .addTexture(GuiButtonActionState.HOVER, 2)
                        .addTexture(GuiButtonActionState.PRESSED, 3)
                        .addTexture(GuiButtonPersistentState.FAIL, 4)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<MPGuiActionButton> event) {
        if (onClick != null) onClick.accept(event);
    }
}
