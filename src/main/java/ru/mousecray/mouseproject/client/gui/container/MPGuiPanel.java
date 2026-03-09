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
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiPanel<T extends MPGuiPanel<T>> implements MPGuiElement<T> {

    protected final List<MPGuiElement<?>> children = new ArrayList<>();

    private final Map<MPGuiElement<?>, GuiMargin> childMargins = new WeakHashMap<>();
    private final Map<MPGuiElement<?>, GuiVector> childOffsets = new WeakHashMap<>();

    private GuiPadding    padding    = new GuiPadding(0);
    private GuiScaleRules scaleRules = new GuiScaleRules(GuiScaleType.FLOW);

    private final MutableGuiShape elementShape;
    private final MutableGuiShape calculatedElementShape = new MutableGuiShape();

    protected final MutableGuiVector measureTemp        = new MutableGuiVector();
    protected final MutableGuiShape  childAvailableTemp = new MutableGuiShape();
    protected final float[]          marginTemp         = new float[4];

    private MPGuiScreen                  screen;
    private WeakReference<MPGuiPanel<?>> parent;
    private MPGuiTexturePack             texturePack = MPGuiTexturePack.EMPTY;
    private int                          id;

    public MPGuiPanel(GuiShape elementShape) {
        this.elementShape = elementShape.toMutable();
    }

    public List<MPGuiElement<?>> getChildren()                    { return children; }

    @Override public void setElementShape(IGuiShape elementShape) { this.elementShape.withShape(elementShape); }
    @SuppressWarnings("unchecked") @Override public T self()      { return (T) this; }
    @Override public void setPadding(GuiPadding padding)          { this.padding = padding; }
    @Override public GuiPadding getPadding()                      { return padding; }
    @Override public void setScaleRules(GuiScaleRules scaleRules) { this.scaleRules = scaleRules; }
    @Override public GuiScaleRules getScaleRules()                { return scaleRules; }

    public void addChild(MPGuiElement<?> child, @Nullable GuiMargin margin, @Nullable GuiVector offset) {
        children.add(child);
        childMargins.put(child, margin != null ? margin : GuiMargin.ZERO);
        childOffsets.put(child, offset != null ? offset : GuiVector.ZERO);

        child.setParent(this);
        if (screen != null) {
            child.setScreen(screen);
            child.setId(screen.genNextElementID());
        }
    }

    public void addChild(MPGuiElement<?> child) {
        addChild(child, GuiMargin.ZERO, GuiVector.ZERO);
    }

    protected GuiMargin getChildMargin(MPGuiElement<?> child) { return childMargins.getOrDefault(child, GuiMargin.ZERO); }
    protected GuiVector getChildOffset(MPGuiElement<?> child) { return childOffsets.getOrDefault(child, GuiVector.ZERO); }

    @Override public void setId(int id)                       { this.id = id; }

    @Override
    public void setScreen(MPGuiScreen screen) {
        this.screen = screen;
        for (MPGuiElement<?> child : children) child.setScreen(screen);
    }

    @Override public MPGuiScreen getScreen()                                      { return screen; }

    @Nullable @Override public MPGuiPanel<?> getParent()                          { return parent.get(); }
    @Override public void setParent(MPGuiPanel<?> parent)                         { this.parent = new WeakReference<>(parent); }

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
        GuiRenderHelper.calculateFlowComponentShape(
                calculatedElementShape,
                parentDefaultSize, parentContentSize, elementShape, scaleRules, available
        );

        if (calculatedElementShape.width() <= 0 || calculatedElementShape.height() <= 0) return;

        //2. Масштабируем отступы
        float padL = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, padding.getLeft());
        float padT = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, padding.getTop());
        float padR = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, padding.getRight());
        float padB = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, padding.getBottom());

        //3. Создаем ВНУТРЕННЮЮ область (уже сжатую паддингом) и передаем её детям
        childAvailableTemp.withShape(calculatedElementShape);
        childAvailableTemp.grow(padL, padT, -padL - padR, -padT - padB);

        layoutChildren(parentDefaultSize, parentContentSize, childAvailableTemp);
    }

    protected abstract void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MutableGuiShape inner);

    @Override
    public void measurePreferred(IGuiVector parentDefaultSize, IGuiVector parentContentSize, float suggestedX, float suggestedY, MutableGuiVector result) {
        GuiRenderHelper.measurePreferredWithScaleRules(parentDefaultSize, parentContentSize, suggestedX, suggestedY, result, elementShape, scaleRules);
    }

    @Override public void onUpdate0(Minecraft mc, int mouseX, int mouseY) {
        for (MPGuiElement<?> child : children)
            child.onUpdate0(mc, mouseX, mouseY);
    }
    @Override
    public void onMouseEnter0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
        if (hovered != null)
            hovered.onMouseEnter0(mc, mouseX, mouseY);
    }
    @Override
    public void onMouseLeave0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
        if (hovered != null)
            hovered.onMouseLeave0(mc, mouseX, mouseY);
    }
    @Override
    public void onMousePressed0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiElement<?> hovered = findTopHovered(mc, mouseX, mouseY);
        if (hovered != null)
            hovered.onMousePressed0(mc, mouseX, mouseY);
    }
    @Override
    public void onMouseReleased0(Minecraft mc, int mouseX, int mouseY) {
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
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) { return calculatedElementShape.contains(mouseX, mouseY); }

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
    public void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
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
    public void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (MPGuiElement<?> child : children)
            child.onDrawForeground(mc, mouseX, mouseY, partialTicks);
    }
    @Override public void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (MPGuiElement<?> child : children)
            child.onDrawText(mc, mouseX, mouseY, partialTicks);
    }
    @Override public void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (MPGuiElement<?> child : children)
            child.onDrawLast(mc, mouseX, mouseY, partialTicks);
    }

    public void collectElements() {
        if (screen == null) return;

        for (MPGuiElement<?> child : children) {
            if (child instanceof MPGuiPanel) ((MPGuiPanel<?>) child).collectElements();
            else if (child instanceof MPGuiScrollPanel) {
                MPGuiPanel<?> content = ((MPGuiScrollPanel<?>) child).getContent();
                if (content != null) content.collectElements();
            } else if (child instanceof GuiButton) screen.getButtonList().add((GuiButton) child);
            else if (child instanceof GuiLabel) screen.getLabelList().add((GuiLabel) child);
            else if (child instanceof MPGuiTextField<?>) screen.getFieldsList().add((MPGuiTextField<?>) child);
        }
    }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedElementShape.offset(dx, dy);
        for (MPGuiElement<?> child : children) child.offsetCalculatedShape(dx, dy);
    }

    public void removeAllChildren() {
        children.clear();
        childMargins.clear();
        childOffsets.clear();
        onChildrenCleared();
    }

    protected void onChildrenCleared() { }
}