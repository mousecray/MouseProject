/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.dim.GuiScaleRules;
import ru.mousecray.mouseproject.client.gui.dim.GuiScaleType;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.StateColorContainer;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiCheckButton extends MPGuiButton<MPGuiCheckButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiCheckButton>> onClick;
    private final float                                            boxOriginalWidth;

    public MPGuiCheckButton(
            GuiShape elementShape, MPGuiString text, FontRenderer fontRenderer,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiCheckButton>> onClick) {
        super(
                text.get(),
                new GuiShape(
                        elementShape.x(),
                        elementShape.y(),
                        fontRenderer.getStringWidth(text.get()) + 2f + elementShape.width(),
                        Math.max(elementShape.height(), fontRenderer.FONT_HEIGHT)
                ),
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.of(184, 0), GuiVector.of(8)
                        )
                        .addTexture(GuiElementPersistentState.NORMAL, 0)
                        .addTexture(GuiElementActionState.HOVER, 1)
                        .addTexture(GuiElementActionState.PRESSED, 2)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.HOVER), 1)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.PRESSED), 2)
                        .addTexture(GuiElementPersistentState.SELECTED, 3)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.HOVER), 4)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        boxOriginalWidth = elementShape.width();
        setScaleRules(new GuiScaleRules(GuiScaleType.ORIGIN_VERTICAL));
        this.onClick = onClick;
        setGuiString(text);
        colorContainer = StateColorContainer.Builder
                .create(14737632)
                .addState(GuiElementPersistentState.DISABLED, 10526880)
                .addState(GuiElementPersistentState.NORMAL, 14737632)
                .addState(GuiElementActionState.HOVER, 15592941)
                .addState(GuiElementActionState.PRESSED, 13948116)
                .addState(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.HOVER), 15592941)
                .addState(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.PRESSED), 13948116)
                .addState(GuiElementPersistentState.SELECTED, 14737632)
                .addState(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.HOVER), 15592941)
                .addState(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.PRESSED), 13948116)
                .build();
    }

    public MPGuiCheckButton(
            GuiShape elementShape, String text, FontRenderer fontRenderer,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiCheckButton>> onClick) {
        super(
                text,
                new GuiShape(
                        elementShape.x(),
                        elementShape.y(),
                        fontRenderer.getStringWidth(text) + 2f + elementShape.width(),
                        Math.max(elementShape.height(), fontRenderer.FONT_HEIGHT)
                ),
                MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.of(184, 0), GuiVector.of(8)
                        )
                        .addTexture(GuiElementPersistentState.NORMAL, 0)
                        .addTexture(GuiElementActionState.HOVER, 1)
                        .addTexture(GuiElementActionState.PRESSED, 2)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.HOVER), 1)
                        .addTexture(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.PRESSED), 2)
                        .addTexture(GuiElementPersistentState.SELECTED, 3)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.HOVER), 4)
                        .addTexture(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.PRESSED), 5)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        boxOriginalWidth = elementShape.width();
        setScaleRules(new GuiScaleRules(GuiScaleType.ORIGIN_VERTICAL));
        this.onClick = onClick;
        colorContainer = StateColorContainer.Builder
                .create(14737632)
                .addState(GuiElementPersistentState.DISABLED, 10526880)
                .addState(GuiElementPersistentState.NORMAL, 14737632)
                .addState(GuiElementActionState.HOVER, 15592941)
                .addState(GuiElementActionState.PRESSED, 13948116)
                .addState(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.HOVER), 15592941)
                .addState(GuiElementPersistentState.NORMAL.combine(GuiElementActionState.PRESSED), 13948116)
                .addState(GuiElementPersistentState.SELECTED, 14737632)
                .addState(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.HOVER), 15592941)
                .addState(GuiElementPersistentState.SELECTED.combine(GuiElementActionState.PRESSED), 13948116)
                .build();
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<MPGuiCheckButton> event) {
        applyState(
                getPersistentState() == GuiElementPersistentState.SELECTED
                        ? GuiElementPersistentState.NORMAL : GuiElementPersistentState.SELECTED
        );
        if (onClick != null) onClick.accept(event);
    }

    @Override
    protected void drawButtonBackgroundLayer(MPGuiTickEvent<MPGuiCheckButton> event) {
        MPGuiTexture texture = getTexturePack().getCalculatedTexture(actionState, persistentState);
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
                    (calculatedShape.x()) * inverseScale + calculatedTextOffsetTemp.x() * inverseScale,
                    calculatedShape.y() * inverseScale + calculatedShape.height() * inverseScale / 2f - (fontrenderer.FONT_HEIGHT) / 2f + calculatedTextOffsetTemp.y() * inverseScale,
                    color, fontSize != MPFontSize.SMALL
            );
            GlStateManager.popMatrix();
        }
    }
}
