/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseButton;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleRules;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.HOVERED;
import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.PRESSED;
import static ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleType.ORIGIN_VERTICAL;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES_SIZE;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiCloseButton extends MPGuiBaseButton<MPGuiCloseButton> {
    public MPGuiCloseButton(MPGuiShape shape) {
        super(shape, MPGuiString.EMPTY());
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        CONTROLS_TEXTURES, CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(95, 0), MPGuiVector.of(9)
                )
                .addTexture(0)
                .addTexture(1, HOVERED)
                .addTexture(2, PRESSED)
                .build());
        setScaleRules(new MPGuiScaleRules(ORIGIN_VERTICAL));
    }
}