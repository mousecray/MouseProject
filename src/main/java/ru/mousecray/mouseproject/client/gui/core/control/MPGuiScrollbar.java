/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.core.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.core.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiMouseDragEvent;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTickEvent;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiScrollbar extends MPGuiPanel<MPGuiScrollbar> {
    private MPOrientation orientation = MPOrientation.VERTICAL;

    private final ScrollbarArrow minusButton;
    private final ScrollbarArrow plusButton;
    private final ScrollbarThumb thumb;

    private MPGuiTexturePack thumbForegroundTexturePack = MPGuiTexturePack.EMPTY();

    private float contentSize  = 1f;
    private float viewportSize = 1f;
    private float scrollValue  = 0f;
    private float scrollStep   = 20f;

    private Consumer<Float> onScroll;

    public MPGuiScrollbar(MPGuiShape shape) {
        super(shape);

        minusButton = new ScrollbarArrow(true);
        plusButton = new ScrollbarArrow(false);
        thumb = new ScrollbarThumb();

        addChild(minusButton);
        addChild(plusButton);
        addChild(thumb);

        updateOrientationState();
    }

    public MPOrientation getOrientation() { return orientation; }

    public void setOrientation(MPOrientation orientation) {
        if (getScreen() != null) {
            MouseProject.LOGGER.warn(
                    "Orientation cannot be setup immediately to MPGuiScrollbar that added to container." +
                            " It set now, but actual element size will be updated on the next gui size calculation."
            );
        }
        if (this.orientation != orientation) {
            this.orientation = orientation;
            updateOrientationState();
        }
    }

    private void updateOrientationState() {
        MPGuiScaleType scaleType = orientation == MPOrientation.VERTICAL ? MPGuiScaleType.PARENT_HORIZONTAL : MPGuiScaleType.PARENT_VERTICAL;
        minusButton.setScaleRules(new MPGuiScaleRules(scaleType));
        plusButton.setScaleRules(new MPGuiScaleRules(scaleType));
        thumb.setScaleRules(new MPGuiScaleRules(scaleType));

        boolean     isVert   = orientation == MPOrientation.VERTICAL;
        MPGuiVector iconSize = MPGuiVector.of(10, 10);

        setMinusArrowTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        isVert ? MPGuiVector.of(0, 0) : MPGuiVector.of(20, 0), iconSize
                )
                .addTexture(0).addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .build()
        );

        setPlusArrowTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        isVert ? MPGuiVector.of(10, 0) : MPGuiVector.of(30, 0), iconSize
                )
                .addTexture(0).addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .build()
        );

        setThumbTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(40, 0), iconSize
                )
                .addTexture(0)
                .addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .build()
        );

        setThumbForegroundTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(50, 0), iconSize
                )
                .addTexture(0)
                .addTexture(1, MPGuiElementState.HOVERED)
                .addTexture(2, MPGuiElementState.PRESSED)
                .build()
        );
    }

    public MPGuiTexturePack getMinusArrowTexturePack() { return minusButton.getTexturePack(); }

    public void setMinusArrowTexturePack(MPGuiTexturePack pack) {
        Objects.requireNonNull(pack, "texturePack cannot be null. Use MPGuiTexturePack.EMPTY() instead.");
        minusButton.setTexturePack(pack);
    }

    public MPGuiTexturePack getPlusArrowTexturePack() { return plusButton.getTexturePack(); }

    public void setPlusArrowTexturePack(MPGuiTexturePack pack) {
        Objects.requireNonNull(pack, "texturePack cannot be null. Use MPGuiTexturePack.EMPTY() instead.");
        plusButton.setTexturePack(pack);
    }

    public MPGuiTexturePack getThumbTexturePack() { return thumb.getTexturePack(); }

    public void setThumbTexturePack(MPGuiTexturePack pack) {
        Objects.requireNonNull(pack, "texturePack cannot be null. Use MPGuiTexturePack.EMPTY() instead.");
        thumb.setTexturePack(pack);
    }

    public MPGuiTexturePack getThumbForegroundTexturePack() { return thumbForegroundTexturePack; }

    public void setThumbForegroundTexturePack(MPGuiTexturePack pack) {
        Objects.requireNonNull(pack, "texturePack cannot be null. Use MPGuiTexturePack.EMPTY() instead.");
        thumbForegroundTexturePack = pack;
    }

    @Override
    public void onClick(MPGuiMouseClickEvent<MPGuiScrollbar> event) {
        boolean isVert   = orientation == MPOrientation.VERTICAL;
        float   clickPos = isVert ? (event.getMouseY() - getCalculatedShape().y()) : (event.getMouseX() - getCalculatedShape().x());
        float thumbPos = isVert ? (thumb.getCalculatedShape().y() - getCalculatedShape().y())
                : (thumb.getCalculatedShape().x() - getCalculatedShape().x());
        float thumbEnd = thumbPos + (isVert ? thumb.getCalculatedShape().height() : thumb.getCalculatedShape().width());

        if (clickPos < thumbPos) setScrollValue(scrollValue - viewportSize, true);
        else if (clickPos > thumbEnd) setScrollValue(scrollValue + viewportSize, true);
    }

    public void setOnScroll(Consumer<Float> onScroll) { this.onScroll = onScroll; }
    public void setScrollStep(float step)             { scrollStep = step; }

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

            float thumbRatio  = viewportSize / contentSize;
            float thumbLength = Math.max(thickness, trackSize * thumbRatio);

            float scrollRatio = scrollValue / maxScroll;
            float thumbOffset = thickness + (scrollRatio * (trackSize - thumbLength));

            if (isVert) thumb.getShape().withWidth(thickness).withHeight(thumbLength).withX(0).withY(thumbOffset);
            else thumb.getShape().withWidth(thumbLength).withHeight(thickness).withX(thumbOffset).withY(0);
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

        if (isVert) childAvailableTemp.withX(inner.x()).withY(inner.y() + totalLength - thickness);
        else childAvailableTemp.withX(inner.x() + totalLength - thickness).withY(inner.y());

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
        }

        @Override
        public void onClick(MPGuiMouseClickEvent<ScrollbarArrow> event) {
            if (isMinus) setScrollValue(scrollValue - scrollStep, true);
            else setScrollValue(scrollValue + scrollStep, true);
        }
    }

    private class ScrollbarThumb extends MPGuiButton<ScrollbarThumb> {
        public ScrollbarThumb() {
            super(new MPGuiShape(0, 0, 0, 0));
        }

        @Override
        protected void onMouseDragged(MPGuiMouseDragEvent<ScrollbarThumb> e) {
            if (e.isCancelled()) return;

            float maxScroll = Math.max(0, contentSize - viewportSize);
            if (maxScroll <= 0) return;

            boolean isVert          = orientation == MPOrientation.VERTICAL;
            float   scrollableTrack = getScrollableTrack(isVert);

            if (scrollableTrack <= 0) return;

            float diff        = isVert ? e.getDiffY() : e.getDiffX();
            float moveRatio   = diff / scrollableTrack;
            float scrollDelta = moveRatio * maxScroll;

            setScrollValue(scrollValue + scrollDelta, true);
            e.consume();
        }

        private float getScrollableTrack(boolean isVert) {
            float thickness = isVert ? getCalculatedShape().width() : getCalculatedShape().height();
            float totalTrackLength = isVert ? MPGuiScrollbar.this.getCalculatedShape().height()
                    : MPGuiScrollbar.this.getCalculatedShape().width();

            float trackSize   = totalTrackLength - (thickness * 2);
            float thumbLength = isVert ? getCalculatedShape().height() : getCalculatedShape().width();
            return trackSize - thumbLength;
        }

        @Override
        public void onClick(MPGuiMouseClickEvent<ScrollbarThumb> event) { }

        @Override
        protected void onDrawForeground(MPGuiTickEvent<ScrollbarThumb> event) {
            super.onDrawForeground(event);

            MPGuiTexture fgTex = thumbForegroundTexturePack.getCalculatedTexture(getStateManager());
            if (fgTex != null) {
                float   w      = getCalculatedShape().width();
                float   h      = getCalculatedShape().height();
                boolean isVert = orientation == MPOrientation.VERTICAL;

                float linesSize = isVert ? w : h;
                float linesX    = getCalculatedShape().x() + (w - linesSize) / 2f;
                float linesY    = getCalculatedShape().y() + (h - linesSize) / 2f;

                fgTex.draw(event.getMc(), linesX, linesY, linesSize, linesSize);
            }
        }
    }
}