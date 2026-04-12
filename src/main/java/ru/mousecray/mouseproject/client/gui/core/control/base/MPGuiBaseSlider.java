/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control.base;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.core.component.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.core.component.sound.MPSoundSourceType;
import ru.mousecray.mouseproject.client.gui.core.component.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiEventFactory;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseDragEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiSliderChangedEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiBaseSlider<T extends MPGuiBaseSlider<T>> extends MPGuiPanel<T> {
    private final MPGuiButton<?> knob;
    private final MPGuiButton<?> track;

    private       int value;
    private final int min, max, range;

    private MPOrientation orientation;
    private float         progress = 0f;

    private final MPGuiSliderChangedEvent<T> sliderChangedEvent = new MPGuiSliderChangedEvent<>();

    private Consumer<MPGuiSliderChangedEvent<T>> onSliderChangedListener = null;

    private IGuiVector lastParentDefaultSize, lastParentContentSize;

    public MPGuiBaseSlider(MPGuiShape iShape, float knobWidth, float knobHeight, int min, int max, MPOrientation orientation) {
        super(iShape);
        this.orientation = orientation;
        this.min = min;
        this.max = Math.max(max, min);
        range = this.max - min;

        Minecraft mc = Minecraft.getMinecraft();
        sliderChangedEvent.bind(mc, self());

        class TrackButton extends MPGuiButton<TrackButton> {
            public TrackButton() {
                super(iShape);
                setSoundPack(MPGuiSoundPack.Builder
                        .create()
                        .addSound(MPSoundSourceType.PRESS, SoundEvents.UI_BUTTON_CLICK)
                        .build()
                );
            }

            @Override public void onClick(MPGuiMouseClickEvent<TrackButton> e) { updateFromMouse(e.getMouseX(), e.getMouseY()); }
        }

        track = new TrackButton();
        track.setScaleRules(new MPGuiScaleRules(MPGuiScaleType.PARENT));
        addChild(track);

        class KnobButton extends MPGuiButton<KnobButton> {
            public KnobButton() {
                super(new MPGuiShape(0, 0, knobWidth, knobHeight));
                setSoundPack(MPGuiSoundPack.Builder
                        .create()
                        .addSound(MPSoundSourceType.PRESS, SoundEvents.UI_BUTTON_CLICK)
                        .build()
                );
            }

            @Override
            protected void onMouseDragged(MPGuiMouseDragEvent<KnobButton> e) {
                if (e.isCancelled()) return;
                updateFromMouse(e.getMouseX(), e.getMouseY());
            }

            @Override
            public void onClick(MPGuiMouseClickEvent<KnobButton> e) {
                if (e.isCancelled()) return;
                updateFromMouse(e.getMouseX(), e.getMouseY());
            }
        }

        knob = new KnobButton();
        addChild(knob);

        updateOrientationState();
        setValueInternal(min, false, 0, 0);
    }

    public void setTrackTexturePack(MPGuiTexturePack pack) { track.setTexturePack(pack); }
    public void setKnobTexturePack(MPGuiTexturePack pack)  { knob.setTexturePack(pack); }
    public MPGuiTexturePack getTrackTexturePack()          { return track.getTexturePack(); }
    public MPGuiTexturePack getKnobTexturePack()           { return knob.getTexturePack(); }

    public MPOrientation getOrientation()                  { return orientation; }

    public void setOrientation(MPOrientation orientation) {
        if (getScreen() != null) {
            MouseProject.LOGGER.warn(
                    "Orientation cannot be setup immediately to MPGuiBaseSlider that added to container." +
                            " It set now, but actual element size will be updated on the next gui size calculation."
            );
        }
        if (this.orientation != orientation) {
            this.orientation = orientation;
            updateOrientationState();
            onOrientationChanged();
        }
    }

    private void updateOrientationState() {
        boolean isVert = orientation == MPOrientation.VERTICAL;
        knob.setScaleRules(new MPGuiScaleRules(isVert ? MPGuiScaleType.ORIGIN_HORIZONTAL : MPGuiScaleType.ORIGIN_VERTICAL));
        recalculateKnobPosition();
    }

    protected void onOrientationChanged() { }

    private void updateFromMouse(int mouseX, int mouseY) {
        MPMutableGuiShape inner = getCalculatedShape();

        float   knobW  = knob.getCalculatedShape().width();
        float   knobH  = knob.getCalculatedShape().height();
        boolean isVert = orientation == MPOrientation.VERTICAL;

        float trackLength = isVert ? inner.height() - knobH : inner.width() - knobW;

        if (trackLength <= 0) return;

        float rel = isVert ? (mouseY - inner.y()) - knobH / 2f : (mouseX - inner.x()) - knobW / 2f;

        float newProgress = MathHelper.clamp(rel / trackLength, 0f, 1f);
        if (Float.compare(progress, newProgress) == 0) return;

        int newValue = min + Math.round(newProgress * range);
        if (value != newValue) setValueInternal(newValue, true, mouseX, mouseY);
    }

    public void setOnSliderChangedListener(@Nullable Consumer<MPGuiSliderChangedEvent<T>> listener) {
        onSliderChangedListener = listener;
    }

    public Consumer<MPGuiSliderChangedEvent<T>> getOnSliderChangedListener() { return onSliderChangedListener; }
    protected void onSliderChanged(MPGuiSliderChangedEvent<T> event)         { }

    public int getValue()                                                    { return value; }
    public void setValue(int newValue)                                       { setValueInternal(newValue, true, 0, 0); }
    public void setValue(int newValue, boolean notify)                       { setValueInternal(newValue, notify, 0, 0); }

    private void setValueInternal(int newValue, boolean notify, int mouseX, int mouseY) {
        newValue = MathHelper.clamp(newValue, min, max);
        if (value == newValue) return;

        int oldValue = value;
        value = newValue;
        progress = range == 0 ? 0f : (float) (newValue - min) / range;

        if (notify) {
            MPGuiEventFactory.pushSliderChangedEvent(sliderChangedEvent, mouseX, mouseY, oldValue, value);
            onAnyEventFire(sliderChangedEvent);
            if (!sliderChangedEvent.isCancelled()) {
                onSliderChanged(sliderChangedEvent);
                if (onSliderChangedListener != null) onSliderChangedListener.accept(sliderChangedEvent);
            } else {
                value = oldValue;
                progress = range == 0 ? 0f : (float) (value - min) / range;
            }
        }
        recalculateKnobPosition();
    }

    public float getProgress() { return progress; }

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

        boolean isVert      = orientation == MPOrientation.VERTICAL;
        float   trackLength = isVert ? inner.height() - knobH : inner.width() - knobW;

        if (trackLength <= 0) {
            progress = 0f;
            value = min;
            trackLength = 0;
        }

        float knobPrimary   = progress * trackLength;
        float knobSecondary = isVert ? (inner.width() - knobW) / 2f : (inner.height() - knobH) / 2f;

        float knobX = inner.x() + (isVert ? knobSecondary : knobPrimary);
        float knobY = inner.y() + (isVert ? knobPrimary : knobSecondary);

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