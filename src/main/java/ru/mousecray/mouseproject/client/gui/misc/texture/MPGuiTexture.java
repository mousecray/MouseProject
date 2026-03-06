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
    private final ResourceLocation texture;
    private final IGuiVector       textureSize;
    private final IGuiVector       startPos;
    private final IGuiVector       endPos;

    public MPGuiTexture(ResourceLocation texture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
        this.texture = texture;

        this.textureSize = textureSize;
        this.startPos = startPos;
        endPos = elementSize;
    }

    public ResourceLocation getTexture()     { return texture; }
    public IGuiVector getTextureSize()       { return textureSize; }
    public IGuiVector getStartPos()          { return startPos; }
    public IGuiVector getElementSize()       { return endPos; }

    public void bind(TextureManager manager) { manager.bindTexture(texture); }

    public void draw(Minecraft mc, float x, float y, float width, float height) {
        bind(mc.getTextureManager());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GuiRenderHelper.drawTexture(
                x, y,
                startPos.x(), startPos.y(), endPos.x(), endPos.y(),
                width, height,
                textureSize.x(), textureSize.y()
        );
    }
}