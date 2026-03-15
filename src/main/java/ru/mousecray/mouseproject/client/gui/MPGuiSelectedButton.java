/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.IGuiVector;
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
public abstract class MPGuiSelectedButton<T extends MPGuiSelectedButton<T>> extends MPGuiButton<T> {
    private final Consumer<MPGuiMouseClickEvent<T>> onClick;

    public MPGuiSelectedButton(
            GuiShape elementShape,
            @Nullable MPGuiString text,
            ResourceLocation texture, IGuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<T>> onClick) {
        super(
                text == null ? "" : text.get(), elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 1)
                        .addTexture(GuiButtonActionState.PRESSED, 2)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.HOVER), 1)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.PRESSED), 2)
                        .addTexture(GuiButtonPersistentState.SELECTED, 3)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.HOVER), 4)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
        if (text != null) setGuiString(text);
    }

    public MPGuiSelectedButton(
            GuiShape elementShape,
            @Nullable String text,
            ResourceLocation texture, IGuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<T>> onClick) {
        super(
                text, elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 1)
                        .addTexture(GuiButtonActionState.PRESSED, 2)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.HOVER), 1)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.PRESSED), 2)
                        .addTexture(GuiButtonPersistentState.SELECTED, 3)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.HOVER), 4)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<T> event) {
        applyState(
                getPersistentState() == GuiButtonPersistentState.SELECTED
                        ? GuiButtonPersistentState.NORMAL : GuiButtonPersistentState.SELECTED
        );
        if (onClick != null) onClick.accept(event);
    }
}
