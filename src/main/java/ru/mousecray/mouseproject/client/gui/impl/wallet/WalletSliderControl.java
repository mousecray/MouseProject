/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl.wallet;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.client.gui.impl.MPGuiNumberField;
import ru.mousecray.mouseproject.client.gui.impl.MPGuiSimpleField;
import ru.mousecray.mouseproject.client.gui.impl.MPGuiSlider;
import ru.mousecray.mouseproject.client.gui.impl.container.MPGuiFreePanel;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class WalletSliderControl extends MPGuiFreePanel {

    private final MPGuiNumberField field;
    private final MPGuiSlider<?>   slider;

    private final long maxCoinValue;

    private Consumer<Boolean> validityListener;

    public WalletSliderControl(
            FontRenderer fontRenderer,
            MPFontSize fontSize,
            float width,
            float height,
            long maxCoinValue
    ) {
        super(new GuiShape(0, 0, width, height));
        this.maxCoinValue = maxCoinValue;

        field = new MPGuiNumberField(
                new GuiShape(0, 0, width, height * 0.8f),
                MPGuiString.localizedGuiTag("wallet.text_field.take_put_count"),
                fontRenderer, fontSize, this::onInternalTextTyped
        );
        field.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));
        field.setPadding(new GuiPadding(3f, 0, 0, 0));

        float sliderH = height * 0.5f;
        float knobW   = sliderH * (5f / 7f);

        class InnerSlider extends MPGuiSlider<InnerSlider> {
            public InnerSlider() {
                super(new GuiShape(0, 0, width, sliderH),
                        null,
                        MPGuiTexturePack.Builder.create(
                                        GuiScreenWallet.TEXTURES, GuiScreenWallet.TEXTURES_SIZE,
                                        GuiVector.of(90, 200), GuiVector.of(5, 7)
                                )
                                .addTexture(GuiButtonPersistentState.NORMAL, 0)
                                .addTexture(GuiButtonActionState.HOVER, 1)
                                .addTexture(GuiButtonActionState.PRESSED, 2)
                                .build(),
                        knobW, sliderH, 0, 100, false);
            }
        }
        slider = new InnerSlider();
        slider.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));

        slider.onChange(value -> {
            long newValue = value == 0 ? 1 : (long) value * this.maxCoinValue / 100;
            field.setNumberText(newValue);
        });

        addChild(field, null, null);
        addChild(slider, null, GuiVector.of(0, height / 1.8f));
    }

    private void onInternalTextTyped(MPGuiTextTypedEvent<MPGuiSimpleField> event) {
        String newText = event.getNewText();

        if (newText == null || newText.trim().isEmpty()) {
            if (validityListener != null) validityListener.accept(false);
            return;
        }

        if (newText.length() > 19) {
            event.setCancelled(true);
            return;
        }

        try {
            long val = Long.parseLong(newText);
            if (val <= 0) throw new NumberFormatException();

            if (validityListener != null) validityListener.accept(true);

            if (maxCoinValue > 0) {
                int progress = (int) Math.min(100, Math.max(0, (val * 100) / maxCoinValue));
                slider.setValue(progress);
            }
        } catch (NumberFormatException e) {
            event.setCancelled(true);
            if (validityListener != null) validityListener.accept(false);
        }
    }

    public WalletSliderControl onValidityChanged(Consumer<Boolean> listener) {
        validityListener = listener;
        return this;
    }

    public void setNumberText(long val) { field.setNumberText(val); }
    public long getNumberText()         { return field.getNumberText(); }

    public void addValue(long delta) {
        long current  = field.getNumberText();
        long newValue = current + delta;

        if (newValue <= 0) field.setText("");
        else field.setNumberText(newValue);
    }
}