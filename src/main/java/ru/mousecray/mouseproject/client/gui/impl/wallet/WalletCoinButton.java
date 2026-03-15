/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl.wallet;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.dim.GuiScaleRules;
import ru.mousecray.mouseproject.client.gui.dim.GuiScaleType;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.client.gui.impl.MPGuiSelectedButton;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.common.economy.CoinValue;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class WalletCoinButton extends MPGuiSelectedButton<WalletCoinButton> {
    private       CoinValue coinValue;
    private final String    cachedName;

    public WalletCoinButton(
            GuiShape elementShape, MPFontSize fontSize, CoinValue coinValue,
            Consumer<MPGuiMouseClickEvent<WalletCoinButton>> onClick
    ) {
        super(
                coinValue.getFormattedValue(CoinValue.FormatType.SHORT), elementShape, GuiScreenWallet.TEXTURES, GuiScreenWallet.TEXTURES_SIZE,
                new GuiShape(230, 0, 10, 13), fontSize, onClick
        );
        this.coinValue = coinValue;
        cachedName = new ItemStack(coinValue.getType().getItem(), 1).getDisplayName();
        setTextOffset(new GuiVector(0, getElementShape().height() / 3.5f));
        int length = coinValue.getFormattedValue(CoinValue.FormatType.SHORT).length();
        if (length > 4) setTextScaleMultiplayer((float) Math.max(0.5, 4d / length));
        setScaleRules(new GuiScaleRules(GuiScaleType.ORIGIN_VERTICAL));
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
    protected void drawButtonForegroundLayer(@Nonnull MPGuiTickEvent<WalletCoinButton> event) {
        float width           = getCalculatedElementShape().width();
        float height          = getCalculatedElementShape().height();
        float x               = getCalculatedElementShape().x();
        float y               = getCalculatedElementShape().y();
        float partialTicks    = event.getPartialTick();
        float itemDefaultSize = 16.0f;
        float scale           = Math.min(width / itemDefaultSize, height / itemDefaultSize) / 1.5f;

        float sizeX = x + (width - itemDefaultSize * scale) / 2;
        float sizeY = y + (height - itemDefaultSize * scale) / 4f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(sizeX + scale * 13f / 1.6f, sizeY + scale * 14f / 1.7f, 0);
        GlStateManager.scale(scale * 13f, scale * 14f, 1.0f);

        GuiRenderHelper.enableBrightItemLighting();
        GlStateManager.enableRescaleNormal();

        if (coinValue != null) {
            if (getActionState() == GuiButtonActionState.HOVER) {
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

        GuiRenderHelper.disableBrightItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawButtonLastLayer(@Nonnull MPGuiTickEvent<WalletCoinButton> event) {
        drawButtonTooltip(event);
    }

    protected void drawButtonTooltip(MPGuiTickEvent<WalletCoinButton> event) {
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();

        if (mouseHover(event.getMc(), mouseX, mouseY)) {
            if (cachedName != null && !cachedName.isEmpty()) {
                GlStateManager.pushMatrix();

                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                GuiRenderHelper.drawTooltip(cachedName + " " + coinValue, event.getMc(), mouseX, mouseY, fontSize);

                GlStateManager.popMatrix();
            }
        }
    }
}