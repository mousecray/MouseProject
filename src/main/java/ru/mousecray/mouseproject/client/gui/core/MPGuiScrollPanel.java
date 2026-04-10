/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.core.components.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.core.components.sound.MPSoundSourceType;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.*;
import ru.mousecray.mouseproject.client.gui.core.misc.MPClickType;
import ru.mousecray.mouseproject.client.gui.core.misc.MPMoveDirection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiScrollPanel<T extends MPGuiScrollPanel<T>> implements MPGuiElement<T> {
    private final MPGuiTickEvent<T>
            updateEvent   = new MPGuiTickEvent<>(),
            drawBGEvent   = new MPGuiTickEvent<>(),
            drawFGEvent   = new MPGuiTickEvent<>(),
            drawLastEvent = new MPGuiTickEvent<>(),
            drawTextEvent = new MPGuiTickEvent<>();
    private final MPGuiMouseClickEvent<T>
            pressEvent   = new MPGuiMouseClickEvent<>(MPClickType.PRESS),
            releaseEvent = new MPGuiMouseClickEvent<>(MPClickType.RELEASE),
            clickEvent   = new MPGuiMouseClickEvent<>(MPClickType.CLICK);
    private final MPGuiMouseMoveEvent<T>   moveEvent   = new MPGuiMouseMoveEvent<>();
    private final MPGuiMouseDragEvent<T>   dragEvent   = new MPGuiMouseDragEvent<>();
    private final MPGuiMouseScrollEvent<T> scrollEvent = new MPGuiMouseScrollEvent<>();
    private final MPGuiKeyEvent<T>         keyEvent    = new MPGuiKeyEvent<>();
    private final MPGuiSoundEvent<T>       soundEvent  = new MPGuiSoundEvent<>();

    private final MPMutableGuiShape shape;
    private final MPMutableGuiShape calculatedShape = new MPMutableGuiShape();
    private       GuiScaleRules     scaleRules      = new GuiScaleRules(MPGuiScaleType.FLOW);

    private MPGuiPanel<?> content;

    private float   scrollY       = 0;
    private float   contentHeight = 0;
    private boolean scrollEnabled = true;

    protected MPGuiTexturePack texturePack = MPGuiTexturePack.EMPTY();
    protected MPGuiColorPack   colorPack   = MPGuiColorPack.CONTROL_SIMPLE();
    protected MPGuiSoundPack   soundPack   = MPGuiSoundPack.EMPTY();

    protected final MPGuiElementStateManager stateManager = new MPGuiElementStateManager();

    private MPGuiScreen   screen;
    private MPGuiPanel<?> parent;
    private int           id;

    public MPGuiScrollPanel(MPGuiShape shape) {
        this.shape = shape.toMutable();
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

    @Nullable public MPGuiPanel<?> getContent()              { return content; }

    @SuppressWarnings("unchecked") @Override public T self() { return (T) this; }

    //Идентификация и иерархия
    @Override public void setId(int id) { this.id = id; }
    @Override public int getId()                       { return id; }

    @Override @Nullable public MPGuiScreen getScreen() { return screen; }

    @Override
    public void setScreen(@Nullable MPGuiScreen screen) {
        this.screen = screen;
        if (content != null) content.setScreen(screen);
    }

    @Override @Nullable public MPGuiPanel<?> getParent() { return parent; }

    @Override
    public void setParent(@Nullable MPGuiPanel<?> parent) {
        this.parent = parent;
        if (content != null) content.setParent(parent);
    }

    //Данные и состояние
    @Override public MPGuiString getGuiString() { return MPGuiString.EMPTY(); }
    @Override public void setGuiString(MPGuiString guiString)   { }

    @Override public MPGuiElementStateManager getStateManager() { return stateManager; }

    @Override public MPGuiTexturePack getTexturePack()          { return texturePack; }

    @Override public void setShape(IGuiShape shape)             { this.shape.withShape(shape); }
    @Override public MPMutableGuiShape getShape()               { return shape; }
    @Override public MPMutableGuiShape getCalculatedShape()     { return calculatedShape; }

    @Override
    public void calculate(IGuiVector pDefSize, IGuiVector pContentSize, IGuiShape available) {
        MPGuiRenderHelper.calculateFlowComponentShape(
                calculatedShape, pDefSize, pContentSize,
                shape, scaleRules, available
        );

        if (content != null) {
            MPMutableGuiShape contentAvail = calculatedShape.copy().toMutable();
            contentAvail.withHeight(99999f);
            content.calculate(pDefSize, pContentSize, contentAvail);

            contentHeight = calculateTrueContentHeight();

            float maxScroll = Math.max(0, contentHeight - calculatedShape.height());
            if (scrollY > maxScroll) scrollY = maxScroll;
            if (scrollY < 0) scrollY = 0;

            content.offsetCalculatedShape(0, -scrollY);
        }
    }

    private float calculateTrueContentHeight() {
        if (content == null) return 0;
        return Math.max(0, findMaxBottom(content) - calculatedShape.y());
    }

    private float findMaxBottom(MPGuiElement<?> element) {
        float max = element.getCalculatedShape().y() + element.getCalculatedShape().height();
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

        float maxScroll = Math.max(0, contentHeight - calculatedShape.height());
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;

        float diff = scrollY - oldScroll;
        if (diff != 0) content.offsetCalculatedShape(0, -diff);
    }

    @Override
    public void dispatchUpdate(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content != null) content.dispatchUpdate(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public void dispatchProcessHover(Minecraft mc, int mouseX, int mouseY) {
        if (content != null) content.dispatchProcessHover(mc, mouseX, mouseY);
    }

    @Override
    public void dispatchMouseEnter(Minecraft mc, int mouseX, int mouseY) {
        if (content != null) content.dispatchMouseEnter(mc, mouseX, mouseY);
    }

    @Override
    public void dispatchMouseLeave(Minecraft mc, int mouseX, int mouseY) {
        if (content != null) content.dispatchMouseLeave(mc, mouseX, mouseY);
    }

    @Override
    public boolean dispatchMousePressed(Minecraft mc, int mouseX, int mouseY, int mouseButton) {
        if (!calculatedShape.contains(mouseX, mouseY)) return false;
        if (content != null) return content.dispatchMousePressed(mc, mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public void dispatchMouseReleased(Minecraft mc, int mouseX, int mouseY, int state) {
        if (!calculatedShape.contains(mouseX, mouseY)) return;
        if (content != null) content.dispatchMouseReleased(mc, mouseX, mouseY, state);
    }

    @Override
    public boolean dispatchMouseDragged(Minecraft mc, int mouseX, int mouseY, MPMoveDirection direction, int diffX, int diffY) {
        if (!calculatedShape.contains(mouseX, mouseY)) return false;
        if (content != null) return content.dispatchMouseDragged(mc, mouseX, mouseY, direction, diffX, diffY);
        return false;
    }

    @Override
    public boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll) {
        if (!calculatedShape.contains(mouseX, mouseY)) return false;

        if (content != null && content.dispatchMouseScrolled(mc, mouseX, mouseY, scroll)) return true;

        if (scrollEnabled) {
            float oldScroll = scrollY;
            applyScroll(-scroll / 10f);
            return Float.compare(oldScroll, scrollY) != 0;
        }

        return false;
    }

    @Override
    public boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode) {
        if (content != null) return content.dispatchKeyTyped(mc, mouseX, mouseY, typedChar, keyCode);
        return false;
    }

    @Override
    public void dispatchPlaySound(Minecraft mc, SoundHandler soundHandler, MPSoundSourceType source) {
        if (content != null) content.dispatchPlaySound(mc, soundHandler, source);
    }

    @Override
    public void dispatchDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.dispatchDrawBackground(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (contentHeight > calculatedShape.height()) drawScrollBar(mc);
    }

    @Override
    public void dispatchDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.dispatchDrawForeground(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void dispatchDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.dispatchDrawText(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void dispatchDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (content == null) return;
        setupScissor(mc);
        content.dispatchDrawLast(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void setupScissor(Minecraft mc) {
        int scale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                (int) (calculatedShape.x() * scale),
                (int) (mc.displayHeight - (calculatedShape.y() + calculatedShape.height()) * scale),
                (int) (calculatedShape.width() * scale),
                (int) (calculatedShape.height() * scale)
        );
    }

    protected void drawScrollBar(Minecraft mc) {
        // Отрисовка скроллбара
    }


    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) { return calculatedShape.contains(mouseX, mouseY); }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedShape.offset(dx, dy);
        if (content != null) content.offsetCalculatedShape(dx, dy);
    }

    @Override
    public void measurePreferred(IGuiVector pDefSize, IGuiVector pContentSize, float sugX, float sugY, MPMutableGuiVector result) {
        MPGuiRenderHelper.measurePreferredWithScaleRules(pDefSize, pContentSize, sugX, sugY, result, shape, scaleRules);
        MPGuiRenderHelper.addPaddingToPreferred(pDefSize, pContentSize, result, getPadding(), scaleRules);
    }

    @Override public GuiScaleRules getScaleRules()                { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules) { this.scaleRules = scaleRules; }
    @Override public void setPadding(MPGuiPadding padding)        { }
    @Override public MPGuiPadding getPadding()                    { return new MPGuiPadding(0); }
    @Override public void setTexturePack(MPGuiTexturePack pack)   { }

    @Override public String getText()                             { return ""; }
    @Override public void setText(String text)                    { }
    @Override public void setTextOffset(IGuiVector offset)        { }
    @Override public MPMutableGuiVector getTextOffset()           { return new MPMutableGuiVector(); }
}