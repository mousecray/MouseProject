package ru.mousecray.mouseproject.client.gui.impl;

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
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiSlider<T extends MPGuiSlider<T>> extends MPGuiPanel<T> {
    private final MPGuiButton<?> knob;

    private       int value;
    private final int min, max, range;

    private final boolean isVertical;

    private float progress = 0f;

    private Consumer<MPGuiMouseClickEvent<T>> onClick = null;
    private Consumer<MPGuiMouseDragEvent<T>>  onDrag  = null;

    private final MPGuiMouseClickEvent<T> clickEvent = new MPGuiMouseClickEvent<>(MPClickType.CLICK);
    private final MPGuiMouseDragEvent<T>  dragEvent  = new MPGuiMouseDragEvent<>();

    private IGuiVector lastParentDefaultSize, lastParentContentSize;

    public MPGuiSlider(GuiShape shape, MPGuiTexturePack trackTexture, MPGuiTexturePack knobTexture, GuiVector knobSize, int min, int max, boolean isVertical) {
        super(shape);
        this.isVertical = isVertical;
        this.min = min;
        this.max = Math.max(max, min);
        range = this.max - min;

        class TrackButton extends MPGuiButton<TrackButton> {
            public TrackButton() {
                super("", shape, trackTexture, SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL);
            }

            @Override
            public void onClick(@Nonnull MPGuiMouseClickEvent<TrackButton> e) {
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireClickEvent(e);
            }
        }

        TrackButton track = new TrackButton();
        track.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT));
        addChild(track, null, null, null);

        class KnobButton extends MPGuiButton<KnobButton> {
            public KnobButton() {
                super("", new GuiShape(0, 0, knobSize.x(), knobSize.y()),
                        knobTexture, SoundEvents.UI_BUTTON_CLICK, MPFontSize.NORMAL);
            }

            @Override
            protected void onMouseDragged(@Nonnull MPGuiMouseDragEvent<KnobButton> e) {
                if (e.isCancelled()) return;
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireDragEvent(e);
            }

            @Override
            public void onClick(@Nonnull MPGuiMouseClickEvent<KnobButton> e) {
                if (e.isCancelled()) return;
                updateFromMouseX(e.getMouseX(), e.getMouseY());
                fireClickEvent(e);
            }
        }

        KnobButton knobBtn = new KnobButton();
        knob = knobBtn;
        knobBtn.setScaleRules(new GuiScaleRules(GuiScaleType.FIXED));
        addChild(knobBtn, null, null, null);

        setValue(min);
    }

    private void updateFromMouseX(int mouseX, int mouseY) {
        MutableGuiShape inner = getCalculatedElementShape();
        float trackLength = isVertical
                ? inner.height() - knob.getCalculatedElementShape().height()
                : inner.width() - knob.getCalculatedElementShape().width();
        if (trackLength <= 0) return;

        float rel = isVertical
                ? mouseY - inner.y()
                : mouseX - inner.x();
        float newProgress = MathHelper.clamp(rel / trackLength, 0f, 1f);
        if (Float.compare(progress, newProgress) == 0) return;

        progress = newProgress;
        value = min + Math.round(progress * range);

        recalculateKnobPosition();
    }

    private void fireClickEvent(MPGuiMouseClickEvent<?> original) {
        if (onClick != null) {
            MPGuiEventFactory.pushMouseClickEvent(clickEvent, self(), original.getMc(), original.getMouseX(), original.getMouseY());
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

    public void onClick(Consumer<MPGuiMouseClickEvent<T>> consumer) {
        onClick = consumer;
    }

    public void onDrag(Consumer<MPGuiMouseDragEvent<T>> consumer) {
        onDrag = consumer;
    }

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

    private void recalculateKnobPosition() {
        MutableGuiShape inner = getCalculatedElementShape();
        float trackLength = isVertical
                ? inner.height() - knob.getElementShape().height()
                : inner.width() - knob.getElementShape().width();

        if (trackLength <= 0) {
            progress = 0f;
            value = min;
        }

        float knobPrimary = progress * trackLength;
        float knobSecondary = isVertical
                ? (inner.width() - knob.getElementShape().width()) / 2f
                : (inner.height() - knob.getElementShape().height()) / 2f;

        MutableGuiShape knobShape = knob.getElementShape();
        if (isVertical) knobShape.withX(knobSecondary).withY(knobPrimary);
        else knobShape.withX(knobPrimary).withY(knobSecondary);

        if (lastParentDefaultSize != null && lastParentContentSize != null) {
            knob.calculate(lastParentDefaultSize, lastParentContentSize, inner);
        } else {
            knob.calculate(new GuiVector(inner.width(), inner.height()),
                    new GuiVector(inner.width(), inner.height()),
                    inner);
        }
    }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        super.calculate(parentDefaultSize, parentContentSize, available);

        lastParentDefaultSize = parentDefaultSize;
        lastParentContentSize = parentContentSize;

        recalculateKnobPosition();
    }
}