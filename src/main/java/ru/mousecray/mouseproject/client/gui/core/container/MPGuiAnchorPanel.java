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

import static ru.mousecray.mouseproject.client.gui.core.component.MPGuiRenderHelper.calculateFlowComponentX;
import static ru.mousecray.mouseproject.client.gui.core.component.MPGuiRenderHelper.calculateFlowComponentY;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiAnchorPanel extends MPGuiPanel<MPGuiAnchorPanel> {
    private final Map<MPGuiElement<?>, MPAnchorPos> childAnchors = new HashMap<>();

    public MPGuiAnchorPanel(MPGuiShape elementShape) { super(elementShape); }

    public void addChild(MPGuiElement<?> child, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        super.addChild(child, margin, offset);
        childAnchors.put(child, anchor != null ? anchor : MPAnchorPos.TOP_LEFT);
    }

    @Override
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MPMutableGuiShape inner) {
        for (MPGuiElement<?> child : children) {
            //1. Вручную считаем отступы, масштабируя их от экрана
            MPGuiMargin margin = getChildMargin(child);
            marginTemp[0] = calculateFlowComponentX(parentDefaultSize, parentContentSize, margin.getLeft());
            marginTemp[1] = calculateFlowComponentY(parentDefaultSize, parentContentSize, margin.getTop());
            marginTemp[2] = calculateFlowComponentX(parentDefaultSize, parentContentSize, margin.getRight());
            marginTemp[3] = calculateFlowComponentY(parentDefaultSize, parentContentSize, margin.getBottom());
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            //2. Доступное пространство - это размер ПАНЕЛИ (inner)
            float childAvailW = Math.max(0, inner.width() - ml - mr);
            float childAvailH = Math.max(0, inner.height() - mt - mb);

            //3. Вычисляем предпочтительный размер элемента (учитывает FLOW, PARENT, ORIGIN, FIXED)
            child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, childAvailH, measureTemp);
            float childW = measureTemp.x();
            float childH = measureTemp.y();

            float childX = inner.x() + ml;
            float childY = inner.y() + mt;

            MPAnchorPos anchor = childAnchors.getOrDefault(child, MPAnchorPos.TOP_LEFT);
            MPGuiVector offset = getChildOffset(child);

            //4. Смещения (offset) масштабируются ВСЕГДА
            float offsetX = calculateFlowComponentX(parentDefaultSize, parentContentSize, offset.x());
            float offsetY = calculateFlowComponentY(parentDefaultSize, parentContentSize, offset.y());

            //5. Позиционирование по якорю
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
    @Override protected void onChildrenCleared() { childAnchors.clear(); }
}