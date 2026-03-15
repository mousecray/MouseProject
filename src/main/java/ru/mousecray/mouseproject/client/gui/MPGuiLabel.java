/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.*;
import ru.mousecray.mouseproject.client.gui.misc.*;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

@SideOnly(Side.CLIENT)
public abstract class MPGuiLabel<T extends MPGuiLabel<T>> extends GuiLabel implements MPGuiElement<T> {
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
    private final MPGuiMouseMoveEvent<T> moveEvent  = new MPGuiMouseMoveEvent<>();
    private final MPGuiMouseDragEvent<T> dragEvent  = new MPGuiMouseDragEvent<>();
    private final MPGuiSoundEvent<T>     soundEvent = new MPGuiSoundEvent<>();

    private final       FontRenderer             fontRenderer;
    protected final     MPFontSize               fontSize;
    protected           float                    textScaleMultiplayer = 1.0F;
    private             boolean                  centered;
    @Nullable protected GuiButtonActionState     actionState          = null;
    @Nullable protected GuiButtonPersistentState persistentState      = GuiButtonPersistentState.NORMAL;
    protected           StateColorContainer      colorContainer       = StateColorContainer.createDefault();
    private             int                      tickDown             = -1;
    private             int                      partialTick;
    private             boolean                  hovered;

    protected final MutableGuiShape elementShape = new MutableGuiShape(), calculatedElementShape = new MutableGuiShape();
    protected final MutableGuiShape calculatedInnerShape = new MutableGuiShape();

    protected MPGuiString guiString = MPGuiString.simple("");

    private GuiScaleRules scaleRules = new GuiScaleRules(GuiScaleType.FLOW);

    private MPGuiPanel<?> parent;
    private GuiPadding    padding = new GuiPadding(0);

    private MPGuiScreen screen;

    @Nullable private final SoundEvent soundClick;

    public MPGuiLabel(String text, FontRenderer fontRenderer, GuiShape elementShape, int color, MPFontSize fontSize, @Nullable SoundEvent soundClick) {
        super(
                fontRenderer, 0,
                (int) elementShape.x(), (int) elementShape.y(),
                (int) elementShape.width(), (int) elementShape.height(),
                color
        );
        this.fontRenderer = fontRenderer;
        this.elementShape.withShape(elementShape);
        this.soundClick = soundClick;
        this.fontSize = fontSize;
        guiString = MPGuiString.simple(text);

        addLine(text);
    }

    @Override public MutableGuiShape getElementShape()           { return elementShape; }
    @Override public MutableGuiShape getCalculatedElementShape() { return calculatedElementShape; }
    @Override public String getText()                            { return guiString.get(); }


    @Override
    public void setText(String rawText) {
        guiString = MPGuiString.simple(rawText);
        labels.clear();
        String[] split = rawText.split("\n");
        labels.addAll(Arrays.asList(split));
    }

    @Override
    public void setGuiString(MPGuiString guiString) {
        this.guiString = guiString;
        labels.clear();
        String[] split = guiString.get().split("\n");
        labels.addAll(Arrays.asList(split));
    }

    @Override public MPGuiString getGuiString()                              { return guiString; }

    @Override public void setId(int id)                                      { this.id = id; }
    @Override public int getId()                                             { return id; }

    @Override @SuppressWarnings("unchecked") public T self()                 { return (T) this; }

    @Override @Nullable public GuiButtonActionState getActionState()         { return actionState; }
    @Override @Nullable public GuiButtonPersistentState getPersistentState() { return persistentState; }
    @Override public MPGuiTexturePack getTexturePack()                       { return MPGuiTexturePack.EMPTY; }

    @Override public void setTexturePack(MPGuiTexturePack texturePack)       { }
    @Override public void setElementShape(IGuiShape elementShape)            { this.elementShape.withShape(elementShape); }
    @Override public GuiScaleRules getScaleRules()                           { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules)            { this.scaleRules = scaleRules; }
    @Override public void setPadding(GuiPadding padding)                     { this.padding = padding; }
    @Override public GuiPadding getPadding()                                 { return padding; }
    @Override public void setScreen(MPGuiScreen screen)                      { this.screen = screen; }
    @Override public MPGuiScreen getScreen()                                 { return screen; }
    @Override public void setParent(MPGuiPanel<?> parent)                    { this.parent = parent; }
    @Override public MPGuiPanel<?> getParent()                               { return parent; }
    @Override public void setTextOffset(IGuiVector offset)                   { }
    @Override public MutableGuiVector getTextOffset()                        { return new MutableGuiVector(); }


    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        GuiRenderHelper.calculateFlowComponentShape(calculatedElementShape, parentDefaultSize, parentContentSize, elementShape, scaleRules, available);

