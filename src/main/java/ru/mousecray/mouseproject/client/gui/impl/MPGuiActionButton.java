/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiActionButton extends MPGuiButton<MPGuiActionButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiActionButton>> onClick;

    public MPGuiActionButton(
            GuiShape elementShape, @Nullable MPGuiString text, MPFontSize fontSize,
            Consumer<MPGuiMouseClickEvent<MPGuiActionButton>> onClick
    ) {
        super(
                text == null ? "" : text.get(), elementShape,
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.ZERO, GuiVector.of(80, 10)
                        )
                        .addTexture(GuiElementPersistentState.DISABLED, 0)
                        .addTexture(GuiElementPersistentState.NORMAL, 1)
                        .addTexture(GuiElementActionState.HOVER, 2)
                        .addTexture(GuiElementActionState.PRESSED, 3)
                        .addTexture(GuiElementPersistentState.FAIL, 4)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
        if (text != null) setGuiString(text);
    }

    public MPGuiActionButton(
            GuiShape elementShape, @Nullable String text,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiActionButton>> onClick
    ) {
        super(
                text, elementShape,
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.ZERO, GuiVector.of(80, 10)
                        )
                        .addTexture(GuiElementPersistentState.DISABLED, 0)
                        .addTexture(GuiElementPersistentState.NORMAL, 1)
                        .addTexture(GuiElementActionState.HOVER, 2)
                        .addTexture(GuiElementActionState.PRESSED, 3)
                        .addTexture(GuiElementPersistentState.FAIL, 4)
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
