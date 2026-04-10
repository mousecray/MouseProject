/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTextureScaleRules;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTextureScaleType;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiEventFactory;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseDragEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPClickType;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiSlider<T extends MPGuiSlider<T>> extends MPGuiPanel<T> {
    private final MPGuiButton<?> knob;
    private final MPGuiButton<?> track;

    private       int value;
    private final int min, max, range;

    private final boolean isVertical;

    private float progress = 0f;

    private Consumer<MPGuiMouseClickEvent<T>> onClick = null;
    private Consumer<MPGuiMouseDragEvent<T>>  onDrag  = null;

    private final MPGuiMouseClickEvent<T> clickEvent = new MPGuiMouseClickEvent<>(MPClickType.CLICK);
    private final MPGuiMouseDragEvent<T>  dragEvent  = new MPGuiMouseDragEvent<>();

    private IGuiVector lastParentDefaultSize, lastParentContentSize;

    public MPGuiSlider(MPGuiShape shape, float knobWidth, float knobHeight, int min, int max, boolean isVertical) {
        super(shape);
        this.isVertical = isVertical;
        this.min = min;
        this.max = Math.max(max, min);
        range = this.max - min;

        MPGuiTextureScaleRules trackScaleRules = isVertical
                ? new MPGuiTextureScaleRules(MPGuiTextureScaleType.FILL_VERTICAL, MPGuiTextureScaleType.SINGLE_HORIZONTAL_LEFT)
                .setMultipliers(0.7f, 0.5f)
                : new MPGuiTextureScaleRules(MPGuiTextureScaleType.FILL_HORIZONTAL, MPGuiTextureScaleType.SINGLE_VERTICAL_TOP)
                .setMultipliers(0.5f, 0.7f);

        class TrackButton extends MPGuiButton<TrackButton> {
            public TrackButton() {
                super("", shape, MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                MPGuiVector.of(230, 0), MPGuiVector.of(18, 7))
                        .setScaleRules(trackScaleRules)
                        .addTexture(GuiElementPersistentState.NORMAL, 0, 0.2f)
                        .build(), SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL);
            }

            @Override
            protected void onMouseDragged(MPGuiMouseDragEvent<TrackButton> event) {

            }


            @Override
            public void onClick(MPGuiMouseClickEvent<TrackButton> e) {
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireClickEvent(e);
            }
        }

        track = new TrackButton();
        track.setScaleRules(new GuiScaleRules(MPGuiScaleType.PARENT));
        addChild(track);

        class KnobButton extends MPGuiButton<KnobButton> {
            public KnobButton() {
                super("", new MPGuiShape(0, 0, knobWidth, knobHeight),
                        MPGuiTexturePack.Builder.create(
                                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                        MPGuiVector.of(90, 0), MPGuiVector.of(5, 7)
                                )
                                .addTexture(GuiElementPersistentState.NORMAL, 0)
                                .addTexture(GuiElementActionState.HOVER, 1)
                                .addTexture(GuiElementActionState.PRESSED, 2)
                                .build(), SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL);
            }

            @Override
            protected void onMouseDragged(MPGuiMouseDragEvent<KnobButton> e) {
                if (e.isCancelled()) return;
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireDragEvent(e);
            }

            @Override
            public void onClick(MPGuiMouseClickEvent<KnobButton> e) {
                if (e.isCancelled()) return;
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireClickEvent(e);
            }
        }

        KnobButton knobBtn = new KnobButton();
        knob = knobBtn;
        knobBtn.setScaleRules(new GuiScaleRules(isVertical ? MPGuiScaleType.ORIGIN_HORIZONTAL : MPGuiScaleType.ORIGIN_VERTICAL));
        addChild(knobBtn);

        setValue(min);
    }

    private void updateFromMouseX(int mouseX, int mouseY) {
        MPMutableGuiShape inner = getCalculatedShape();

        float knobW = knob.getCalculatedShape().width();
        float knobH = knob.getCalculatedShape().height();

        float trackLength = isVertical
                ? inner.height() - knobH
                : inner.width() - knobW;

        if (trackLength <= 0) return;

        float rel = isVertical
                ? (mouseY - inner.y()) - knobH / 2f
                : (mouseX - inner.x()) - knobW / 2f;

        float newProgress = MathHelper.clamp(rel / trackLength, 0f, 1f);
        if (Float.compare(progress, newProgress) == 0) return;

        progress = newProgress;
        value = min + Math.round(progress * range);

        recalculateKnobPosition();
    }

    private void fireClickEvent(MPGuiMouseClickEvent<?> original) {
        if (onClick != null) {
            MPGuiEventFactory.pushMouseClickEvent(
                    clickEvent, original.getMouseX(), original.getMouseY()
            );
            onClick.accept(clickEvent);
        }
    }

    private void fireDragEvent(MPGuiMouseDragEvent<?> original) {
        if (onDrag != null) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent,
                    original.getMouseX(), original.getMouseY(),
                    original.getMoveDirection(), original.getDiffX(), original.getDiffY(),
                    original.getTickDown());
            onDrag.accept(dragEvent);
        }
    }

    public void onClick(Consumer<MPGuiMouseClickEvent<T>> consumer) { onClick = consumer; }
    public void onDrag(Consumer<MPGuiMouseDragEvent<T>> consumer)   { onDrag = consumer; }

    public T onChange(Consumer<Integer> consumer) {
        onDrag(e -> consumer.accept(getValue()));
        onClick(e -> consumer.accept(getValue()));
        return self();
    }

    public int getValue() { return value; }

    public void setValue(int newValue) {
        newValue = MathHelper.clamp(newValue, min, max);
        if (value == newValue) return;

        value = newValue;
        progress = range == 0 ? 0f : (float) (newValue - min) / range;

        recalculateKnobPosition();
    }

    public float getProgress()  { return progress; }
    public boolean isVertical() { return isVertical; }

    @Override
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MPMutableGuiShape inner) {
        childAvailableTemp.withShape(inner);
        track.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

        recalculateKnobPosition();
    }

    private void recalculateKnobPosition() {
        MPMutableGuiShape inner = getCalculatedShape();
        if (inner.width() <= 0 || inner.height() <= 0) return;

        float knobW = knob.getShape().width();
        float knobH = knob.getShape().height();

        if (lastParentDefaultSize != null && lastParentContentSize != null) {
            knob.measurePreferred(
                    lastParentDefaultSize, lastParentContentSize,
                    inner.width(), inner.height(), measureTemp
            );
            knobW = measureTemp.x();
            knobH = measureTemp.y();
        }

        float trackLength = isVertical
                ? inner.height() - knobH
                : inner.width() - knobW;

        if (trackLength <= 0) {
            progress = 0f;
            value = min;
            trackLength = 0;
        }

        float knobPrimary = progress * trackLength;
        float knobSecondary = isVertical
                ? (inner.width() - knobW) / 2f
                : (inner.height() - knobH) / 2f;

        float knobX = inner.x() + (isVertical ? knobSecondary : knobPrimary);
        float knobY = inner.y() + (isVertical ? knobPrimary : knobSecondary);

        childAvailableTemp.withX(knobX).withY(knobY).withWidth(knobW).withHeight(knobH);

        if (lastParentDefaultSize != null && lastParentContentSize != null) {
            knob.calculate(lastParentDefaultSize, lastParentContentSize, childAvailableTemp);
        } else {
            knob.calculate(MPGuiVector.of(inner.width(), inner.height()),
                    MPGuiVector.of(inner.width(), inner.height()),
                    childAvailableTemp);
        }
    }

    @Override
    public void calculate(IGuiVector pDefSize, IGuiVector pContentSize, IGuiShape available) {
        lastParentDefaultSize = pDefSize;
        lastParentContentSize = pContentSize;

        super.calculate(pDefSize, pContentSize, available);
    }
}