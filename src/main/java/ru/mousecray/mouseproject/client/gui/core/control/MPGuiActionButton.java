/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseButton;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiActionButton extends MPGuiBaseButton<MPGuiActionButton> {
    public MPGuiActionButton(
            MPGuiShape shape, MPGuiString text
    ) {
        super(shape, text);
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.ZERO, MPGuiVector.of(80, 10)
                )
                .addTexture(0, MPGuiElementState.DISABLED)
                .addTexture(1)
                .addTexture(2, MPGuiElementState.HOVERED)
                .addTexture(3, MPGuiElementState.PRESSED)
                .addTexture(4, MPGuiElementState.FAIL)
                .build());
    }
}
