/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.misc.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.dim.IGuiVector;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;

@SideOnly(Side.CLIENT)
public class MPGuiTexture {
    private final ResourceLocation     texture;
    private final IGuiVector           textureSize;
    private final IGuiVector           startPos;
    private final IGuiVector           endPos;
    private final GuiTextureScaleRules scaleRules;

    public MPGuiTexture(ResourceLocation texture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
        this(texture, textureSize, startPos, elementSize, new GuiTextureScaleRules(GuiTextureScaleType.STRETCH));
    }

    public MPGuiTexture(ResourceLocation texture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize, GuiTextureScaleRules scaleRules) {
        this.texture = texture;
        this.textureSize = textureSize;
        this.startPos = startPos;
        endPos = elementSize;
        this.scaleRules = scaleRules != null ? scaleRules : new GuiTextureScaleRules(GuiTextureScaleType.STRETCH);
    }

    public ResourceLocation getTexture()        { return texture; }
    public IGuiVector getTextureSize()          { return textureSize; }
    public IGuiVector getStartPos()             { return startPos; }
    public IGuiVector getElementSize()          { return endPos; }
    public GuiTextureScaleRules getScaleRules() { return scaleRules; }

    public void bind(TextureManager manager)    { manager.bindTexture(texture); }

    public void draw(Minecraft mc, float x, float y, float width, float height) {
        bind(mc.getTextureManager());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        boolean fillX = scaleRules.isFillHorizontal();
        boolean fillY = scaleRules.isFillVertical();

        if (!fillX && !fillY) {
            GuiRenderHelper.drawTexture(
                    x, y,
                    startPos.x(), startPos.y(), endPos.x(), endPos.y(),
                    width, height,
                    textureSize.x(), textureSize.y()
            );
        } else {
            float sourceW = endPos.x();
            float sourceH = endPos.y();

            float stepX = fillX ? sourceW : width;
            float stepY = fillY ? sourceH : height;

            for (float curX = 0; curX < width; curX += stepX) {
                float drawW = Math.min(stepX, width - curX);
                float uvW   = fillX ? drawW : sourceW; // Координаты обрезаются

                for (float curY = 0; curY < height; curY += stepY) {
                    float drawH = Math.min(stepY, height - curY);
                    float uvH   = fillY ? drawH : sourceH;

                    GuiRenderHelper.drawTexture(
                            x + curX, y + curY,
                            startPos.x(), startPos.y(), uvW, uvH,
                            drawW, drawH,
                            textureSize.x(), textureSize.y()
                    );
                }
            }
        }
    }
}