/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.core.components.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTextureScaleRules;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTextureScaleType;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseDragEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiScrollbar extends MPGuiPanel<MPGuiScrollbar> {

    // --- КОНСТАНТЫ ТЕКСТУР ---
    private static final float TEX_W          = 10f;
    private static final float TEX_H          = 10f;
    private static final float STATE_V_OFFSET = 10f;

    private static final float ARROW_UP_U = 0f, ARROW_UP_V = 0f;
    private static final float ARROW_DOWN_U = 10f, ARROW_DOWN_V = 0f;
    private static final float ARROW_LEFT_U = 20f, ARROW_LEFT_V = 0f;
    private static final float ARROW_RIGHT_U = 30f, ARROW_RIGHT_V = 0f;

    private static final float THUMB_BG_U = 40f, THUMB_BG_V = 0f;
    private static final float THUMB_LINES_U = 50f, THUMB_LINES_V = 0f;

    private final MPOrientation orientation;

    private final ScrollbarArrow minusButton;
    private final ScrollbarArrow plusButton;
    private final ScrollbarThumb thumb;

    private float contentSize  = 1f;
    private float viewportSize = 1f;
    private float scrollValue  = 0f;
    private float scrollStep   = 20f;

    private Consumer<Float> onScroll;

    public MPGuiScrollbar(MPGuiShape shape, MPOrientation orientation) {
        super(shape);
        this.orientation = orientation;

        boolean isVert = orientation == MPOrientation.VERTICAL;

        MPGuiTextureScaleRules trackRules = isVert
                ? new MPGuiTextureScaleRules(MPGuiTextureScaleType.FILL_VERTICAL, MPGuiTextureScaleType.SINGLE_HORIZONTAL_LEFT).setMultipliers(0.7f, 0.5f)
                : new MPGuiTextureScaleRules(MPGuiTextureScaleType.FILL_HORIZONTAL, MPGuiTextureScaleType.SINGLE_VERTICAL_TOP).setMultipliers(0.5f, 0.7f);

        setTexturePack(MPGuiTexturePack.Builder
                .create(MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE, MPGuiVector.of(230, 0), MPGuiVector.of(18, 7))
                .setScaleRules(trackRules)
                .addTexture(0)
                .build());

        minusButton = new ScrollbarArrow(true);
        plusButton = new ScrollbarArrow(false);
        thumb = new ScrollbarThumb();

        addChild(minusButton);
        addChild(plusButton);
        addChild(thumb);
    }

    @Override
    public void onClick(MPGuiMouseClickEvent<MPGuiScrollbar> event) {
        boolean isVert   = orientation == MPOrientation.VERTICAL;
        float   clickPos = isVert ? (event.getMouseY() - getCalculatedShape().y()) : (event.getMouseX() - getCalculatedShape().x());
        float   thumbPos = isVert ? (thumb.getCalculatedShape().y() - getCalculatedShape().y()) : (thumb.getCalculatedShape().x() - getCalculatedShape().x());
        float   thumbEnd = thumbPos + (isVert ? thumb.getCalculatedShape().height() : thumb.getCalculatedShape().width());

        if (clickPos < thumbPos) {
            setScrollValue(scrollValue - viewportSize, true);
        } else if (clickPos > thumbEnd) {
            setScrollValue(scrollValue + viewportSize, true);
        }
    }

    public MPGuiScrollbar setOnScroll(Consumer<Float> onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    public void updateSizes(float viewportSize, float contentSize) {
        this.viewportSize = viewportSize;
        this.contentSize = contentSize;
        recalculateThumb();
    }

    public void setScrollValue(float value, boolean notify) {
        float maxScroll = Math.max(0, contentSize - viewportSize);
        value = Math.max(0, Math.min(value, maxScroll));

        if (Float.compare(scrollValue, value) != 0) {
            scrollValue = value;
            recalculateThumb();
            if (notify && onScroll != null) onScroll.accept(scrollValue);
        }
    }

    public float getScrollValue() { return scrollValue; }

    private void recalculateThumb() {
        MPMutableGuiShape inner = getCalculatedShape();
        if (inner.width() <= 0 || inner.height() <= 0) return;

        boolean isVert      = orientation == MPOrientation.VERTICAL;
        float   thickness   = isVert ? inner.width() : inner.height();
        float   totalLength = isVert ? inner.height() : inner.width();

        float trackSize = totalLength - (thickness * 2);
        float maxScroll = Math.max(0, contentSize - viewportSize);

        if (maxScroll <= 0 || trackSize <= 0) {
            thumb.getStateManager().add(MPGuiElementState.DISABLED);
            thumb.getStateManager().add(MPGuiElementState.HIDDEN);
        } else {
            thumb.getStateManager().remove(MPGuiElementState.DISABLED);
            thumb.getStateManager().remove(MPGuiElementState.HIDDEN);

            float minThumbSize = thickness;
            float thumbRatio   = viewportSize / contentSize;
            float thumbLength  = Math.max(minThumbSize, trackSize * thumbRatio);

            float scrollRatio = scrollValue / maxScroll;
            float thumbOffset = thickness + (scrollRatio * (trackSize - thumbLength));

            if (isVert) {
                thumb.getShape().withWidth(thickness).withHeight(thumbLength).withX(0).withY(thumbOffset);
            } else {
                thumb.getShape().withWidth(thumbLength).withHeight(thickness).withX(thumbOffset).withY(0);
            }
        }
    }

    @Override
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MPMutableGuiShape inner) {
        boolean isVert      = orientation == MPOrientation.VERTICAL;
        float   thickness   = isVert ? inner.width() : inner.height();
        float   totalLength = isVert ? inner.height() : inner.width();

        childAvailableTemp.withX(inner.x()).withY(inner.y()).withWidth(thickness).withHeight(thickness);
        minusButton.getShape().withWidth(thickness).withHeight(thickness);
        minusButton.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

        if (isVert) {
            childAvailableTemp.withX(inner.x()).withY(inner.y() + totalLength - thickness);
        } else {
            childAvailableTemp.withX(inner.x() + totalLength - thickness).withY(inner.y());
        }
        plusButton.getShape().withWidth(thickness).withHeight(thickness);
        plusButton.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

        recalculateThumb();
        childAvailableTemp.withX(inner.x() + thumb.getShape().x())
                .withY(inner.y() + thumb.getShape().y())
                .withWidth(thumb.getShape().width())
                .withHeight(thumb.getShape().height());
        thumb.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);
    }

    private class ScrollbarArrow extends MPGuiButton<ScrollbarArrow> {
        private final boolean isMinus;

        public ScrollbarArrow(boolean isMinus) {
            super(new MPGuiShape(0, 0, 0, 0));
            this.isMinus = isMinus;
            setSoundPack(MPGuiSoundPack.CONTROL_SIMPLE());
            setScaleRules(new GuiScaleRules(MPGuiScaleType.FIXED));
        }

        @Override public void setTexturePack(MPGuiTexturePack pack) { }

        @Override
        public void onClick(MPGuiMouseClickEvent<ScrollbarArrow> event) {
            if (isMinus) setScrollValue(scrollValue - scrollStep, true);
            else setScrollValue(scrollValue + scrollStep, true);
        }

        @Override
        protected void onDrawBackground(MPGuiTickEvent<ScrollbarArrow> event) {
            event.getMc().getTextureManager().bindTexture(MPStaticData.CONTROLS_TEXTURES);

            int stateIdx = 0;
            if (getStateManager().has(MPGuiElementState.PRESSED)) stateIdx = 2;
            else if (getStateManager().has(MPGuiElementState.HOVERED)) stateIdx = 1;

            boolean isVert = orientation == MPOrientation.VERTICAL;
            float   u, v;
            if (isVert) {
                u = isMinus ? ARROW_UP_U : ARROW_DOWN_U;
                v = (isMinus ? ARROW_UP_V : ARROW_DOWN_V) + (stateIdx * STATE_V_OFFSET);
            } else {
                u = isMinus ? ARROW_LEFT_U : ARROW_RIGHT_U;
                v = (isMinus ? ARROW_LEFT_V : ARROW_RIGHT_V) + (stateIdx * STATE_V_OFFSET);
            }

            MPGuiRenderHelper.drawTexture(
                    getCalculatedShape().x(), getCalculatedShape().y(),
                    u, v, TEX_W, TEX_H,
                    getCalculatedShape().width(), getCalculatedShape().height(),
                    MPStaticData.CONTROLS_TEXTURES_SIZE.x(), MPStaticData.CONTROLS_TEXTURES_SIZE.y()
            );
        }
    }

    private class ScrollbarThumb extends MPGuiButton<ScrollbarThumb> {
        public ScrollbarThumb() {
            super(new MPGuiShape(0, 0, 0, 0));
            setSoundPack(MPGuiSoundPack.CONTROL_SIMPLE());
            setScaleRules(new GuiScaleRules(MPGuiScaleType.FIXED));
        }

        @Override public void setTexturePack(MPGuiTexturePack pack) { }

        @Override
        protected void onMouseDragged(MPGuiMouseDragEvent<ScrollbarThumb> e) {
            if (e.isCancelled()) return;

            float maxScroll = Math.max(0, contentSize - viewportSize);
            if (maxScroll <= 0) return;

            boolean isVert           = orientation == MPOrientation.VERTICAL;
            float   thickness        = isVert ? getCalculatedShape().width() : getCalculatedShape().height();
            float   totalTrackLength = isVert ? MPGuiScrollbar.this.getCalculatedShape().height() : MPGuiScrollbar.this.getCalculatedShape().width();

            float trackSize       = totalTrackLength - (thickness * 2);
            float thumbLength     = isVert ? getCalculatedShape().height() : getCalculatedShape().width();
            float scrollableTrack = trackSize - thumbLength;

            if (scrollableTrack <= 0) return;

            float diff        = isVert ? e.getDiffY() : e.getDiffX();
            float moveRatio   = diff / scrollableTrack;
            float scrollDelta = moveRatio * maxScroll;

            setScrollValue(scrollValue + scrollDelta, true);
            e.consume();
        }

        @Override
        public void onClick(MPGuiMouseClickEvent<ScrollbarThumb> event) { }

        @Override
        protected void onDrawBackground(MPGuiTickEvent<ScrollbarThumb> event) {
            event.getMc().getTextureManager().bindTexture(MPStaticData.CONTROLS_TEXTURES);

            float x = getCalculatedShape().x();
            float y = getCalculatedShape().y();
            float w = getCalculatedShape().width();
            float h = getCalculatedShape().height();

            int stateIdx = 0;
            if (getStateManager().has(MPGuiElementState.PRESSED)) stateIdx = 2;
            else if (getStateManager().has(MPGuiElementState.HOVERED)) stateIdx = 1;

            float vOffset = stateIdx * STATE_V_OFFSET;

            MPGuiRenderHelper.drawTexture(
                    x, y, THUMB_BG_U, THUMB_BG_V + vOffset, TEX_W, TEX_H, w, h,
                    MPStaticData.CONTROLS_TEXTURES_SIZE.x(), MPStaticData.CONTROLS_TEXTURES_SIZE.y()
            );

            boolean isVert    = orientation == MPOrientation.VERTICAL;
            float   linesSize = isVert ? w : h;
            float   linesX    = x + (w - linesSize) / 2f;
            float   linesY    = y + (h - linesSize) / 2f;

            MPGuiRenderHelper.drawTexture(
                    linesX, linesY, THUMB_LINES_U, THUMB_LINES_V + vOffset, TEX_W, TEX_H, linesSize, linesSize,
                    MPStaticData.CONTROLS_TEXTURES_SIZE.x(), MPStaticData.CONTROLS_TEXTURES_SIZE.y()
            );
        }
    }
}