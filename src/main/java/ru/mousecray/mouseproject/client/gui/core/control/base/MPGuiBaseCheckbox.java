/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control.base;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.core.component.MPGuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleRules;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleType;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public abstract class MPGuiBaseCheckbox<T extends MPGuiBaseCheckbox<T>> extends MPGuiSelectableButton<T> {
    private final float boxOriginalWidth;

    public MPGuiBaseCheckbox(MPGuiShape shape, MPGuiString text, FontRenderer fontRenderer) {
        super(shape, text);
        setFontRenderer(fontRenderer);
        setShape(new MPGuiShape(
                shape.x(),
                shape.y(),
                fontRenderer.getStringWidth(text.get()) + 2f + shape.width(),
                Math.max(shape.height(), fontRenderer.FONT_HEIGHT)
        ));
        boxOriginalWidth = shape.width();
        setScaleRules(new MPGuiScaleRules(MPGuiScaleType.ORIGIN_VERTICAL));
        setGuiString(text);
    }

    @Override
    protected void onDrawBackground(MPGuiTickEvent<T> event) {
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
    protected void onDrawText(MPGuiTickEvent<T> event) {
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
