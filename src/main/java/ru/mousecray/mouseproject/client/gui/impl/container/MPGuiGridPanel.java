package ru.mousecray.mouseproject.client.gui.impl.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SideOnly(Side.CLIENT)
public class MPGuiGridPanel extends MPGuiPanel<MPGuiGridPanel> {
    private final Map<MPGuiElement<?>, GridPos> childGridPos = new HashMap<>();

    private int   gridRows;
    private int   gridCols;
    private float gridGapX = 0f;
    private float gridGapY = 0f;

    private final MutableGuiShape  childAvailableTemp = new MutableGuiShape();
    private final MutableGuiVector measureTemp        = new MutableGuiVector();
    private final float[]          marginTemp         = new float[4];

    public MPGuiGridPanel(GuiShape elementShape, int rows, int cols) {
        super(elementShape);
        gridRows = Math.max(1, rows);
        gridCols = Math.max(1, cols);
        // Отключаем стандартные LayoutType, так как GridPanel обрабатывает свои элементы иначе
        setLayoutType(LayoutType.FREE);
    }

    @Override public MPGuiGridPanel self() { return this; }

    public MPGuiGridPanel setGaps(float gapX, float gapY) {
        gridGapX = gapX;
        gridGapY = gapY;
        return this;
    }

    public MPGuiGridPanel setGridSize(int rows, int cols) {
        gridRows = Math.max(1, rows);
        gridCols = Math.max(1, cols);
        return this;
    }

    public void addChild(MPGuiElement<?> child, @Nullable GuiMargin margin, @Nullable AnchorPosition anchorPosition, @Nullable GuiVector offset, @Nullable GridPos gridPos) {
        super.addChild(child, margin, anchorPosition, offset);
        if (gridPos != null) childGridPos.put(child, gridPos);
    }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        GuiRenderHelper.calculateFlowComponentShapeWithPad(parentDefaultSize, parentContentSize, available, getCalculatedElementShape(), getElementShape(), getScaleRules(), getPadding());

        MutableGuiShape inner = getCalculatedElementShape();
        if (inner.width() <= 0 || inner.height() <= 0) return;

        layoutGrid(parentDefaultSize, inner);
    }

    private void layoutGrid(IGuiVector parentDefaultSize, MutableGuiShape inner) {
        if (gridRows <= 0 || gridCols <= 0) return;

        float availW = inner.width() - gridGapX * (gridCols - 1);
        float availH = inner.height() - gridGapY * (gridRows - 1);

        float cellW = Math.max(0, availW / gridCols);
        float cellH = Math.max(0, availH / gridRows);

        for (MPGuiElement<?> child : getChildren()) {
            GridPos pos = childGridPos.getOrDefault(child, new GridPos(0, 0));
            int     r   = pos.row;
            int     c   = pos.col;
            int     rs  = pos.rowSpan;
            int     cs  = pos.colSpan;

            float cellAreaX = inner.x() + c * (cellW + gridGapX);
            float cellAreaY = inner.y() + r * (cellH + gridGapY);
            float cellAreaW = cellW * cs + gridGapX * (cs - 1);
            float cellAreaH = cellH * rs + gridGapY * (rs - 1);

            GuiRenderHelper.measureChildWithMargin(parentDefaultSize, inner.size(), child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childAvailW = Math.max(0, cellAreaW - ml - mr);
            float childAvailH = Math.max(0, cellAreaH - mt - mb);

            child.measurePreferred(parentDefaultSize, inner.size(), childAvailW, childAvailH, measureTemp);
            float childW = measureTemp.x();
            float childH = measureTemp.y();

            float childX = cellAreaX + ml;
            float childY = cellAreaY + mt;

            AnchorPosition anchor = getChildAnchor(child);
            if (anchor != null) {
                GuiVector offset  = getChildOffset(child);
                float     offsetX = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, inner.size(), offset.x());
                float     offsetY = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, inner.size(), offset.y());

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
            }

            childAvailableTemp.withX(childX);
            childAvailableTemp.withY(childY);
            childAvailableTemp.withWidth(childW);
            childAvailableTemp.withHeight(childH);

            child.calculate(parentDefaultSize, inner.size(), childAvailableTemp);
        }
    }
}