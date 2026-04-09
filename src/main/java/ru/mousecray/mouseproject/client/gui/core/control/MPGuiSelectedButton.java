/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.IGuiVector;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public abstract class MPGuiSelectedButton<T extends MPGuiSelectedButton<T>> extends MPGuiButton<T> {
    private final Consumer<MPGuiMouseClickEvent<T>> onClick;

    public MPGuiSelectedButton(
            MPGuiShape elementShape,
            @Nullable MPGuiString text,
            ResourceLocation texture, IGuiVector textureSize, MPGuiShape textureShape,
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
            MPGuiShape elementShape,
            @Nullable String text,
            ResourceLocation texture, IGuiVector textureSize, MPGuiShape textureShape,
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
