/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseCheckbox;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiSimpleCheckbox extends MPGuiBaseCheckbox<MPGuiSimpleCheckbox> {
    public MPGuiSimpleCheckbox(MPGuiShape shape, MPGuiString text, FontRenderer fontRenderer) {
        super(shape, text, fontRenderer);
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(184, 0), MPGuiVector.of(8)
                )
                .addTexture(0)
                .addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .addTexture(3, MPGuiElementState.SELECTED)
                .addTexture(4, MPGuiElementState.SELECTED, MPGuiElementState.HOVERED)
                .addTexture(5, MPGuiElementState.SELECTED, MPGuiElementState.PRESSED)
                .build());
        colorPack = MPGuiColorPack.Builder
                .create(14737632)
                .addColor(10526880, MPGuiElementState.DISABLED)
                .addColor(14737632)
                .addColor(15592941, MPGuiElementState.HOVERED)
                .addColor(13948116, MPGuiElementState.PRESSED)
                .addColor(14737632, MPGuiElementState.SELECTED)
                .addColor(15592941, MPGuiElementState.SELECTED, MPGuiElementState.HOVERED)
                .addColor(13948116, MPGuiElementState.SELECTED, MPGuiElementState.PRESSED)
                .build();
    }
}
