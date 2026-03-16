/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.MPGuiEventFactory;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseDragEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPClickType;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.texture.GuiTextureScaleRules;
import ru.mousecray.mouseproject.client.gui.misc.texture.GuiTextureScaleType;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;
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

    public MPGuiSlider(GuiShape shape, float knobWidth, float knobHeight, int min, int max, boolean isVertical) {
        super(shape);
        this.isVertical = isVertical;
        this.min = min;
        this.max = Math.max(max, min);
        range = this.max - min;

        class TrackButton extends MPGuiButton<TrackButton> {
            public TrackButton() {
                super("", shape, MPGuiTexturePack.Builder
                        .create(
                                MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                GuiVector.of(230, 0), GuiVector.of(9, 4))
                        .setScaleRules(new GuiTextureScaleRules(GuiTextureScaleType.FILL_HORIZONTAL))
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .build(), SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL);
            }

            @Override
            public void onClick(MPGuiMouseClickEvent<TrackButton> e) {
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireClickEvent(e);
            }
        }

        track = new TrackButton();
        track.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT));
        addChild(track);

        class KnobButton extends MPGuiButton<KnobButton> {
            public KnobButton() {
                super("", new GuiShape(0, 0, knobWidth, knobHeight),
                        MPGuiTexturePack.Builder.create(
                                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                                        GuiVector.of(90, 0), GuiVector.of(5, 7)
                                )
                                .addTexture(GuiButtonPersistentState.NORMAL, 0)
                                .addTexture(GuiButtonActionState.HOVER, 1)
                                .addTexture(GuiButtonActionState.PRESSED, 2)
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
        knobBtn.setScaleRules(new GuiScaleRules(GuiScaleType.ORIGIN_VERTICAL));
        addChild(knobBtn);

        setValue(min);
    }

    private void updateFromMouseX(int mouseX, int mouseY) {
        MutableGuiShape inner = getCalculatedElementShape();

        float knobW = knob.getCalculatedElementShape().width();
        float knobH = knob.getCalculatedElementShape().height();

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
                    clickEvent, self(), original.getMc(), original.getMouseX(), original.getMouseY()
            );
            onClick.accept(clickEvent);
        }
    }

    private void fireDragEvent(MPGuiMouseDragEvent<?> original) {
        if (onDrag != null) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, self(), original.getMc(),
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
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        childAvailableTemp.withShape(inner);
        track.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

        recalculateKnobPosition();
    }

    private void recalculateKnobPosition() {
        MutableGuiShape inner = getCalculatedElementShape();
        if (inner.width() <= 0 || inner.height() <= 0) return;

        float knobW = knob.getElementShape().width();
        float knobH = knob.getElementShape().height();

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
            knob.calculate(GuiVector.of(inner.width(), inner.height()),
                    GuiVector.of(inner.width(), inner.height()),
                    childAvailableTemp);
        }
    }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        lastParentDefaultSize = parentDefaultSize;
        lastParentContentSize = parentContentSize;

        super.calculate(parentDefaultSize, parentContentSize, available);
    }
}