/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.wallet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiSelectedButton;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleRules;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiScaleType;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.common.economy.CoinValue;

import javax.annotation.Nonnull;

public class WalletCoinButton extends MPGuiSelectedButton<WalletCoinButton> {
    private       CoinValue coinValue;
    private final String    cachedName;

    public WalletCoinButton(MPGuiShape elementShape, CoinValue coinValue) {
        super(elementShape, MPGuiString.simple(coinValue.getFormattedValue(CoinValue.FormatType.SHORT)));
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        GuiScreenWallet.TEXTURES, GuiScreenWallet.TEXTURES_SIZE,
                        MPGuiVector.of(230, 0), MPGuiVector.of(10, 13)
                )
                .addTexture(0)
                .addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .addTexture(3, MPGuiElementState.SELECTED)
                .addTexture(4, MPGuiElementState.SELECTED, MPGuiElementState.HOVERED)
                .addTexture(5, MPGuiElementState.SELECTED, MPGuiElementState.PRESSED)
                .build()
        );
        this.coinValue = coinValue;
        cachedName = new ItemStack(coinValue.getType().getItem(), 1).getDisplayName();
        setTextOffset(MPGuiVector.of(0, getShape().height() / 3.5f));
        int length = coinValue.getFormattedValue(CoinValue.FormatType.SHORT).length();
        if (length > 4) setTextScaleMultiplayer((float) Math.max(0.5, 4d / length));
        setScaleRules(new MPGuiScaleRules(MPGuiScaleType.ORIGIN_VERTICAL));
    }

    public void setCount(CoinValue count) {
        coinValue = count;
        String formattedValue = coinValue.getFormattedValue(CoinValue.FormatType.SHORT);
        int    length         = formattedValue.length();
        if (length > 4) setTextScaleMultiplayer((float) Math.max(0.5, 4d / length));
        setText(formattedValue);
    }

    public CoinValue getCount() { return coinValue; }

    @Override
    protected void onDrawForeground(@Nonnull MPGuiTickEvent<WalletCoinButton> event) {
        super.onDrawForeground(event);
        float width           = getCalculatedShape().width();
        float height          = getCalculatedShape().height();
        float x               = getCalculatedShape().x();
        float y               = getCalculatedShape().y();
        float partialTicks    = event.getPartialTick();
        float itemDefaultSize = 16.0f;
        float scale           = Math.min(width / itemDefaultSize, height / itemDefaultSize) / 1.5f;

        float sizeX = x + (width - itemDefaultSize * scale) / 2;
        float sizeY = y + (height - itemDefaultSize * scale) / 4f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(sizeX + scale * 13f / 1.6f, sizeY + scale * 14f / 1.7f, 0);
        GlStateManager.scale(scale * 13f, scale * 14f, 1.0f);

        MPGuiRenderHelper.enableBrightItemLighting();
        GlStateManager.enableRescaleNormal();

        if (coinValue != null) {
            if (stateManager.has(MPGuiElementState.HOVERED)) {
                GlStateManager.translate(0, 0, 0);
                GlStateManager.scale(1.2f, 1.2f, 1.0f);
                float rotationAngle = ((System.currentTimeMillis() % 2000) / 2000.0f) * 360.0f;
                rotationAngle += partialTicks * 9.0f;
                GlStateManager.rotate(rotationAngle, 0.0f, 1.0f, 0.0f);
            }
            event.getMc().getRenderItem().renderItem(
                    new ItemStack(coinValue.getType().getItem()),
                    ItemCameraTransforms.TransformType.FIXED
            );
        }

        MPGuiRenderHelper.disableBrightItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    @Override
    protected void onDrawLast(@Nonnull MPGuiTickEvent<WalletCoinButton> event) {
        super.onDrawLast(event);
        drawButtonTooltip(event);
    }

    private final ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());

    protected void drawButtonTooltip(MPGuiTickEvent<WalletCoinButton> event) {
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();

        if (mouseHover(event.getMc(), mouseX, mouseY)) {
            if (cachedName != null && !cachedName.isEmpty()) {
                GlStateManager.pushMatrix();

                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                MPGuiRenderHelper.drawTooltip(cachedName + " " + coinValue, event.getMc(), mouseX, mouseY, getFontSize(), sc);

                GlStateManager.popMatrix();
            }
        }
    }
}