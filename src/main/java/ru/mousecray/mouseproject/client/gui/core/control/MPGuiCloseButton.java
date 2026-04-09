/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.GuiScaleRules;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleType;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiCloseButton extends MPGuiButton<MPGuiCloseButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiCloseButton>> onClick;

    public MPGuiCloseButton(MPGuiShape elementShape, Consumer<MPGuiMouseClickEvent<MPGuiCloseButton>> onClick) {
        super(
                null, elementShape,
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                MPGuiVector.of(95, 0), MPGuiVector.of(9)
                        )
                        .addTexture(GuiElementPersistentState.NORMAL, 0)
                        .addTexture(GuiElementActionState.HOVER, 1)
                        .addTexture(GuiElementActionState.PRESSED, 2)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL
        );
        this.onClick = onClick;
        setScaleRules(new GuiScaleRules(MPGuiScaleType.ORIGIN_VERTICAL));
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<MPGuiCloseButton> event) {
        if (onClick != null) onClick.accept(event);
    }
}
