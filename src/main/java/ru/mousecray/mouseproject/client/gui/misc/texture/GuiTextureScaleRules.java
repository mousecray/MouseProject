/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.misc.texture;

import java.util.*;

public class GuiTextureScaleRules {
    private final Set<GuiTextureScaleType> scaleTypes = new HashSet<>();

    public GuiTextureScaleRules(GuiTextureScaleType... types) {
        for (GuiTextureScaleType type : types) addType(type);
    }

    private void addType(GuiTextureScaleType newType) {
        List<GuiTextureScaleType> toRemove = new ArrayList<>();
        for (GuiTextureScaleType existing : scaleTypes) {
            if (isIncompatible(existing, newType)) toRemove.add(existing);
        }
        toRemove.forEach(scaleTypes::remove);

        scaleTypes.add(newType);

        if (scaleTypes.contains(GuiTextureScaleType.STRETCH)) {
            scaleTypes.remove(GuiTextureScaleType.STRETCH_HORIZONTAL);
            scaleTypes.remove(GuiTextureScaleType.STRETCH_VERTICAL);
        }
        if (scaleTypes.contains(GuiTextureScaleType.FILL)) {
            scaleTypes.remove(GuiTextureScaleType.FILL_HORIZONTAL);
            scaleTypes.remove(GuiTextureScaleType.FILL_VERTICAL);
        }

        if (scaleTypes.contains(GuiTextureScaleType.STRETCH_HORIZONTAL) && scaleTypes.contains(GuiTextureScaleType.STRETCH_VERTICAL)) {
            scaleTypes.remove(GuiTextureScaleType.STRETCH_HORIZONTAL);
            scaleTypes.remove(GuiTextureScaleType.STRETCH_VERTICAL);
            addType(GuiTextureScaleType.STRETCH);
        }
        if (scaleTypes.contains(GuiTextureScaleType.FILL_HORIZONTAL) && scaleTypes.contains(GuiTextureScaleType.FILL_VERTICAL)) {
            scaleTypes.remove(GuiTextureScaleType.FILL_HORIZONTAL);
            scaleTypes.remove(GuiTextureScaleType.FILL_VERTICAL);
            addType(GuiTextureScaleType.FILL);
        }
    }

    private boolean isIncompatible(GuiTextureScaleType a, GuiTextureScaleType b) {
        GuiTextureScaleType.Category catA = a.getCategory();
        GuiTextureScaleType.Category catB = b.getCategory();
        if (catA == catB) return false;

        return axesOverlap(a, b);
    }

    private boolean axesOverlap(GuiTextureScaleType a, GuiTextureScaleType b) {
        Set<GuiTextureScaleType.Axes> axesA = a.getAxes();
        Set<GuiTextureScaleType.Axes> axesB = b.getAxes();
        axesA.retainAll(axesB);
        return !axesA.isEmpty();
    }

    public boolean isStretchHorizontal()            { return scaleTypes.contains(GuiTextureScaleType.STRETCH) || scaleTypes.contains(GuiTextureScaleType.STRETCH_HORIZONTAL); }
    public boolean isStretchVertical()              { return scaleTypes.contains(GuiTextureScaleType.STRETCH) || scaleTypes.contains(GuiTextureScaleType.STRETCH_VERTICAL); }
    public boolean isFillHorizontal()               { return scaleTypes.contains(GuiTextureScaleType.FILL) || scaleTypes.contains(GuiTextureScaleType.FILL_HORIZONTAL); }
    public boolean isFillVertical()                 { return scaleTypes.contains(GuiTextureScaleType.FILL) || scaleTypes.contains(GuiTextureScaleType.FILL_VERTICAL); }

    public Set<GuiTextureScaleType> getScaleTypes() { return Collections.unmodifiableSet(scaleTypes); }
}