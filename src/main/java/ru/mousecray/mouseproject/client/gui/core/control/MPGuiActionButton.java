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
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.*;
import static ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector.ZERO;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES_SIZE;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiActionButton extends MPGuiBaseButton<MPGuiActionButton> {
    public MPGuiActionButton(
            MPGuiShape shape, MPGuiString text
    ) {
        super(shape, text);
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        CONTROLS_TEXTURES, CONTROLS_TEXTURES_SIZE,
                        ZERO, MPGuiVector.of(80, 10)
                )
                .addTexture(0, DISABLED)
                .addTexture(1)
                .addTexture(2, HOVERED)
                .addTexture(3, PRESSED)
                .addTexture(4, FAIL)
                .build());
    }
}
