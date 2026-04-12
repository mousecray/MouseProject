/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTextureScaleRules;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTextureScaleType;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseSlider;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.dim.MPOrientation;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;

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
                ? new MPGuiTextureScaleRules(MPGuiTextureScaleType.FILL_VERTICAL, MPGuiTextureScaleType.SINGLE_HORIZONTAL_LEFT)
                .setMultipliers(0.7f, 0.5f)
                : new MPGuiTextureScaleRules(MPGuiTextureScaleType.FILL_HORIZONTAL, MPGuiTextureScaleType.SINGLE_VERTICAL_TOP)
                .setMultipliers(0.5f, 0.7f);

        setTrackTexturePack(MPGuiTexturePack.Builder.create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(230, 0), MPGuiVector.of(18, 7))
                .setScaleRules(trackScaleRules)
                .addTexture(0)
                .build());

        setKnobTexturePack(MPGuiTexturePack.Builder.create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(90, 0), MPGuiVector.of(5, 7)
                )
                .addTexture(0)
                .addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .build());
    }
}