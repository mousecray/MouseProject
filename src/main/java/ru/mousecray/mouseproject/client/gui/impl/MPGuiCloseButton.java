/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.dim.GuiScaleRules;
import ru.mousecray.mouseproject.client.gui.dim.GuiScaleType;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiCloseButton extends MPGuiButton<MPGuiCloseButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiCloseButton>> onClick;

    public MPGuiCloseButton(GuiShape elementShape, Consumer<MPGuiMouseClickEvent<MPGuiCloseButton>> onClick) {
        super(
                null, elementShape,
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.of(95, 0), GuiVector.of(9)
                        )
                        .addTexture(GuiElementPersistentState.NORMAL, 0)
                        .addTexture(GuiElementActionState.HOVER, 1)
                        .addTexture(GuiElementActionState.PRESSED, 2)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL
        );
        this.onClick = onClick;
        setScaleRules(new GuiScaleRules(GuiScaleType.ORIGIN_VERTICAL));
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<MPGuiCloseButton> event) {
        if (onClick != null) onClick.accept(event);
    }
}
