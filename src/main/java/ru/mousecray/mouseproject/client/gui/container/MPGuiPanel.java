package ru.mousecray.mouseproject.client.gui.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.MPGuiScreen;
import ru.mousecray.mouseproject.client.gui.MPGuiTextField;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper.*;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiPanel<T extends MPGuiPanel<T>> implements MPGuiElement<T> {
    protected final List<MPGuiElement<?>> children = new ArrayList<>();

    public List<MPGuiElement<?>> getChildren() { return children; }

    private final Map<MPGuiElement<?>, GuiMargin>      childMargins = new HashMap<>();
    private final Map<MPGuiElement<?>, AnchorPosition> childAnchors = new HashMap<>();
    private final Map<MPGuiElement<?>, GuiVector>      childOffsets = new HashMap<>();

    private GuiPadding    padding    = new GuiPadding(0);
    private LayoutType    layoutType = LayoutType.FREE;
    private GuiScaleRules scaleRules = new GuiScaleRules(GuiScaleType.FLOW);

    private final MutableGuiShape elementShape, calculatedElementShape = new MutableGuiShape();
    private final MutableGuiVector measureTemp        = new MutableGuiVector();
    private final MutableGuiShape  childAvailableTemp = new MutableGuiShape();
    private final float[]          marginTemp         = new float[4];

    private MPGuiScreen   screen;
    private MPGuiPanel<?> parent;

    private MPGuiTexturePack texturePack = MPGuiTexturePack.EMPTY;
    private int              id;

    public MPGuiPanel(GuiShape elementShape) {
        this.elementShape = elementShape.toMutable();
    }

    @Override public void setElementShape(IGuiShape elementShape) { this.elementShape.withShape(elementShape); }

    @SuppressWarnings("unchecked") @Override public T self()      { return (T) this; }

    @Override public void setPadding(GuiPadding padding)          { this.padding = padding; }
    @Override public GuiPadding getPadding()                      { return padding; }
    public void setLayoutType(LayoutType layoutType)              { this.layoutType = layoutType; }
    public LayoutType getLayoutType()                             { return layoutType; }

    public void addChild(MPGuiElement<?> child, @Nullable GuiMargin margin, @Nullable AnchorPosition anchorPosition, @Nullable GuiVector offset) {
        children.add(child);
        childMargins.put(child, margin != null ? margin : new GuiMargin(0));

        childAnchors.put(child, anchorPosition);
        childOffsets.put(child, offset != null ? offset : GuiVector.ZERO);

        child.setParent(self());
        if (screen != null) {
            child.setScreen(screen);
            child.setId(screen.genNextElementID());
        }
    }

    protected GuiMargin getChildMargin(MPGuiElement<?> child) {
        return childMargins.getOrDefault(child, new GuiMargin(0));
    }

    protected AnchorPosition getChildAnchor(MPGuiElement<?> child) {
        return childAnchors.get(child);
    }

    protected GuiVector getChildOffset(MPGuiElement<?> child) {
        return childOffsets.getOrDefault(child, GuiVector.ZERO);
    }

    @Override public void setId(int id) { this.id = id; }

    @Override
    public void setScreen(MPGuiScreen screen) {
        this.screen = screen;
        for (MPGuiElement<?> child : children) child.setScreen(screen);
    }

    @Override public MPGuiScreen getScreen()                                      { return screen; }
    @Override public MPGuiPanel<?> getParent()                                    { return parent; }
    @Override public void setParent(MPGuiPanel<?> parent)                         { this.parent = parent; }
    @Override public GuiScaleRules getScaleRules()                                { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules)                 { this.scaleRules = scaleRules; }
    @Override public MPGuiTexturePack getTexturePack()                            { return texturePack; }
    @Override public void setTexturePack(MPGuiTexturePack texturePack)            { this.texturePack = texturePack; }
    @Override public MutableGuiShape getElementShape()                            { return elementShape; }
    @Override public MutableGuiShape getCalculatedElementShape()                  { return calculatedElementShape; }
    @Override public int getId()                                                  { return id; }
    @Override public String getText()                                             { return ""; }
    @Override public void setText(String text)                                    { }
    @Override public void setGuiString(MPGuiString guiString)                     { }
    @Override public MPGuiString getGuiString()                                   { return MPGuiString.simple(""); }
    @Override public void setTextOffset(IGuiVector offset)                        { }
    @Override public MutableGuiVector getTextOffset()                             { return new MutableGuiVector(); }

    @Override public boolean applyState(@Nullable GuiButtonPersistentState state) { return true; }

    @Override @Nullable public GuiButtonActionState getActionState()              { return null; }
    @Override @Nullable public GuiButtonPersistentState getPersistentState()      { return GuiButtonPersistentState.NORMAL; }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        calculateFlowComponentShapeWithPad(parentDefaultSize, parentContentSize, available, calculatedElementShape, elementShape, scaleRules, padding);

        if (calculatedElementShape.width() <= 0 || calculatedElementShape.height() <= 0) return;

        // Передаем parentContentSize (размер экрана), а не inner.size() (свой размер), чтобы масштаб сохранялся
        if (layoutType == LayoutType.LINEAR_HORIZONTAL)
            layoutLinearHorizontal(parentDefaultSize, parentContentSize, calculatedElementShape);
        else if (layoutType == LayoutType.LINEAR_VERTICAL)
            layoutLinearVertical(parentDefaultSize, parentContentSize, calculatedElementShape);
        else if (layoutType == LayoutType.ANCHOR) layoutAnchor(parentDefaultSize, parentContentSize, calculatedElementShape);
        else layoutFree(parentDefaultSize, parentContentSize, calculatedElementShape);
    }

    private void layoutFree(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childW = measureTemp.x();
            float childH = measureTemp.y();

            float posX = child.getScaleRules().isFixedHorizontal()
                    ? child.getElementShape().x()
                    : calculateFlowComponentX(parentDefaultSize, parentContentSize, child.getElementShape().x());

            float posY = child.getScaleRules().isFixedVertical()
                    ? child.getElementShape().y()
                    : calculateFlowComponentY(parentDefaultSize, parentContentSize, child.getElementShape().y());

            childAvailableTemp.withX(inner.x() + ml + posX);
            childAvailableTemp.withY(inner.y() + mt + posY);
            childAvailableTemp.withWidth(childW);
            childAvailableTemp.withHeight(childH);

            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);
        }
    }

    private void layoutLinearHorizontal(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        float fixedSum  = 0f;
        int   fillCount = 0;

        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mr = marginTemp[2];

            if (child.getScaleRules().isParentHorizontal()) {
                fillCount++;
                fixedSum += ml + mr;
            } else {
                float prefW = measureTemp.x();
                fixedSum += prefW + ml + mr;
            }
        }

        float remaining = inner.width() - fixedSum;
        float fillW     = fillCount > 0 && remaining > 0 ? remaining / fillCount : 0f;

        float curX = inner.x();
        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childAvailH = inner.height() - mt - mb;

            float childW;
            if (child.getScaleRules().isParentHorizontal()) {
                childW = fillW;
            } else {
                child.measurePreferred(parentDefaultSize, parentContentSize, Float.MAX_VALUE, childAvailH, measureTemp);
                childW = measureTemp.x();
            }

            child.measurePreferred(parentDefaultSize, parentContentSize, childW, childAvailH, measureTemp);
            float childH = measureTemp.y();

            childAvailableTemp.withX(curX + ml);
            childAvailableTemp.withY(inner.y() + mt);
            childAvailableTemp.withWidth(childW);
            childAvailableTemp.withHeight(childH);

            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

            curX += ml + childW + mr;
        }
    }

    private void layoutLinearVertical(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        float fixedSum  = 0f;
        int   fillCount = 0;

        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float mt = marginTemp[1], mb = marginTemp[3];

            if (child.getScaleRules().isParentVertical()) {
                fillCount++;
                fixedSum += mt + mb;
            } else {
                float prefH = measureTemp.y();
                fixedSum += prefH + mt + mb;
            }
        }

        float remaining = inner.height() - fixedSum;
        float fillH     = fillCount > 0 && remaining > 0 ? remaining / fillCount : 0f;

        float curY = inner.y();
        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childAvailW = inner.width() - ml - mr;

            float childH;
            if (child.getScaleRules().isParentVertical()) {
                childH = fillH;
            } else {
                child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, Float.MAX_VALUE, measureTemp);
                childH = measureTemp.y();
            }

            child.measurePreferred(parentDefaultSize, parentContentSize, childAvailW, childH, measureTemp);
            float childW = measureTemp.x();

            childAvailableTemp.withX(inner.x() + ml);
            childAvailableTemp.withY(curY + mt);
            childAvailableTemp.withWidth(childW);
            childAvailableTemp.withHeight(childH);

            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);

            curY += mt + childH + mb;
        }
    }

    private void layoutAnchor(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner) {
        for (MPGuiElement<?> child : children) {
            measureChildWithMargin(parentDefaultSize, parentContentSize, child, getChildMargin(child), marginTemp, measureTemp);
            float ml = marginTemp[0], mt = marginTemp[1], mr = marginTemp[2], mb = marginTemp[3];

            float childW = measureTemp.x();
            float childH = measureTemp.y();

            float childX = inner.x() + ml;
            float childY = inner.y() + mt;

            AnchorPosition anchor = childAnchors.get(child);
            if (anchor != null) {
                GuiVector offset  = childOffsets.getOrDefault(child, GuiVector.ZERO);
                float     offsetX = calculateFlowComponentX(parentDefaultSize, parentContentSize, offset.x());
                float     offsetY = calculateFlowComponentY(parentDefaultSize, parentContentSize, offset.y());

                switch (anchor) {
                    case TOP_LEFT:
                        childX += offsetX;
                        childY += offsetY;
                        break;
                    case TOP_CENTER:
                        childX += (inner.width() - childW) / 2 + offsetX;
                        childY += offsetY;
                        break;
                    case TOP_RIGHT:
                        childX += inner.width() - childW - mr + offsetX;
                        childY += offsetY;
                        break;
                    case MIDDLE_LEFT:
                        childX += offsetX;
                        childY += (inner.height() - childH) / 2 + offsetY;
                        break;
                    case MIDDLE_CENTER:
                        childX += (inner.width() - childW) / 2 + offsetX;
                        childY += (inner.height() - childH) / 2 + offsetY;
                        break;
                    case MIDDLE_RIGHT:
                        childX += inner.width() - childW - mr + offsetX;
                        childY += (inner.height() - childH) / 2 + offsetY;
                        break;
                    case BOTTOM_LEFT:
                        childX += offsetX;
                        childY += inner.height() - childH - mb + offsetY;
                        break;
                    case BOTTOM_CENTER:
                        childX += (inner.width() - childW) / 2 + offsetX;
                        childY += inner.height() - childH - mb + offsetY;
                        break;
                    case BOTTOM_RIGHT:
                        childX += inner.width() - childW - mr + offsetX;
                        childY += inner.height() - childH - mb + offsetY;
                        break;
                }
            }

            childAvailableTemp.withX(childX);
            childAvailableTemp.withY(childY);
            childAvailableTemp.withWidth(childW);
            childAvailableTemp.withHeight(childH);

            child.calculate(parentDefaultSize, parentContentSize, childAvailableTemp);
        }
    }

    @Override
    public void measurePreferred(IGuiVector parentDefaultSize, IGuiVector parentContentSize, float suggestedX, float suggestedY, MutableGuiVector result) {
        measurePreferredWithScaleRules(parentDefaultSize, parentContentSize, suggestedX, suggestedY, result, elementShape, scaleRules);
    }

    @Override public void onUpdate0(Minecraft mc, int mouseX, int mouseY)                                            {
                                                                                                                         for (MPGuiElement<?> child : children)
                                                                                                                             child.onUpdate0(mc, mouseX, mouseY);
                                                                                                                     }
    @Override
    public void onMouseEnter0(Minecraft mc, int mouseX, int mouseY)                                                  {
                                                                                                                         MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
                                                                                                                         if (hovered != null)
                                                                                                                             hovered.onMouseEnter0(mc, mouseX, mouseY);
                                                                                                                     }
    @Override
    public void onMouseLeave0(Minecraft mc, int mouseX, int mouseY)                                                  {
                                                                                                                         MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
                                                                                                                         if (hovered != null)
                                                                                                                             hovered.onMouseLeave0(mc, mouseX, mouseY);
                                                                                                                     }
    @Override
    public void onMousePressed0(Minecraft mc, int mouseX, int mouseY)                                                {
                                                                                                                         MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
                                                                                                                         if (hovered != null)
                                                                                                                             hovered.onMousePressed0(mc, mouseX, mouseY);
                                                                                                                     }
    @Override
    public void onMouseReleased0(Minecraft mc, int mouseX, int mouseY)                                               {
                                                                                                                         MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
                                                                                                                         if (hovered != null)
                                                                                                                             hovered.onMouseReleased0(mc, mouseX, mouseY);
                                                                                                                     }
    @Override
    public void onMouseDragged0(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY) {
                                                                                                                         MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
                                                                                                                         if (hovered != null)
                                                                                                                             hovered.onMouseDragged0(mc, mouseX, mouseY, direction, diffX, diffY);
                                                                                                                     }
    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY)                                                  { return calculatedElementShape.contains(mouseX, mouseY); }

    @Override @Nullable
    public MPGuiElement<?> findTopHovered(Minecraft mc, int mouseX, int mouseY) {
        if (!calculatedElementShape.contains(mouseX, mouseY)) return null;
        for (int k = children.size() - 1; k >= 0; k--) {
            MPGuiElement<?> child   = children.get(k);
            MPGuiElement<?> hovered = child.findTopHovered(mc, mouseX, mouseY);
            if (hovered != null) return hovered;
        }
        return this;
    }

    @Override
    public void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks)       {
                                                                                                     drawPanelBackground(mc, mouseX, mouseY, partialTicks);
                                                                                                     for (MPGuiElement<?> child : children)
                                                                                                         child.onDrawBackground(mc, mouseX, mouseY, partialTicks);
                                                                                                 }
    protected void drawPanelBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                                                                                                     MPGuiTexture texture = texturePack.getCalculatedTexture(getActionState(), getPersistentState());
                                                                                                     if (texture != null)
                                                                                                         texture.draw(mc, calculatedElementShape.x(), calculatedElementShape.y(), calculatedElementShape.width(), calculatedElementShape.height());
                                                                                                 }
    @Override
    public void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks)       {
                                                                                                     for (MPGuiElement<?> child : children)
                                                                                                         child.onDrawForeground(mc, mouseX, mouseY, partialTicks);
                                                                                                 }
    @Override
    public void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks)             {
                                                                                                     for (MPGuiElement<?> child : children)
                                                                                                         child.onDrawText(mc, mouseX, mouseY, partialTicks);
                                                                                                 }
    @Override
    public void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks)             {
                                                                                                     for (MPGuiElement<?> child : children)
                                                                                                         child.onDrawLast(mc, mouseX, mouseY, partialTicks);
                                                                                                 }

    public void collectElements() {
        for (MPGuiElement<?> child : children) {
            if (child instanceof MPGuiPanel) ((MPGuiPanel<?>) child).collectElements();
            else if (child instanceof GuiButton) screen.getButtonList().add((GuiButton) child);
            else if (child instanceof GuiLabel) screen.getLabelList().add((GuiLabel) child);
            else if (child instanceof MPGuiTextField<?>) screen.getFieldsList().add((MPGuiTextField<?>) child);
        }
    }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedElementShape.offset(dx, dy);
        for (MPGuiElement<?> child : children) {
            child.offsetCalculatedShape(dx, dy);
        }
    }
}