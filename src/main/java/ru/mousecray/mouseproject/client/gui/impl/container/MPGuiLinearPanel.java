/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.components.GuiRenderHelper.*;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiLinearPanel extends MPGuiPanel<MPGuiLinearPanel> {
    private LinearPanelOrientation linearOrientation;

    public MPGuiLinearPanel(GuiShape elementShape, LinearPanelOrientation linearOrientation) {
        super(elementShape);
        this.linearOrientation = linearOrientation;
    }

    public void setOrientation(LinearPanelOrientation linearOrientation) { this.linearOrientation = linearOrientation; }

    @Override
    protected void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        if (linearOrientation == LinearPanelOrientation.HORIZONTAL) layoutHorizontal(parentDefaultSize, parentContentSize, inner);
        else layoutVertical(parentDefaultSize, parentContentSize, inner);
    }

    private void layoutHorizontal(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        float fixedSum  = 0f;
        int   fillCount = 0;

        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mr = marginTemp[2];

            if (child.getScaleRules().isParentHorizontal()) {
                fillCount++;
                fixedSum += ml + mr;
            } else fixedSum += measureTemp.x() + ml + mr;
        }

        float remaining = inner.width() - fixedSum;
        float fillW     = fillCount > 0 && remaining > 0 ? remaining / fillCount : 0f;

        float curX = inner.x();
        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childAvailH = inner.height() - mt - mb;
            float childW;

            if (child.getScaleRules().isParentHorizontal()) childW = fillW;
            else {
                child.measurePreferred(parentDefaultSize, parentContentSize, Float.MAX_VALUE, childAvailH, measureTemp);
                childW = measureTemp.x();
            }

            child.measurePreferred(parentDefaultSize, parentContentSize, childW, childAvailH, measureTemp);
            float childH = measureTemp.y();

            GuiVector offset  = getChildOffset(child);
            float     offsetX = calculateFlowComponentX(parentDefaultSize, parentContentSize, offset.x());
            float     offsetY = calculateFlowComponentY(parentDefaultSize, parentContentSize, offset.y());

            childAvailableTemp.withX(curX + ml + offsetX).withY(inner.y() + mt + offsetY).withWidth(childW).withHeight(childH);
            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

            curX += ml + childW + mr;
        }
    }

    private void layoutVertical(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        float fixedSum  = 0f;
        int   fillCount = 0;

        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float mt = marginTemp[1], mb = marginTemp[3];

            if (child.getScaleRules().isParentVertical()) {
                fillCount++;
                fixedSum += mt + mb;
            } else fixedSum += measureTemp.y() + mt + mb;
        }

        float remaining = inner.height() - fixedSum;
        float fillH     = fillCount > 0 && remaining > 0 ? remaining / fillCount : 0f;

        float curY = inner.y();
        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childAvailW = inner.width() - ml - mr;
            float childH;

            if (child.getScaleRules().isParentVertical()) childH = fillH;
            else {
                child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, Float.MAX_VALUE, measureTemp);
                childH = measureTemp.y();
            }

            child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, childH, measureTemp);
            float childW = measureTemp.x();

            GuiVector offset  = getChildOffset(child);
            float     offsetX = calculateFlowComponentX(parentDefaultSize, parentContentSize, offset.x());
            float     offsetY = calculateFlowComponentY(parentDefaultSize, parentContentSize, offset.y());

            childAvailableTemp.withX(inner.x() + ml + offsetX).withY(curY + mt + offsetY).withWidth(childW).withHeight(childH);
            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

            curY += mt + childH + mb;
        }
    }
}