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

        float startX    = 0, limitX = width, stepX = width;
        float singleUvX = 0;

        if (modeX == GuiTextureScaleRules.ScaleMode.SINGLE) {
            float startPosX = 0;
            if (anchorX == GuiTextureScaleRules.TextureAnchor.CENTER) startPosX = (width - texW) / 2f;
            else if (anchorX == GuiTextureScaleRules.TextureAnchor.MAX) startPosX = width - texW;

            float drawStart = Math.max(0, startPosX);
            float drawEnd   = Math.min(width, startPosX + texW);
            if (drawEnd <= drawStart) return;

            startX = drawStart;
            limitX = drawEnd;
            stepX = drawEnd - drawStart;
            singleUvX = drawStart - startPosX;
        } else if (modeX == GuiTextureScaleRules.ScaleMode.FILL) {
            stepX = texW;
        }

        float startY    = 0, limitY = height, stepY = height;
        float singleUvY = 0;

        if (modeY == GuiTextureScaleRules.ScaleMode.SINGLE) {
            float startPosY = 0;
            if (anchorY == GuiTextureScaleRules.TextureAnchor.CENTER) startPosY = (height - texH) / 2f;
            else if (anchorY == GuiTextureScaleRules.TextureAnchor.MAX) startPosY = height - texH;

            float drawStart = Math.max(0, startPosY);
            float drawEnd   = Math.min(height, startPosY + texH);
            if (drawEnd <= drawStart) return;

            startY = drawStart;
            limitY = drawEnd;
            stepY = drawEnd - drawStart;
            singleUvY = drawStart - startPosY;
        } else if (modeY == GuiTextureScaleRules.ScaleMode.FILL) {
            stepY = texH;
        }

        for (float curX = startX; curX < limitX - 0.001f; curX += stepX) {
            float drawW, uvX, uvW;
            if (modeX == GuiTextureScaleRules.ScaleMode.STRETCH) {
                drawW = width;
                uvX = 0;
                uvW = texW;
            } else if (modeX == GuiTextureScaleRules.ScaleMode.FILL) {
                drawW = Math.min(stepX, limitX - curX);
                uvX = 0;
                uvW = drawW;
            } else {
                drawW = stepX;
                uvX = singleUvX;
                uvW = stepX;
            }

            for (float curY = startY; curY < limitY - 0.001f; curY += stepY) {
                float drawH, uvY, uvH;
                if (modeY == GuiTextureScaleRules.ScaleMode.STRETCH) {
                    drawH = height;
                    uvY = 0;
                    uvH = texH;
                } else if (modeY == GuiTextureScaleRules.ScaleMode.FILL) {
                    drawH = Math.min(stepY, limitY - curY);
                    uvY = 0;
                    uvH = drawH;
                } else {
                    drawH = stepY;
                    uvY = singleUvY;
                    uvH = stepY;
                }

                GuiRenderHelper.drawTexture(
                        x + curX, y + curY,
                        startPos.x() + uvX, startPos.y() + uvY,
                        uvW, uvH,
                        drawW, drawH,
                        textureSize.x(), textureSize.y()
                );
            }
        }
    }
}