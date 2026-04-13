/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTextureScaleRules;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseSlider;
import ru.mousecray.mouseproject.client.gui.core.dim.IGuiVector;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.dim.MPOrientation;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.HOVERED;
import static ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState.PRESSED;
import static ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTextureScaleType.*;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES;
import static ru.mousecray.mouseproject.utils.MPStaticData.CONTROLS_TEXTURES_SIZE;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiSimpleSlider extends MPGuiBaseSlider<MPGuiSimpleSlider> {

    public MPGuiSimpleSlider(MPGuiShape shape, float knobWidth, float knobHeight, int min, int max, MPOrientation orientation) {
        super(shape, knobWidth, knobHeight, min, max, orientation);
        updateTextures();
    }

    @Override
    protected void onOrientationChanged() {
        super.onOrientationChanged();
        updateTextures();
    }

    private void updateTextures() {
        boolean isVert = getOrientation() == MPOrientation.VERTICAL;

        MPGuiTextureScaleRules trackScaleRules = isVert
                ? new MPGuiTextureScaleRules(FILL_VERTICAL, SINGLE_HORIZONTAL_LEFT).setMultipliers(0.7f, 0.5f)
                : new MPGuiTextureScaleRules(FILL_HORIZONTAL, SINGLE_VERTICAL_TOP).setMultipliers(0.5f, 0.7f);

        IGuiVector trackPos  = isVert ? MPGuiVector.of(230, 8) : MPGuiVector.of(230, 0);
        IGuiVector trackSize = isVert ? MPGuiVector.of(7, 18) : MPGuiVector.of(18, 7);

        IGuiVector knobPos  = isVert ? MPGuiVector.of(90, 22) : MPGuiVector.of(90, 0);
        IGuiVector knobSize = isVert ? MPGuiVector.of(5) : MPGuiVector.of(5, 7);

        setTrackTexturePack(MPGuiTexturePack.Builder
                .create(CONTROLS_TEXTURES, CONTROLS_TEXTURES_SIZE, trackPos, trackSize)
                .setScaleRules(trackScaleRules)
                .addTexture(0)
                .build()
        );

        setKnobTexturePack(MPGuiTexturePack.Builder
                .create(CONTROLS_TEXTURES, CONTROLS_TEXTURES_SIZE, knobPos, knobSize)
                .addTexture(0)
                .addTexture(1, HOVERED)
                .addTexture(2, PRESSED)
                .build()
        );
    }
}