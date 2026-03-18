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
                        .addTexture(GuiElementPersistentState.NORMAL, 0)
                        .addTexture(GuiElementActionState.HOVER, 1)
                        .addTexture(GuiElementActionState.PRESSED, 2)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.HOVER), 1)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.PRESSED), 2)
                        .addTexture(GuiElementPersistentState.SELECTED, 3)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.HOVER), 4)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.PRESSED), 5)
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
                        .addTexture(GuiElementPersistentState.NORMAL, 0)
                        .addTexture(GuiElementActionState.HOVER, 1)
                        .addTexture(GuiElementActionState.PRESSED, 2)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.HOVER), 1)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.PRESSED), 2)
                        .addTexture(GuiElementPersistentState.SELECTED, 3)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.HOVER), 4)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<T> event) {
        applyState(
                getPersistentState() == GuiElementPersistentState.SELECTED
                        ? GuiElementPersistentState.NORMAL : GuiElementPersistentState.SELECTED
        );
        if (onClick != null) onClick.accept(event);
    }
}
