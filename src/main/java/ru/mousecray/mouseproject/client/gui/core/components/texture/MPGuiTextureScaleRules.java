/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.components.texture;

import java.util.*;

public class MPGuiTextureScaleRules {
    private final Set<MPGuiTextureScaleType> scaleTypes  = new HashSet<>();
    private       float                      multiplierX = 1.0f;
    private       float                      multiplierY = 1.0f;

    public MPGuiTextureScaleRules(MPGuiTextureScaleType... types) {
        for (MPGuiTextureScaleType type : types) addType(type);
    }

    private void addType(MPGuiTextureScaleType newType) {
        List<MPGuiTextureScaleType> toRemove = new ArrayList<>();
        for (MPGuiTextureScaleType existing : scaleTypes) {
            if (isIncompatible(existing, newType)) toRemove.add(existing);
        }
        toRemove.forEach(scaleTypes::remove);

        scaleTypes.add(newType);

        if (scaleTypes.contains(MPGuiTextureScaleType.STRETCH)) {
            scaleTypes.remove(MPGuiTextureScaleType.STRETCH_HORIZONTAL);
            scaleTypes.remove(MPGuiTextureScaleType.STRETCH_VERTICAL);
        }
        if (scaleTypes.contains(MPGuiTextureScaleType.FILL)) {
            scaleTypes.remove(MPGuiTextureScaleType.FILL_HORIZONTAL);
            scaleTypes.remove(MPGuiTextureScaleType.FILL_VERTICAL);
        }
    }

    private boolean isIncompatible(MPGuiTextureScaleType a, MPGuiTextureScaleType b) {
        MPGuiTextureScaleType.Category catA = a.getCategory();
        MPGuiTextureScaleType.Category catB = b.getCategory();

        if (catA == catB && axesOverlap(a, b)) return true;
        return axesOverlap(a, b);
    }

    private boolean axesOverlap(MPGuiTextureScaleType a, MPGuiTextureScaleType b) {
        Set<MPGuiTextureScaleType.Axes> axesA = a.getAxes();
        Set<MPGuiTextureScaleType.Axes> axesB = b.getAxes();
        axesA.retainAll(axesB);
        return !axesA.isEmpty();
    }

    public MPGuiTextureScaleRules setMultipliers(float multiplierX, float multiplierY) {
        this.multiplierX = multiplierX;
        this.multiplierY = multiplierY;
        return this;
    }

    public MPGuiTextureScaleRules setMultiplayerX(float multiplierX) {
        this.multiplierX = multiplierX;
        return this;
    }

    public MPGuiTextureScaleRules setMultiplayerY(float multiplierY) {
        this.multiplierY = multiplierY;
        return this;
    }

    public MPGuiTextureScaleRules setMultiplier(float multiplier) {
        return setMultipliers(multiplier, multiplier);
    }

    public float getMultiplierX() { return multiplierX; }
    public float getMultiplierY() { return multiplierY; }

    public ScaleMode getModeX() {
        if (scaleTypes.contains(MPGuiTextureScaleType.FILL) || scaleTypes.contains(MPGuiTextureScaleType.FILL_HORIZONTAL))
            return ScaleMode.FILL;
        if (scaleTypes.stream().anyMatch(t -> t.getCategory() == MPGuiTextureScaleType.Category.SINGLE && t.getAxes().contains(MPGuiTextureScaleType.Axes.HORIZONTAL)))
            return ScaleMode.SINGLE;
        return ScaleMode.STRETCH;
    }

    public ScaleMode getModeY() {
        if (scaleTypes.contains(MPGuiTextureScaleType.FILL) || scaleTypes.contains(MPGuiTextureScaleType.FILL_VERTICAL))
            return ScaleMode.FILL;
        if (scaleTypes.stream().anyMatch(t -> t.getCategory() == MPGuiTextureScaleType.Category.SINGLE && t.getAxes().contains(MPGuiTextureScaleType.Axes.VERTICAL)))
            return ScaleMode.SINGLE;
        return ScaleMode.STRETCH;
    }

    public TextureAnchor getAnchorX() {
        if (scaleTypes.contains(MPGuiTextureScaleType.SINGLE_HORIZONTAL_CENTER)) return TextureAnchor.CENTER;
        if (scaleTypes.contains(MPGuiTextureScaleType.SINGLE_HORIZONTAL_RIGHT)) return TextureAnchor.MAX;
        return TextureAnchor.MIN;
    }

    public TextureAnchor getAnchorY() {
        if (scaleTypes.contains(MPGuiTextureScaleType.SINGLE_VERTICAL_CENTER)) return TextureAnchor.CENTER;
        if (scaleTypes.contains(MPGuiTextureScaleType.SINGLE_VERTICAL_BOTTOM)) return TextureAnchor.MAX;
        return TextureAnchor.MIN;
    }

    public Set<MPGuiTextureScaleType> getScaleTypes() { return Collections.unmodifiableSet(scaleTypes); }

    public enum ScaleMode {STRETCH, FILL, SINGLE}

    public enum TextureAnchor {MIN, CENTER, MAX}
}