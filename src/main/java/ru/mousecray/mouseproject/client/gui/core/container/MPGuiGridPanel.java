/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.core.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.core.dim.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiGridPanel extends MPGuiPanel<MPGuiGridPanel> {
    private static final MPGridPos GRID_POS_ZERO = new MPGridPos(0, 0);

    private final Map<MPGuiElement<?>, MPAnchorPos> childAnchors = new HashMap<>();
    private final Map<MPGuiElement<?>, MPGridPos>   childGridPos = new HashMap<>();

    private int   gridRows;
    private int   gridCols;
    private float gridGapX = 0f;
    private float gridGapY = 0f;

    public MPGuiGridPanel(MPGuiShape elementShape, int rows, int cols) {
        super(elementShape);
        gridRows = Math.max(1, rows);
        gridCols = Math.max(1, cols);
    }

    public MPGuiGridPanel setGridSize(int rows, int cols) {
        gridRows = Math.max(1, rows);
        gridCols = Math.max(1, cols);
        return this;
    }

    public MPGuiGridPanel setGaps(float gapX, float gapY) {
        gridGapX = gapX;
        gridGapY = gapY;
        return this;
    }

    public void addChild(MPGuiElement<?> child, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset, @Nullable MPGridPos gridPos) {
        super.addChild(child, margin, offset);
        childAnchors.put(child, anchor != null ? anchor : MPAnchorPos.TOP_LEFT);
        childGridPos.put(child, gridPos != null ? gridPos : GRID_POS_ZERO);
    }

    @Override
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MPMutableGuiShape inner) {
        if (gridRows <= 0 || gridCols <= 0) return;

        float scaledGapX = calculateFlowComponentX(parentDefaultSize, parentContentSize, gridGapX);
        float scaledGapY = calculateFlowComponentY(parentDefaultSize, parentContentSize, gridGapY);

        float availW = inner.width() - scaledGapX * (gridCols - 1);
        float availH = inner.height() - scaledGapY * (gridRows - 1);

        float cellW = Math.max(0, availW / gridCols);
        float cellH = Math.max(0, availH / gridRows);

        for (MPGuiElement<?> child : children) {
            MPAnchorPos anchor = childAnchors.getOrDefault(child, MPAnchorPos.TOP_LEFT);
            MPGridPos   pos    = childGridPos.getOrDefault(child, GRID_POS_ZERO);

            float cellAreaX = inner.x() + pos.col * (cellW + scaledGapX);
            float cellAreaY = inner.y() + pos.row * (cellH + scaledGapY);
            float cellAreaW = cellW * pos.colSpan + scaledGapX * (pos.colSpan - 1);
            float cellAreaH = cellH * pos.rowSpan + scaledGapY * (pos.rowSpan - 1);

            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childAvailW = Math.max(0, cellAreaW - ml - mr);
            float childAvailH = Math.max(0, cellAreaH - mt - mb);

            child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, childAvailH, measureTemp);
            float childW = measureTemp.x();
            float childH = measureTemp.y();

            float childX = cellAreaX + ml;
            float childY = cellAreaY + mt;

            MPGuiVector offset  = getChildOffset(child);
            float       offsetX = calculateFlowComponentX(parentDefaultSize, parentContentSize, offset.x());
            float       offsetY = calculateFlowComponentY(parentDefaultSize, parentContentSize, offset.y());

            switch (anchor) {
                case TOP_LEFT:
                    childX += offsetX;
                    childY += offsetY;
                    break;
                case TOP_CENTER:
                    childX += (childAvailW - childW) / 2 + offsetX;
                    childY += offsetY;
                    break;
                case TOP_RIGHT:
                    childX += childAvailW - childW + offsetX;
                    childY += offsetY;
                    break;
                case MIDDLE_LEFT:
                    childX += offsetX;
                    childY += (childAvailH - childH) / 2 + offsetY;
                    break;
                case MIDDLE_CENTER:
                    childX += (childAvailW - childW) / 2 + offsetX;
                    childY += (childAvailH - childH) / 2 + offsetY;
                    break;
                case MIDDLE_RIGHT:
                    childX += childAvailW - childW + offsetX;
                    childY += (childAvailH - childH) / 2 + offsetY;
                    break;
                case BOTTOM_LEFT:
                    childX += offsetX;
                    childY += childAvailH - childH + offsetY;
                    break;
                case BOTTOM_CENTER:
                    childX += (childAvailW - childW) / 2 + offsetX;
                    childY += childAvailH - childH + offsetY;
                    break;
                case BOTTOM_RIGHT:
                    childX += childAvailW - childW + offsetX;
                    childY += childAvailH - childH + offsetY;
                    break;
            }

            childAvailableTemp.withX(childX).withY(childY).withWidth(childW).withHeight(childH);
            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);
        }
    }

    @Override
    protected void onChildrenCleared() {
        childAnchors.clear();
        childGridPos.clear();
    }
}