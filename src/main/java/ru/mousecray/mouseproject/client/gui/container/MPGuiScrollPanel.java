package ru.mousecray.mouseproject.client.gui.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.MPGuiScreen;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

import static ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper.measurePreferredWithScaleRules;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiScrollPanel<T extends MPGuiScrollPanel<T>> implements MPGuiElement<T> {

    private final MutableGuiShape elementShape;
    private final MutableGuiShape calculatedElementShape = new MutableGuiShape();
    private       GuiScaleRules   scaleRules             = new GuiScaleRules(GuiScaleType.FLOW);

    private MPGuiPanel<?> content;

    private float   scrollY       = 0;
    private float   contentHeight = 0;
    private boolean scrollEnabled = true;

    private MPGuiScreen   screen;
    private MPGuiPanel<?> parent;
    private int           id;

    public MPGuiScrollPanel(GuiShape elementShape) {
        this.elementShape = elementShape.toMutable();
    }

    public void setContent(@Nullable MPGuiPanel<?> content) {
        this.content = content;
        if (content != null) {
            if (parent != null) content.setParent(parent);
            if (screen != null) {
                content.setScreen(screen);
                content.setId(screen.genNextElementID());
            }
        }
    }

    @Nullable public MPGuiPanel<?> getContent()                  { return content; }

    @SuppressWarnings("unchecked") @Override public T self()     { return (T) this; }
    @Override public void setElementShape(IGuiShape shape)       { elementShape.withShape(shape); }
    @Override public MutableGuiShape getElementShape()           { return elementShape; }
    @Override public MutableGuiShape getCalculatedElementShape() { return calculatedElementShape; }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        GuiRenderHelper.calculateFlowComponentShape(
                calculatedElementShape, parentDefaultSize, parentContentSize,
                elementShape, scaleRules, available
        );

        if (content != null) {
            MutableGuiShape contentAvail = calculatedElementShape.toMutable();
            contentAvail.withHeight(Float.MAX_VALUE);
            content.calculate(parentDefaultSize, parentContentSize, contentAvail);

            contentHeight = calculateTrueContentHeight();

            float maxScroll = Math.max(0, contentHeight - calculatedElementShape.height());
            if (scrollY > maxScroll) scrollY = maxScroll;
            if (scrollY < 0) scrollY = 0;

            content.offsetCalculatedShape(0, -scrollY);
        }
    }

    private float calculateTrueContentHeight() {
        if (content == null) return 0;
        return Math.max(0, findMaxBottom(content) - calculatedElementShape.y());
    }

    private float findMaxBottom(MPGuiElement<?> element) {
        float max = element.getCalculatedElementShape().y() + element.getCalculatedElementShape().height();
        if (element instanceof MPGuiPanel) {
            for (MPGuiElement<?> child : ((MPGuiPanel<?>) element).getChildren()) {
                max = Math.max(max, findMaxBottom(child));
            }
        }
        return max;
    }

    public void applyScroll(float amount) {
        if (content == null) return;

        float oldScroll = scrollY;
        scrollY += amount;

        float maxScroll = Math.max(0, contentHeight - calculatedElementShape.height());
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;

        float diff = scrollY - oldScroll;
        if (diff != 0) content.offsetCalculatedShape(0, -diff);
    }

    @Override
    public void onUpdate0(Minecraft mc, int mouseX, int mouseY) {
        if (content != null) content.onUpdate0(mc, mouseX, mouseY);
    }

    protected void delegateToContent(Minecraft mc, int mouseX, int mouseY, Consumer<MPGuiElement<?>> action) {
        if (mouseHover(mc, mouseX, mouseY) && content != null && content.mouseHover(mc, mouseX, mouseY)) {
            action.accept(content);
        }
    }

    @Override
    public void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;

        setupScissor(mc);
        content.onDrawBackground(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (contentHeight > calculatedElementShape.height()) drawScrollBar(mc);
    }

    @Override
    public void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.onDrawForeground(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.onDrawText(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.onDrawLast(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void setupScissor(Minecraft mc) {
        int scale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                (int) (calculatedElementShape.x() * scale),
                (int) (mc.displayHeight - (calculatedElementShape.y() + calculatedElementShape.height()) * scale),
                (int) (calculatedElementShape.width() * scale),
                (int) (calculatedElementShape.height() * scale)
        );
    }

    protected void drawScrollBar(Minecraft mc) {
        // Отрисовка скроллбара
    }

    @Override @Nullable
    public MPGuiElement<?> findTopHovered(Minecraft mc, int mouseX, int mouseY) {
        if (!calculatedElementShape.contains(mouseX, mouseY)) return null;
        if (content != null) {
            MPGuiElement<?> hovered = content.findTopHovered(mc, mouseX, mouseY);
            if (hovered != null) return hovered;
        }
        return this;
    }

    @Override public boolean onMouseEnter0(Minecraft mc, int mouseX, int mouseY) { return true; }
    @Override public boolean onMouseLeave0(Minecraft mc, int mouseX, int mouseY) { return true; }

    @Override
    public boolean onMouseScrolled0(Minecraft mc, int mouseX, int mouseY, int scroll) {
        if (!calculatedElementShape.contains(mouseX, mouseY)) return false;

        if (content != null && content.mouseHover(mc, mouseX, mouseY)) {
            if (content.onMouseScrolled0(mc, mouseX, mouseY, scroll)) return true;
        }

        if (scrollEnabled) {
            float oldScroll = scrollY;
            applyScroll(-scroll / 10f);

            return Float.compare(oldScroll, scrollY) != 0;
        }

        return false;
    }

    @Override
    public boolean onMousePressed0(Minecraft mc, int mouseX, int mouseY) {
        if (!calculatedElementShape.contains(mouseX, mouseY)) return false;
        if (content != null && content.mouseHover(mc, mouseX, mouseY)) {
            return content.onMousePressed0(mc, mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean onMouseReleased0(Minecraft mc, int mouseX, int mouseY) {
        if (!calculatedElementShape.contains(mouseX, mouseY)) return false;
        if (content != null && content.mouseHover(mc, mouseX, mouseY)) {
            return content.onMouseReleased0(mc, mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean onMouseDragged0(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY) {
        if (!calculatedElementShape.contains(mouseX, mouseY)) return false;
        if (content != null && content.mouseHover(mc, mouseX, mouseY)) {
            return content.onMouseDragged0(mc, mouseX, mouseY, direction, diffX, diffY);
        }
        return false;
    }

    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) { return calculatedElementShape.contains(mouseX, mouseY); }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedElementShape.offset(dx, dy);
        if (content != null) content.offsetCalculatedShape(dx, dy);
    }

    @Override
    public void measurePreferred(IGuiVector parentDefaultSize, IGuiVector parentContentSize, float suggestedX, float suggestedY, MutableGuiVector result) {
        measurePreferredWithScaleRules(parentDefaultSize, parentContentSize, suggestedX, suggestedY, result, elementShape, scaleRules);
    }

    @Override public void setScreen(MPGuiScreen screen) {
        this.screen = screen;
        if (content != null) content.setScreen(screen);
    }
    @Override public MPGuiScreen getScreen() { return screen; }

    @Override
    public void setParent(MPGuiPanel<?> parent) {
        this.parent = parent;
        if (content != null) content.setParent(parent);
    }
    @Override public MPGuiPanel<?> getParent()                                    { return parent; }

    @Override public void setId(int id)                                           { this.id = id; }
    @Override public int getId()                                                  { return id; }

    @Override public GuiScaleRules getScaleRules()                                { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules)                 { this.scaleRules = scaleRules; }
    @Override public void setPadding(GuiPadding padding)                          { }
    @Override public GuiPadding getPadding()                                      { return new GuiPadding(0); }
    @Override public void setTexturePack(MPGuiTexturePack pack)                   { }
    @Override public MPGuiTexturePack getTexturePack()                            { return MPGuiTexturePack.EMPTY; }

    @Override public String getText()                                             { return ""; }
    @Override public void setText(String text)                                    { }
    @Override public MPGuiString getGuiString()                                   { return MPGuiString.simple(""); }
    @Override public void setGuiString(MPGuiString guiString)                     { }
    @Override public void setTextOffset(IGuiVector offset)                        { }
    @Override public MutableGuiVector getTextOffset()                             { return new MutableGuiVector(); }

    @Override public boolean applyState(@Nullable GuiButtonPersistentState state) { return true; }
    @Override @Nullable public GuiButtonActionState getActionState()              { return null; }
    @Override @Nullable public GuiButtonPersistentState getPersistentState()      { return GuiButtonPersistentState.NORMAL; }
}