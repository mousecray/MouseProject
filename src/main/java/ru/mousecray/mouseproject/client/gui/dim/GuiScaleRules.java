/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.dim;

import java.util.*;

public class GuiScaleRules {
    private final Set<GuiScaleType> scaleTypes = new HashSet<>();

    public GuiScaleRules(GuiScaleType... types) { for (GuiScaleType type : types) addType(type); }

    private void addType(GuiScaleType newType) {
        List<GuiScaleType> toRemove = new ArrayList<>();
        for (GuiScaleType existing : scaleTypes) if (isIncompatible(existing, newType)) toRemove.add(existing);
        toRemove.forEach(scaleTypes::remove);

        scaleTypes.add(newType);

        if (newType == GuiScaleType.ORIGIN_HORIZONTAL) addType(GuiScaleType.FLOW_HORIZONTAL);
        else if (newType == GuiScaleType.ORIGIN_VERTICAL) addType(GuiScaleType.FLOW_VERTICAL);

        if (scaleTypes.contains(GuiScaleType.FIXED)) {
            scaleTypes.remove(GuiScaleType.FIXED_HORIZONTAL);
            scaleTypes.remove(GuiScaleType.FIXED_VERTICAL);
        }
        if (scaleTypes.contains(GuiScaleType.FLOW)) {
            scaleTypes.remove(GuiScaleType.FLOW_HORIZONTAL);
            scaleTypes.remove(GuiScaleType.FLOW_VERTICAL);
        }
        if (scaleTypes.contains(GuiScaleType.PARENT)) {
            scaleTypes.remove(GuiScaleType.PARENT_HORIZONTAL);
            scaleTypes.remove(GuiScaleType.PARENT_VERTICAL);
        }

        if (scaleTypes.contains(GuiScaleType.FIXED_HORIZONTAL) && scaleTypes.contains(GuiScaleType.FIXED_VERTICAL)) {
            scaleTypes.remove(GuiScaleType.FIXED_HORIZONTAL);
            scaleTypes.remove(GuiScaleType.FIXED_VERTICAL);
            addType(GuiScaleType.FIXED);
        }
        if (scaleTypes.contains(GuiScaleType.FLOW_HORIZONTAL) && scaleTypes.contains(GuiScaleType.FLOW_VERTICAL)) {
            scaleTypes.remove(GuiScaleType.FLOW_HORIZONTAL);
            scaleTypes.remove(GuiScaleType.FLOW_VERTICAL);
            addType(GuiScaleType.FLOW);
        }
        if (scaleTypes.contains(GuiScaleType.PARENT_HORIZONTAL) && scaleTypes.contains(GuiScaleType.PARENT_VERTICAL)) {
            scaleTypes.remove(GuiScaleType.PARENT_HORIZONTAL);
            scaleTypes.remove(GuiScaleType.PARENT_VERTICAL);
            addType(GuiScaleType.PARENT);
        }
    }

    private boolean isIncompatible(GuiScaleType a, GuiScaleType b) {
        GuiScaleType.Category catA = a.getCategory();
        GuiScaleType.Category catB = b.getCategory();
        if (catA == catB) return catA == GuiScaleType.Category.ORIGIN && a != b;

        boolean isFixedVsOther = (catA == GuiScaleType.Category.FIXED && (catB == GuiScaleType.Category.FLOW || catB == GuiScaleType.Category.ORIGIN || catB == GuiScaleType.Category.PARENT)) ||
                (catB == GuiScaleType.Category.FIXED && (catA == GuiScaleType.Category.FLOW || catA == GuiScaleType.Category.ORIGIN || catA == GuiScaleType.Category.PARENT));
        return isFixedVsOther && axesOverlap(a, b);
    }

    private boolean axesOverlap(GuiScaleType a, GuiScaleType b) {
        Set<GuiScaleType.Axes> axesA = a.getAxes();
        Set<GuiScaleType.Axes> axesB = b.getAxes();
        axesA.retainAll(axesB);
        return !axesA.isEmpty();
    }

    public boolean isSizeable()              { return isSizeableHorizontal() || isSizeableVertical(); }
    public boolean isSizeableHorizontal()    { return scaleTypes.contains(GuiScaleType.FLOW) || scaleTypes.contains(GuiScaleType.FLOW_HORIZONTAL); }
    public boolean isSizeableVertical()      { return scaleTypes.contains(GuiScaleType.FLOW) || scaleTypes.contains(GuiScaleType.FLOW_VERTICAL); }
    public boolean isFixed()                 { return isFixedHorizontal() && isFixedVertical(); }
    public boolean isFixedHorizontal()       { return scaleTypes.contains(GuiScaleType.FIXED) || scaleTypes.contains(GuiScaleType.FIXED_HORIZONTAL); }
    public boolean isFixedVertical()         { return scaleTypes.contains(GuiScaleType.FIXED) || scaleTypes.contains(GuiScaleType.FIXED_VERTICAL); }
    public boolean isOriginDependent()       { return isOriginHorizontal() || isOriginVertical(); }
    public boolean isOriginHorizontal()      { return scaleTypes.contains(GuiScaleType.ORIGIN_HORIZONTAL); }
    public boolean isOriginVertical()        { return scaleTypes.contains(GuiScaleType.ORIGIN_VERTICAL); }
    public boolean isParent()                { return isParentHorizontal() && isParentVertical(); }
    public boolean isParentHorizontal()      { return scaleTypes.contains(GuiScaleType.PARENT) || scaleTypes.contains(GuiScaleType.PARENT_HORIZONTAL); }
    public boolean isParentVertical()        { return scaleTypes.contains(GuiScaleType.PARENT) || scaleTypes.contains(GuiScaleType.PARENT_VERTICAL); }

    public Set<GuiScaleType> getScaleTypes() { return Collections.unmodifiableSet(scaleTypes); }
}