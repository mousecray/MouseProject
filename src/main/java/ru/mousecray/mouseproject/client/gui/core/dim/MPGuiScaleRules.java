/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.dim;

import java.util.*;

public class MPGuiScaleRules {
    private final Set<MPGuiScaleType> scaleTypes = new HashSet<>();

    public MPGuiScaleRules(MPGuiScaleType... types) { for (MPGuiScaleType type : types) addType(type); }

    private void addType(MPGuiScaleType newType) {
        List<MPGuiScaleType> toRemove = new ArrayList<>();
        for (MPGuiScaleType existing : scaleTypes) if (isIncompatible(existing, newType)) toRemove.add(existing);
        toRemove.forEach(scaleTypes::remove);

        scaleTypes.add(newType);

        if (newType == MPGuiScaleType.ORIGIN_HORIZONTAL) addType(MPGuiScaleType.FLOW_HORIZONTAL);
        else if (newType == MPGuiScaleType.ORIGIN_VERTICAL) addType(MPGuiScaleType.FLOW_VERTICAL);

        if (scaleTypes.contains(MPGuiScaleType.FIXED)) {
            scaleTypes.remove(MPGuiScaleType.FIXED_HORIZONTAL);
            scaleTypes.remove(MPGuiScaleType.FIXED_VERTICAL);
        }
        if (scaleTypes.contains(MPGuiScaleType.FLOW)) {
            scaleTypes.remove(MPGuiScaleType.FLOW_HORIZONTAL);
            scaleTypes.remove(MPGuiScaleType.FLOW_VERTICAL);
        }
        if (scaleTypes.contains(MPGuiScaleType.PARENT)) {
            scaleTypes.remove(MPGuiScaleType.PARENT_HORIZONTAL);
            scaleTypes.remove(MPGuiScaleType.PARENT_VERTICAL);
        }

        if (scaleTypes.contains(MPGuiScaleType.FIXED_HORIZONTAL) && scaleTypes.contains(MPGuiScaleType.FIXED_VERTICAL)) {
            scaleTypes.remove(MPGuiScaleType.FIXED_HORIZONTAL);
            scaleTypes.remove(MPGuiScaleType.FIXED_VERTICAL);
            addType(MPGuiScaleType.FIXED);
        }
        if (scaleTypes.contains(MPGuiScaleType.FLOW_HORIZONTAL) && scaleTypes.contains(MPGuiScaleType.FLOW_VERTICAL)) {
            scaleTypes.remove(MPGuiScaleType.FLOW_HORIZONTAL);
            scaleTypes.remove(MPGuiScaleType.FLOW_VERTICAL);
            addType(MPGuiScaleType.FLOW);
        }
        if (scaleTypes.contains(MPGuiScaleType.PARENT_HORIZONTAL) && scaleTypes.contains(MPGuiScaleType.PARENT_VERTICAL)) {
            scaleTypes.remove(MPGuiScaleType.PARENT_HORIZONTAL);
            scaleTypes.remove(MPGuiScaleType.PARENT_VERTICAL);
            addType(MPGuiScaleType.PARENT);
        }
    }

    private boolean isIncompatible(MPGuiScaleType a, MPGuiScaleType b) {
        MPGuiScaleType.Category catA = a.getCategory();
        MPGuiScaleType.Category catB = b.getCategory();
        if (catA == catB) return catA == MPGuiScaleType.Category.ORIGIN && a != b;

        boolean isFixedVsOther = (catA == MPGuiScaleType.Category.FIXED && (catB == MPGuiScaleType.Category.FLOW || catB == MPGuiScaleType.Category.ORIGIN || catB == MPGuiScaleType.Category.PARENT)) ||
                (catB == MPGuiScaleType.Category.FIXED && (catA == MPGuiScaleType.Category.FLOW || catA == MPGuiScaleType.Category.ORIGIN || catA == MPGuiScaleType.Category.PARENT));
        return isFixedVsOther && axesOverlap(a, b);
    }

    private boolean axesOverlap(MPGuiScaleType a, MPGuiScaleType b) {
        Set<MPGuiScaleType.Axes> axesA = a.getAxes();
        Set<MPGuiScaleType.Axes> axesB = b.getAxes();
        axesA.retainAll(axesB);
        return !axesA.isEmpty();
    }

    public boolean isSizeable()                { return isSizeableHorizontal() || isSizeableVertical(); }
    public boolean isSizeableHorizontal()      { return scaleTypes.contains(MPGuiScaleType.FLOW) || scaleTypes.contains(MPGuiScaleType.FLOW_HORIZONTAL); }
    public boolean isSizeableVertical()        { return scaleTypes.contains(MPGuiScaleType.FLOW) || scaleTypes.contains(MPGuiScaleType.FLOW_VERTICAL); }
    public boolean isFixed()                   { return isFixedHorizontal() && isFixedVertical(); }
    public boolean isFixedHorizontal()         { return scaleTypes.contains(MPGuiScaleType.FIXED) || scaleTypes.contains(MPGuiScaleType.FIXED_HORIZONTAL); }
    public boolean isFixedVertical()           { return scaleTypes.contains(MPGuiScaleType.FIXED) || scaleTypes.contains(MPGuiScaleType.FIXED_VERTICAL); }
    public boolean isOriginDependent()         { return isOriginHorizontal() || isOriginVertical(); }
    public boolean isOriginHorizontal()        { return scaleTypes.contains(MPGuiScaleType.ORIGIN_HORIZONTAL); }
    public boolean isOriginVertical()          { return scaleTypes.contains(MPGuiScaleType.ORIGIN_VERTICAL); }
    public boolean isParent()                  { return isParentHorizontal() && isParentVertical(); }
    public boolean isParentHorizontal()        { return scaleTypes.contains(MPGuiScaleType.PARENT) || scaleTypes.contains(MPGuiScaleType.PARENT_HORIZONTAL); }
    public boolean isParentVertical()          { return scaleTypes.contains(MPGuiScaleType.PARENT) || scaleTypes.contains(MPGuiScaleType.PARENT_VERTICAL); }

    public Set<MPGuiScaleType> getScaleTypes() { return Collections.unmodifiableSet(scaleTypes); }
}