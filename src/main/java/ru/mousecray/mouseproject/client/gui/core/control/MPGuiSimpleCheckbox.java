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
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseCheckbox;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.*;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES_SIZE;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiSimpleCheckbox extends MPGuiBaseCheckbox<MPGuiSimpleCheckbox> {
    public MPGuiSimpleCheckbox(MPGuiShape shape, MPGuiString text, FontRenderer fontRenderer) {
        super(shape, text, fontRenderer);
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        CONTROLS_TEXTURES, CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(184, 0), MPGuiVector.of(8)
                )
                .addTexture(0)
                .addTexture(1, HOVERED)
                .addTexture(2, PRESSED)
                .addTexture(3, SELECTED)
                .addTexture(4, SELECTED, HOVERED)
                .addTexture(5, SELECTED, PRESSED)
                .build());
        colorPack = MPGuiColorPack.Builder
                .create(14737632)
                .addColor(10526880, DISABLED)
                .addColor(14737632)
                .addColor(15592941, HOVERED)
                .addColor(13948116, PRESSED)
                .addColor(14737632, SELECTED)
                .addColor(15592941, SELECTED, HOVERED)
                .addColor(13948116, SELECTED, PRESSED)
                .build();
    }
}
