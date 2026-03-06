package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.StateColorContainer;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiCheckButton extends MPGuiButton<MPGuiCheckButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiCheckButton>> onClick;

    public MPGuiCheckButton(
            MPGuiString text,
            FontRenderer fontRenderer,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiCheckButton>> onClick) {
        super(
                text.get(),
                elementShape.grow(-fontRenderer.getStringWidth(text.get()) - 1, 0, 0, 0),
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 1)
                        .addTexture(GuiButtonActionState.PRESSED, 2)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.HOVER), 1)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.PRESSED), 2)
                        .addTexture(GuiButtonPersistentState.SELECTED, 3)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.HOVER), 4)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
        setGuiString(text);
        colorContainer = StateColorContainer.Builder
                .create(14737632)
                .addState(GuiButtonPersistentState.DISABLED, 10526880)
                .addState(GuiButtonPersistentState.NORMAL, 14737632)
                .addState(GuiButtonActionState.HOVER, 15592941)
                .addState(GuiButtonActionState.PRESSED, 13948116)
                .addState(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.HOVER), 15592941)
                .addState(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.PRESSED), 13948116)
                .addState(GuiButtonPersistentState.SELECTED, 14737632)
                .addState(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.HOVER), 15592941)
                .addState(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.PRESSED), 13948116)
                .build();
    }

    public MPGuiCheckButton(
            String text,
            FontRenderer fontRenderer,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiCheckButton>> onClick) {
        super(
                text,
                elementShape.grow(-fontRenderer.getStringWidth(text) - 1, 0, 0, 0),
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 1)
                        .addTexture(GuiButtonActionState.PRESSED, 2)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.HOVER), 1)
                        .addTexture(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.PRESSED), 2)
                        .addTexture(GuiButtonPersistentState.SELECTED, 3)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.HOVER), 4)
                        .addTexture(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
        colorContainer = StateColorContainer.Builder
                .create(14737632)
                .addState(GuiButtonPersistentState.DISABLED, 10526880)
                .addState(GuiButtonPersistentState.NORMAL, 14737632)
                .addState(GuiButtonActionState.HOVER, 15592941)
                .addState(GuiButtonActionState.PRESSED, 13948116)
                .addState(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.HOVER), 15592941)
                .addState(GuiButtonPersistentState.NORMAL.combine(GuiButtonActionState.PRESSED), 13948116)
                .addState(GuiButtonPersistentState.SELECTED, 14737632)
                .addState(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.HOVER), 15592941)
                .addState(GuiButtonPersistentState.SELECTED.combine(GuiButtonActionState.PRESSED), 13948116)
                .build();
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<MPGuiCheckButton> event) {
        applyState(
                getPersistentState() == GuiButtonPersistentState.SELECTED
                        ? GuiButtonPersistentState.NORMAL : GuiButtonPersistentState.SELECTED
        );
        if (onClick != null) onClick.accept(event);
    }

    @Override
    protected void drawButtonTextLayer(@Nonnull MPGuiTickEvent<MPGuiCheckButton> event) {
        if (displayString != null) {
            FontRenderer fontrenderer = event.getMc().fontRenderer;
            int          color        = colorContainer.getCalculatedColor(actionState, persistentState, packedFGColour);

            float scale        = fontSize.getScale() * textScaleMultiplayer;
            float inverseScale = 1.0F / scale;

            GlStateManager.pushMatrix();
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GlStateManager.scale(scale, scale, 1.0F);
            GuiRenderHelper.drawString(
                    fontrenderer, displayString,
                    (calculatedElementShape.x() - fontrenderer.getStringWidth(displayString) - 1) * inverseScale + calculatedTextOffsetTemp.x() * inverseScale,
                    calculatedElementShape.y() * inverseScale + calculatedElementShape.height() * inverseScale / 2f - (fontrenderer.FONT_HEIGHT) / 2f + calculatedTextOffsetTemp.y() * inverseScale,
                    color, fontSize != MPFontSize.SMALL
            );
            GlStateManager.popMatrix();
        }
    }
}
