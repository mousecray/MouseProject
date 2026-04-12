/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.core.component.MPGuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.core.component.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiSelectedButton;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleRules;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleType;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class MPGuiCheckButton extends MPGuiSelectedButton<MPGuiCheckButton> {
    private final float boxOriginalWidth;

    public MPGuiCheckButton(MPGuiShape shape, MPGuiString text, FontRenderer fontRenderer) {
        super(shape, text);
        setFontRenderer(fontRenderer);
        setShape(new MPGuiShape(
                shape.x(),
                shape.y(),
                fontRenderer.getStringWidth(text.get()) + 2f + shape.width(),
                Math.max(shape.height(), fontRenderer.FONT_HEIGHT)
        ));
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
        boxOriginalWidth = shape.width();
        setScaleRules(new MPGuiScaleRules(MPGuiScaleType.ORIGIN_VERTICAL));
        setGuiString(text);
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

    @Override
    protected void onDrawBackground(MPGuiTickEvent<MPGuiCheckButton> event) {
        MPGuiTexture texture = getTexturePack().getCalculatedTexture(stateManager);
        if (texture != null) {
            float scaleY  = calculatedShape.height() / Math.max(1f, shape.height());
            float curBoxW = boxOriginalWidth * scaleY;

            float boxX = calculatedShape.x() + calculatedShape.width() - curBoxW;
            float boxY = calculatedShape.y();

            texture.draw(
                    event.getMc(),
                    boxX, boxY,
                    curBoxW, calculatedShape.height()
            );
        }
    }

    @Override
    protected void onDrawText(MPGuiTickEvent<MPGuiCheckButton> event) {
        super.onDrawText(event);
        if (displayString != null) {
            FontRenderer fontrenderer = event.getMc().fontRenderer;
            int          color        = colorPack.getCalculatedColor(stateManager, packedFGColour);

            float scale        = getFontSize().getScale() * textScaleMultiplayer;
            float inverseScale = 1.0F / scale;

            GlStateManager.pushMatrix();
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GlStateManager.scale(scale, scale, 1.0F);
            MPGuiRenderHelper.drawString(
                    fontrenderer, displayString,
                    (calculatedInnerShape.x()) * inverseScale + calculatedTextOffsetTemp.x() * inverseScale,
                    calculatedInnerShape.y() * inverseScale + calculatedInnerShape.height() * inverseScale / 2f - (fontrenderer.FONT_HEIGHT) / 2f + calculatedTextOffsetTemp.y() * inverseScale,
                    color, getFontSize() != MPFontSize.SMALL
            );
            GlStateManager.popMatrix();
        }
    }
}
