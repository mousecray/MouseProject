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
        this.endPos = elementSize;
        this.scaleRules = scaleRules != null ? scaleRules : new GuiTextureScaleRules(GuiTextureScaleType.STRETCH);
    }

    public ResourceLocation getTexture()        { return texture; }
    public IGuiVector getTextureSize()          { return textureSize; }
    public IGuiVector getStartPos()             { return startPos; }
    public IGuiVector getElementSize()          { return endPos; }
    public GuiTextureScaleRules getScaleRules() { return scaleRules; }

    public void bind(TextureManager manager)    { manager.bindTexture(texture); }

    public void draw(Minecraft mc, float x, float y, float width, float height) {
        if (width <= 0 || height <= 0) return;

        bind(mc.getTextureManager());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GuiTextureScaleRules.ScaleMode     modeX   = scaleRules.getModeX();
        GuiTextureScaleRules.ScaleMode     modeY   = scaleRules.getModeY();
        GuiTextureScaleRules.TextureAnchor anchorX = scaleRules.getAnchorX();
        GuiTextureScaleRules.TextureAnchor anchorY = scaleRules.getAnchorY();

        float texW = endPos.x();
        float texH = endPos.y();

        float multX = scaleRules.getMultiplierX();
        float multY = scaleRules.getMultiplierY();

        float scaledTexW = texW * multX;
        float scaledTexH = texH * multY;

        float uvRatioX = 1.0f / multX;
        float uvRatioY = 1.0f / multY;

        float xCursor = 0;
        while (xCursor < width) {
            float drawX = xCursor, drawW = 0, uvX = 0, uvW = 0;

            if (modeX == GuiTextureScaleRules.ScaleMode.STRETCH) {
                drawW = width;
                uvW = texW;
                xCursor = width;
            } else if (modeX == GuiTextureScaleRules.ScaleMode.FILL) {
                drawW = Math.min(scaledTexW, width - xCursor);
                uvW = drawW * uvRatioX;
                xCursor += scaledTexW;
            } else {
                float boxStart = 0;
                if (anchorX == GuiTextureScaleRules.TextureAnchor.CENTER) boxStart = (width - scaledTexW) / 2f;
                else if (anchorX == GuiTextureScaleRules.TextureAnchor.MAX) boxStart = width - scaledTexW;

                float dStart = Math.max(0, boxStart);
                float dEnd   = Math.min(width, boxStart + scaledTexW);

                if (dEnd > dStart) {
                    drawX = dStart;
                    drawW = dEnd - dStart;
                    uvX = (dStart - boxStart) * uvRatioX;
                    uvW = drawW * uvRatioX;
                }
                xCursor = width;
            }

            if (drawW > 0) {
                float yCursor = 0;
                while (yCursor < height) {
                    float drawY = yCursor, drawH = 0, uvY = 0, uvH = 0;

                    if (modeY == GuiTextureScaleRules.ScaleMode.STRETCH) {
                        drawH = height;
                        uvH = texH;
                        yCursor = height;
                    } else if (modeY == GuiTextureScaleRules.ScaleMode.FILL) {
                        drawH = Math.min(scaledTexH, height - yCursor);
                        uvH = drawH * uvRatioY;
                        yCursor += scaledTexH;
                    } else {
                        float boxStart = 0;
                        if (anchorY == GuiTextureScaleRules.TextureAnchor.CENTER) boxStart = (height - scaledTexH) / 2f;
                        else if (anchorY == GuiTextureScaleRules.TextureAnchor.MAX) boxStart = height - scaledTexH;

                        float dStart = Math.max(0, boxStart);
                        float dEnd   = Math.min(height, boxStart + scaledTexH);

                        if (dEnd > dStart) {
                            drawY = dStart;
                            drawH = dEnd - dStart;
                            uvY = (dStart - boxStart) * uvRatioY;
                            uvH = drawH * uvRatioY;
                        }
                        yCursor = height;
                    }

                    if (drawH > 0) {
                        GuiRenderHelper.drawTexture(
                                x + drawX, y + drawY,
                                startPos.x() + uvX, startPos.y() + uvY,
                                uvW, uvH,
                                drawW, drawH,
                                textureSize.x(), textureSize.y()
                        );
                    }
                }
            }
        }
    }
}