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

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper.calculateFlowComponentX;
import static ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper.calculateFlowComponentY;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiFreePanel extends MPGuiPanel<MPGuiFreePanel> {
    public MPGuiFreePanel(MPGuiShape elementShape) { super(elementShape); }

    @Override
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MPMutableGuiShape inner) {
        for (MPGuiElement<?> child : children) {
            //1. Отступы
            MPGuiMargin margin = getChildMargin(child);
            marginTemp[0] = calculateFlowComponentX(parentDefaultSize, parentContentSize, margin.getLeft());
            marginTemp[1] = calculateFlowComponentY(parentDefaultSize, parentContentSize, margin.getTop());
            marginTemp[2] = calculateFlowComponentX(parentDefaultSize, parentContentSize, margin.getRight());
            marginTemp[3] = calculateFlowComponentY(parentDefaultSize, parentContentSize, margin.getBottom());
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            //2. PARENT заполняет панель
            float childAvailW = Math.max(0, inner.width() - ml - mr);
            float childAvailH = Math.max(0, inner.height() - mt - mb);

            child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, childAvailH, measureTemp);
            float childW = measureTemp.x();
            float childH = measureTemp.y();

            //3. Координаты X/Y подчиняются scaleRules (isFixed)
            float posX = child.getScaleRules().isFixedHorizontal() ? child.getShape().x() : calculateFlowComponentX(parentDefaultSize, parentContentSize, child.getShape().x());
            float posY = child.getScaleRules().isFixedVertical() ? child.getShape().y() : calculateFlowComponentY(parentDefaultSize, parentContentSize, child.getShape().y());

            //4. Оффсеты масштабируются
            MPGuiVector offset  = getChildOffset(child);
            float       offsetX = calculateFlowComponentX(parentDefaultSize, parentContentSize, offset.x());
            float       offsetY = calculateFlowComponentY(parentDefaultSize, parentContentSize, offset.y());

            childAvailableTemp.withX(inner.x() + ml + posX + offsetX);
            childAvailableTemp.withY(inner.y() + mt + posY + offsetY);
            childAvailableTemp.withWidth(childW);
            childAvailableTemp.withHeight(childH);

            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);
        }
    }
}