        GuiPadding pad  = getPadding();
        float      padL = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getLeft());
        float      padT = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getTop());
        float      padR = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getRight());
        float      padB = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getBottom());

        calculatedInnerShape.withShape(calculatedElementShape);
        calculatedInnerShape.grow(-padL, -padT, -padR, -padB);

        x = (int) calculatedElementShape.x();
        y = (int) calculatedElementShape.y();
        width = (int) calculatedElementShape.width();
        height = (int) calculatedElementShape.height();
    }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedElementShape.offset(dx, dy);
        calculatedInnerShape.offset(dx, dy);
        x = (int) calculatedElementShape.x();
        y = (int) calculatedElementShape.y();
    }

    @Override
    public void measurePreferred(IGuiVector parentDefaultSize, IGuiVector parentContentSize, float suggestedX, float suggestedY, MutableGuiVector result) {
        GuiRenderHelper.measurePreferredWithScaleRules(parentDefaultSize, parentContentSize, suggestedX, suggestedY, result, elementShape, scaleRules);
    }


    @Override
    public boolean applyState(@Nullable GuiButtonPersistentState state) {
        processVanillaPersistentState(state);
        persistentState = state;
        return true;
    }

    protected boolean applyActionState(@Nullable GuiButtonActionState state) {
        processVanillaActionState(state);
        actionState = state;
        return true;
    }

    private void processVanillaActionState(@Nullable GuiButtonActionState state) { }

    private void processVanillaPersistentState(@Nullable GuiButtonPersistentState state) {
        visible = state != null;
    }

    @Override
    public final void onUpdate0(Minecraft mc, int mouseX, int mouseY) {
        if (++partialTick >= 20) partialTick = 0;
        if (tickDown >= 0) ++tickDown;

        MPGuiEventFactory.pushTickEvent(updateEvent, self(), mc, mouseX, mouseY, partialTick);
        onAnyEventFire(updateEvent);
        if (!updateEvent.isCancelled()) onUpdate(updateEvent);
        int           diffX     = mouseX - moveEvent.getMouseX();
        int           diffY     = mouseY - moveEvent.getMouseY();
        MoveDirection direction = MoveDirection.getMoveDirection(diffX, diffY);
        MPGuiEventFactory.pushMouseMoveEvent(moveEvent, self(), mc, mouseX, mouseY, direction);
        if (tickDown >= 0 && direction != null) {
            onMouseDragged0(mc, mouseX, mouseY, direction, diffX, diffY);
        }
    }

    @Override
    public final boolean onMouseEnter0(Minecraft mc, int mouseX, int mouseY) {
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            if (persistentState == null
                    || persistentState == GuiButtonPersistentState.DISABLED
                    || actionState == GuiButtonActionState.PRESSED) return false;
            hovered = true;
            applyActionState(GuiButtonActionState.HOVER);
            onMouseEnter(moveEvent);
            return true;
        }
        return false;
    }

    @Override
    public final boolean onMouseLeave0(Minecraft mc, int mouseX, int mouseY) {
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            if (actionState == GuiButtonActionState.HOVER) applyActionState(null);
            hovered = false;
            onMouseLeave(moveEvent);
            return true;
        }
        return false;
    }

    @Override
    public final boolean onMouseReleased0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiEventFactory.pushMouseClickEvent(releaseEvent, self(), mc, mouseX, mouseY);
        onAnyEventFire(releaseEvent);
        if (!releaseEvent.isCancelled()) {
            if (isMouseOver()) applyActionState(GuiButtonActionState.HOVER);
            else applyActionState(null);
            tickDown = -1;
            if (persistentState != null) {
                onMouseReleased(releaseEvent);
                if (isMouseOver()) {
                    MPGuiEventFactory.pushMouseClickEvent(clickEvent, self(), mc, mouseX, mouseY);
                    onAnyEventFire(clickEvent);
                    if (!clickEvent.isCancelled()) onClick(clickEvent);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseDragged0(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY) {
        MPGuiEventFactory.pushMouseDragEvent(dragEvent, self(), mc, mouseX, mouseY, direction, diffX, diffY, tickDown);
        onAnyEventFire(dragEvent);
        if (!dragEvent.isCancelled()) {
            onMouseDragged(dragEvent);
            return true;
        }
        return false;
    }

    @Override
    public final boolean onMousePressed0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiEventFactory.pushMouseClickEvent(pressEvent, self(), mc, mouseX, mouseY);
        onAnyEventFire(pressEvent);
        if (!pressEvent.isCancelled()) {
            if (persistentState == null || persistentState == GuiButtonPersistentState.DISABLED) return false;
            applyActionState(GuiButtonActionState.PRESSED);
            tickDown = 0;
            onMousePressed(pressEvent);
            onPlaySound0(mc, mc.getSoundHandler(), soundClick, SoundSourceType.PRESS);
            return true;
        }
        return false;
    }

    protected final void onPlaySound0(Minecraft mc, SoundHandler soundHandler, @Nullable SoundEvent sound, SoundSourceType source) {
        if (sound != null) {
            MPGuiEventFactory.pushSoundEvent(soundEvent, self(), mc, moveEvent.getMouseX(), moveEvent.getMouseY(), soundHandler, sound, source);
            onAnyEventFire(soundEvent);
            if (!soundEvent.isCancelled()) onPlaySound(soundEvent);
        }
    }

    protected void onPlaySound(MPGuiSoundEvent<T> event) {
        event.getHandler().playSound(PositionedSoundRecord.getMasterRecord(event.getSound(), 1.0F));
    }

    public void mouseReleased(int mouseX, int mouseY) { onMouseReleased0(Minecraft.getMinecraft(), mouseX, mouseY); }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (tickDown >= 0) {
            int           diffX     = mouseX - moveEvent.getMouseX();
            int           diffY     = mouseY - moveEvent.getMouseY();
            MoveDirection direction = MoveDirection.getMoveDirection(diffX, diffY);
            if (direction != null) {
                MPGuiEventFactory.pushMouseDragEvent(dragEvent, self(), mc, mouseX, mouseY, direction, diffX, diffY, tickDown);
                onAnyEventFire(dragEvent);
                if (!dragEvent.isCancelled()) onMouseDragged(dragEvent);
            }
        }
    }

    protected void onAnyEventFire(MPGuiEvent<T> event) { }

    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY);
    }


    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY);
    }

    @Override @Nullable
    public MPGuiElement<?> findTopHovered(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY) ? this : null;
    }

    public abstract void onClick(MPGuiMouseClickEvent<T> event);

    protected void onDrag(MPGuiMouseDragEvent<T> event) { }

    protected void onUpdate(MPGuiTickEvent<T> event)    { }
    protected void onMouseDragged(MPGuiMouseDragEvent<T> event) {
        if (!event.isCancelled()) onDrag(event);
    }
    protected void onMouseReleased(MPGuiMouseClickEvent<T> event) { }
    protected void onMouseEnter(MPGuiMouseMoveEvent<T> event)     { }
    protected void onMouseLeave(MPGuiMouseMoveEvent<T> event)     { }
    protected void onMousePressed(MPGuiMouseClickEvent<T> event)  { }

    protected final int getHoverState(boolean mouseOver) {
        return persistentState == GuiButtonPersistentState.DISABLED ? 0 : mouseOver ? 2 : 1;
    }

    public final boolean isMouseOver() { return hovered; }

    public final void playPressSound(SoundHandler soundHandler) {
        onPlaySound0(Minecraft.getMinecraft(), Minecraft.getMinecraft().getSoundHandler(), soundClick, SoundSourceType.PRESS);
    }

    @Override
    public final void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawBGEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawBGEvent);
        if (persistentState != null && !drawBGEvent.isCancelled()) drawLabelBackgroundLayer(drawBGEvent);
    }

    @Override
    public final void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawFGEvent);
        if (persistentState != null && !drawFGEvent.isCancelled()) drawLabelForegroundLayer(drawFGEvent);
    }

    @Override
    public final void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawTextEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawTextEvent);
        if (persistentState != null && !drawTextEvent.isCancelled()) drawLabelTextLayer(drawTextEvent);
    }

    @Override
    public final void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawLastEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawLastEvent);
        if (persistentState != null && !drawLastEvent.isCancelled()) drawLabelLastLayer(drawLastEvent);
    }

    protected final void onDrawLabel(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawBackground(mc, mouseX, mouseY, partialTicks);
        onDrawForeground(mc, mouseX, mouseY, partialTicks);
        onDrawText(mc, mouseX, mouseY, partialTicks);
        onDrawLast(mc, mouseX, mouseY, partialTicks);
    }

    protected void drawLabelLastLayer(MPGuiTickEvent<T> event)       { }
    protected void drawLabelForegroundLayer(MPGuiTickEvent<T> event) { }
    protected void drawLabelBackgroundLayer(MPGuiTickEvent<T> event) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        drawLabelBackground(event.getMc(), event.getMouseX(), event.getMouseY());
    }

    protected void drawLabelTextLayer(MPGuiTickEvent<T> event) {
        if (!visible) return;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        int color = colorContainer.getCalculatedColor(actionState, persistentState, 0);

        float scale        = 1.0F;
        float inverseScale = 1.0F;
        if (fontSize != MPFontSize.NORMAL || textScaleMultiplayer != 1.0F) {
            scale = fontSize.getScale() * textScaleMultiplayer;
            inverseScale = 1.0F / scale;
        }

        float innerX = calculatedInnerShape.x();
        float innerY = calculatedInnerShape.y();
        float innerW = calculatedInnerShape.width();
        float innerH = calculatedInnerShape.height();

        float centerY = (innerY + innerH / 2f) * inverseScale;
        float j       = centerY - labels.size() * (fontRenderer.FONT_HEIGHT + 1) * inverseScale / 2f;

        GlStateManager.pushMatrix();
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GlStateManager.scale(scale, scale, 1.0F);
        for (int k = 0; k < labels.size(); ++k) {
            float lineY = j + k * (fontRenderer.FONT_HEIGHT + 1) * inverseScale;
            if (centered) {
                GuiRenderHelper.drawCenteredString(
                        fontRenderer, labels.get(k), (innerX + innerW / 2f) * inverseScale, lineY, color, true
                );
            } else {
                GuiRenderHelper.drawString(fontRenderer, labels.get(k), innerX * inverseScale, lineY, color, true);
            }
        }
        GlStateManager.popMatrix();
    }

    @Nonnull @Override
    public GuiLabel setCentered() {
        centered = true;
        return this;
    }
}
