package ru.mousecray.mouseproject.client.gui.wallet;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.container.MPGuiFreePanel;
import ru.mousecray.mouseproject.client.gui.core.control.MPGuiNumberField;
import ru.mousecray.mouseproject.client.gui.core.control.MPGuiSlider;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.core.misc.MPNumberMode;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class WalletSliderControl extends MPGuiFreePanel {
    private final MPGuiNumberField field;
    private final MPGuiSlider      slider;

    private final long maxCoinValue;

    private Consumer<Boolean> validityListener;

    public WalletSliderControl(
            FontRenderer fontRenderer,
            MPFontSize fontSize,
            float width,
            float height,
            long maxCoinValue
    ) {
        super(new MPGuiShape(0, 0, width, height));
        this.maxCoinValue = maxCoinValue;

        field = new MPGuiNumberField(
                new MPGuiShape(0, 0, width, height * 0.8f),
                MPGuiString.localizedGuiTag("wallet.text_field.take_put_count"),
                MPNumberMode.POSITIVE
        );
        field.setFontRenderer(fontRenderer);
        field.setFontSize(fontSize);
        field.setOnTextTypedListener(this::onInternalTextTyped);

        field.setScaleRules(new MPGuiScaleRules(MPGuiScaleType.PARENT_HORIZONTAL));
        field.setPadding(new MPGuiPadding(3f, 0, 0, 0));

        float sliderH = height * 0.5f;
        float knobW   = sliderH * (5f / 7f);

        slider = new MPGuiSlider(new MPGuiShape(0, 0, width, sliderH), knobW, sliderH, 0, 100, MPOrientation.HORIZONTAL);
        slider.setScaleRules(new MPGuiScaleRules(MPGuiScaleType.PARENT_HORIZONTAL));

        slider.setOnSliderChangedListener(event -> {
            long newValue = event.getNewValue() == 0 ? 1 : (long) event.getNewValue() * this.maxCoinValue / 100;
            field.setNumberText(newValue);
        });

        addChild(field, null, null);
        addChild(slider, null, MPGuiVector.of(0, height / 1.8f));
    }

    private void onInternalTextTyped(MPGuiTextTypedEvent<MPGuiNumberField> event) {
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
                slider.setValue(progress, false);
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