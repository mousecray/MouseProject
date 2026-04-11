/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.core.components.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.core.components.sound.MPSoundSourceType;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.event.*;
import ru.mousecray.mouseproject.client.gui.core.misc.MPClickType;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.core.misc.MPMoveDirection;
import ru.mousecray.mouseproject.client.gui.core.misc.MPScrollDirection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;


@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiPanel<T extends MPGuiPanel<T>> implements MPGuiElement<T> {
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

    protected final List<MPGuiElement<?>> children = new ArrayList<>();

    private final Map<MPGuiElement<?>, MPGuiMargin> childMargins = new WeakHashMap<>();
    private final Map<MPGuiElement<?>, MPGuiVector> childOffsets = new WeakHashMap<>();

    private MPGuiPadding    padding    = new MPGuiPadding(0);
    private MPGuiScaleRules scaleRules = new MPGuiScaleRules(MPGuiScaleType.FLOW);

    private final   MPMutableGuiShape shape;
    private final   MPMutableGuiShape calculatedShape      = new MPMutableGuiShape();
    protected final MPMutableGuiShape calculatedInnerShape = new MPMutableGuiShape();

    protected final MPMutableGuiVector measureTemp        = new MPMutableGuiVector();
    protected final MPMutableGuiShape  childAvailableTemp = new MPMutableGuiShape();
    protected final MPMutableGuiShape  innerShapeTemp     = new MPMutableGuiShape();
    protected final float[]            marginTemp         = new float[4];

    private MPGuiScreen   screen;
    private MPGuiPanel<?> parent;

    private int id;

    protected MPGuiTexturePack texturePack = MPGuiTexturePack.EMPTY();
    protected MPGuiColorPack   colorPack   = MPGuiColorPack.CONTROL_SIMPLE();
    protected MPGuiSoundPack   soundPack   = MPGuiSoundPack.EMPTY();

    protected final MPGuiElementStateManager stateManager = new MPGuiElementStateManager();

    @Nullable private MPGuiElement<?> lastHoveredElement  = null;
    @Nullable private MPGuiElement<?> lastSelectedElement = null;
    private           int             tickDown            = -1;

    public MPGuiPanel(MPGuiShape shape) {
        this.shape = shape.toMutable();
        stateManager.setForbidden(MPGuiElementState.FOCUSED, true);

        Minecraft mc = Minecraft.getMinecraft();
        T         th = self();
        updateEvent.bind(mc, th);
        drawBGEvent.bind(mc, th);
        drawFGEvent.bind(mc, th);
        drawLastEvent.bind(mc, th);
        drawTextEvent.bind(mc, th);
        pressEvent.bind(mc, th);
        releaseEvent.bind(mc, th);
        clickEvent.bind(mc, th);
        moveEvent.bind(mc, th);
        dragEvent.bind(mc, th);
        scrollEvent.bind(mc, th);
        keyEvent.bind(mc, th);
        soundEvent.bind(mc, th);
    }

    public List<MPGuiElement<?>> getChildren()               { return children; }

    @SuppressWarnings("unchecked") @Override public T self() { return (T) this; }

    //Идентификация и иерархия
    @Override public void setId(int id) { this.id = id; }
    @Override public int getId()                       { return id; }

    @Override @Nullable public MPGuiScreen getScreen() { return screen; }

    @Override
    public void setScreen(@Nullable MPGuiScreen screen) {
        this.screen = screen;
        stateManager.lockForbidden(screen != null || getParent() != null);
        for (MPGuiElement<?> child : children) child.setScreen(screen);
    }

    @Override @Nullable public MPGuiPanel<?> getParent() { return parent; }

    @Override
    public void setParent(@Nullable MPGuiPanel<?> parent) {
        this.parent = parent;
        stateManager.lockForbidden(parent != null || getScreen() != null);
    }

    //Данные и состояние
    @Override public MPGuiString getGuiString() { return MPGuiString.EMPTY(); }
    @Override public void setGuiString(MPGuiString guiString)   { }

    @Override public MPGuiElementStateManager getStateManager() { return stateManager; }

    @Override public MPGuiTexturePack getTexturePack()          { return texturePack; }

    @Override
    public void setTexturePack(MPGuiTexturePack texturePack) {
        Objects.requireNonNull(texturePack, "texturePack cannot be null. Use MPGuiTexturePack.EMPTY() instead.");
        this.texturePack = texturePack;
    }

    @Override public MPGuiSoundPack getSoundPack() { return soundPack; }

    @Override
    public void setSoundPack(MPGuiSoundPack soundPack) {
        Objects.requireNonNull(soundPack, "soundPack cannot be null. Use MPGuiSoundPack.EMPTY() instead.");
        this.soundPack = soundPack;
    }

    @Override public MPGuiColorPack getColorPack() { return colorPack; }

    @Override
    public void setColorPack(MPGuiColorPack colorPack) {
        Objects.requireNonNull(colorPack, "colorPack cannot be null. Use MPGuiColorPack.EMPTY() instead.");
        this.colorPack = colorPack;
    }

    @Override
    public FontRenderer getFontRenderer() {
        if (getScreen() != null) return getScreen().getFontRenderer();
        return Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void setFontRenderer(@Nullable FontRenderer fr) {
        MouseProject.LOGGER.warn("MPGuiPanel cannot support custom FontRenderer");
    }

    @Override public MPFontSize getFontSize() { return MPFontSize.NORMAL; }

    @Override
    public void setFontSize(MPFontSize size) {
        MouseProject.LOGGER.warn("MPGuiPanel cannot support custom FontSize");
    }

    @Override public float getTextScaleMultiplayer() { return 0.0f; }

    @Override
    public void setTextScaleMultiplayer(float multiplayer) {
        MouseProject.LOGGER.warn("MPGuiPanel cannot support custom TextScaleMultiplayer");
    }

    //Геометрия
    @Override public MPMutableGuiShape getShape() { return shape; }
    @Override public MPMutableGuiShape getCalculatedShape()         { return calculatedShape; }
    @Override public MPMutableGuiShape getCalculatedInnerShape()    { return calculatedInnerShape; }

    @Override public MPGuiScaleRules getScaleRules()                { return scaleRules; }
    @Override public void setScaleRules(MPGuiScaleRules scaleRules) { this.scaleRules = scaleRules; }
    @Override public MPGuiPadding getPadding()                      { return padding; }
    @Override public void setPadding(MPGuiPadding padding)          { this.padding = padding; }
    @Override public MPMutableGuiVector getTextOffset()             { return new MPMutableGuiVector(); }

    public void addChild(MPGuiElement<?> child, @Nullable MPGuiMargin margin, @Nullable MPGuiVector offset) {
        children.add(child);
        childMargins.put(child, margin != null ? margin : MPGuiMargin.ZERO());
        childOffsets.put(child, offset != null ? offset : MPGuiVector.ZERO);

        child.setParent(this);
        if (screen != null) {
            child.setScreen(screen);
            child.setId(screen.genNextElementID());
        }
    }

    public void addChild(MPGuiElement<?> child)                 { addChild(child, MPGuiMargin.ZERO(), MPGuiVector.ZERO); }

    protected MPGuiMargin getChildMargin(MPGuiElement<?> child) { return childMargins.getOrDefault(child, MPGuiMargin.ZERO()); }
    protected MPGuiVector getChildOffset(MPGuiElement<?> child) { return childOffsets.getOrDefault(child, MPGuiVector.ZERO); }

    @Nullable
    public MPGuiElement<?> getLastSelectedElementRecursively() {
        if (lastSelectedElement instanceof MPGuiPanel) {
            MPGuiElement<?> nested = ((MPGuiPanel<?>) lastSelectedElement).getLastSelectedElementRecursively();
            return nested != null ? nested : lastSelectedElement;
        }
        return lastSelectedElement;
    }

    @Override
    public void onCalculated(IGuiVector pDefSize, IGuiVector pContentSize, IGuiShape innerCalcShape) {
        innerShapeTemp.withShape(innerCalcShape);
        layoutChildren(pDefSize, pContentSize, innerShapeTemp);
    }

    protected abstract void layoutChildren(IGuiVector parentDefaultSize, IGuiVector parentContentSize, MPMutableGuiShape inner);

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedShape.offset(dx, dy);
        calculatedInnerShape.offset(dx, dy);
        for (MPGuiElement<?> child : children) child.offsetCalculatedShape(dx, dy);
    }

    //Диспетчеризация событий
    @Override
    public final void dispatchUpdate(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (tickDown >= 0) ++tickDown;

        MPGuiEventFactory.pushTickEvent(updateEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(updateEvent);
        if (!updateEvent.isCancelled()) onUpdate(updateEvent);

        int             diffX     = mouseX - moveEvent.getMouseX();
        int             diffY     = mouseY - moveEvent.getMouseY();
        MPMoveDirection direction = MPMoveDirection.getMoveDirection(diffX, diffY);
        MPGuiEventFactory.pushMouseMoveEvent(moveEvent, mouseX, mouseY, direction);

        if (tickDown >= 0 && direction != null) dispatchMouseDragged(mc, mouseX, mouseY, direction, diffX, diffY);

        for (MPGuiElement<?> child : children) child.dispatchUpdate(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void dispatchProcessHover(Minecraft mc, int mouseX, int mouseY) {
        if (!isVisible()) return;

        MPGuiElement<?> currentHovered = null;

        for (int k = children.size() - 1; k >= 0; k--) {
            MPGuiElement<?> child = children.get(k);
            if (child.mouseHover(mc, mouseX, mouseY) && child.isVisible()) {
                currentHovered = child;
                break;
            }
        }

        if (lastHoveredElement != currentHovered) {
            if (lastHoveredElement != null) lastHoveredElement.dispatchMouseLeave(mc, mouseX, mouseY);
            if (currentHovered != null) currentHovered.dispatchMouseEnter(mc, mouseX, mouseY);
            lastHoveredElement = currentHovered;
        }

        if (currentHovered != null) currentHovered.dispatchProcessHover(mc, mouseX, mouseY);
    }

    @Override
    public final void dispatchMouseEnter(Minecraft mc, int mouseX, int mouseY) {
        stateManager.add(MPGuiElementState.HOVERED);
        MPGuiEventFactory.pushMouseMoveEvent(
                moveEvent, mouseX, mouseY, MPMoveDirection.calculateMoveDirection(mouseX, mouseY, moveEvent)
        );
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.ENTER);
            onMouseEnter(moveEvent);
        }
    }

    @Override
    public final void dispatchMouseLeave(Minecraft mc, int mouseX, int mouseY) {
        if (lastHoveredElement != null) {
            lastHoveredElement.dispatchMouseLeave(mc, mouseX, mouseY);
            lastHoveredElement = null;
        }

        stateManager.remove(MPGuiElementState.HOVERED);
        MPGuiEventFactory.pushMouseMoveEvent(
                moveEvent, mouseX, mouseY, MPMoveDirection.calculateMoveDirection(mouseX, mouseY, moveEvent)
        );
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.LEAVE);
            onMouseLeave(moveEvent);
        }
    }

    @Override
    public final boolean dispatchMousePressed(Minecraft mc, int mouseX, int mouseY, int mouseButton) {
        if (!isEnabled() || !isVisible()) {
            if (calculatedShape.contains(mouseX, mouseY)) {
                dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.DISABLED);
            }
            return false;
        }

        MPGuiEventFactory.pushMouseClickEvent(pressEvent, mouseX, mouseY);
        onAnyEventFire(pressEvent);
        if (pressEvent.isCancelled()) return true;

        for (int k = children.size() - 1; k >= 0; k--) {
            MPGuiElement<?> child = children.get(k);
            if (child.dispatchMousePressed(mc, mouseX, mouseY, mouseButton)) {
                lastSelectedElement = child;
                return true;
            }
        }

        if (!calculatedShape.contains(mouseX, mouseY)) return false;

        if (stateManager.has(MPGuiElementState.FAIL)) dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.FAIL);

        tickDown = 0;
        stateManager.add(MPGuiElementState.PRESSED);

        dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.PRESS);
        onMousePressed(pressEvent);
        return true;
    }

    @Override
    public final void dispatchMouseReleased(Minecraft mc, int mouseX, int mouseY, int state) {
        if (lastSelectedElement != null) {
            lastSelectedElement.dispatchMouseReleased(mc, mouseX, mouseY, state);
            lastSelectedElement = null;
            return;
        }

        tickDown = -1;
        stateManager.remove(MPGuiElementState.PRESSED);

        MPGuiEventFactory.pushMouseClickEvent(releaseEvent, mouseX, mouseY);
        onAnyEventFire(releaseEvent);
        if (!releaseEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.RELEASE);
            onMouseReleased(releaseEvent);
            if (calculatedShape.contains(mouseX, mouseY)) {
                MPGuiEventFactory.pushMouseClickEvent(clickEvent, mouseX, mouseY);
                onAnyEventFire(clickEvent);
                if (!clickEvent.isCancelled()) {
                    dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.CLICK);
                    onClick(clickEvent);
                }
            }
        }
    }

    @Override
    public final boolean dispatchMouseDragged(Minecraft mc, int mouseX, int mouseY, MPMoveDirection dir, int diffX, int diffY) {
        if (lastSelectedElement != null) {
            return lastSelectedElement.dispatchMouseDragged(mc, mouseX, mouseY, dir, diffX, diffY);
        }

        if (tickDown >= 0) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, mouseX, mouseY, dir, diffX, diffY, tickDown);
            onAnyEventFire(dragEvent);
            if (!dragEvent.isCancelled()) {
                dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.DRAG);
                onMouseDragged(dragEvent);
            }
            return !dragEvent.isCancelled();
        }
        return false;
    }

    @Override
    public final boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll) {
        if (!calculatedShape.contains(mouseX, mouseY) || !isVisible()) return false;

        for (int k = children.size() - 1; k >= 0; k--) {
            MPGuiElement<?> child = children.get(k);
            if (child.getCalculatedShape().contains(mouseX, mouseY)) {
                if (child.dispatchMouseScrolled(mc, mouseX, mouseY, scroll)) return true;
            }
        }

        MPGuiEventFactory.pushMouseScrollEvent(
                scrollEvent, mouseX, mouseY, MPScrollDirection.getScrollDirection(scroll), scroll
        );
        onAnyEventFire(scrollEvent);
        if (!scrollEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.SCROLL);
            onMouseScrolled(scrollEvent);
        }

        return scrollEvent.isConsumed();
    }

    @Override
    public final boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode) {
        if (!isVisible()) return false;

        for (MPGuiElement<?> child : children) {
            if (child.dispatchKeyTyped(mc, mouseX, mouseY, typedChar, keyCode)) return true;
        }

        if (stateManager.has(MPGuiElementState.FOCUSED)) {
            MPGuiEventFactory.pushKeyEvent(keyEvent, mouseX, mouseY, typedChar, keyCode);
            onAnyEventFire(keyEvent);

            if (!keyEvent.isCancelled()) {
                dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.KEY_TYPED);
                onKeyTyped(keyEvent);
            }

            return keyEvent.isConsumed();
        }
        return false;
    }

    @Override
    public final void dispatchPlaySound(Minecraft mc, SoundHandler soundHandler, MPSoundSourceType source) {
        SoundEvent sound = soundPack.getSound(source);
        if (sound != null) {
            MPGuiEventFactory.pushSoundEvent(
                    soundEvent, moveEvent.getMouseX(), moveEvent.getMouseY(), soundHandler, sound, source
            );
            onAnyEventFire(soundEvent);
            if (!soundEvent.isCancelled()) onPlaySound(soundEvent);
        }
    }

    //Рендеринг
    @Override
    public final void dispatchDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawBGEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawBGEvent);
        if (!drawBGEvent.isCancelled()) {
            onDrawBackground(drawBGEvent);
            for (MPGuiElement<?> child : children) {
                child.dispatchDrawBackground(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public final void dispatchDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawFGEvent);
        if (!drawFGEvent.isCancelled()) {
            onDrawForeground(drawFGEvent);
            for (MPGuiElement<?> child : children) {
                child.dispatchDrawForeground(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public final void dispatchDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawTextEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawTextEvent);
        if (!drawTextEvent.isCancelled()) {
            onDrawText(drawTextEvent);
            for (MPGuiElement<?> child : children) {
                child.dispatchDrawText(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public final void dispatchDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawLastEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawLastEvent);
        if (!drawLastEvent.isCancelled()) {
            onDrawLast(drawLastEvent);
            for (MPGuiElement<?> child : children) {
                child.dispatchDrawLast(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    //Обработчики событий
    protected void onPlaySound(MPGuiSoundEvent<T> event) {
        event.getHandler().playSound(PositionedSoundRecord.getMasterRecord(event.getSound(), 1.0F));
    }

    protected void onDrawBackground(MPGuiTickEvent<T> event) {
        MPGuiTexture texture = texturePack.getCalculatedTexture(stateManager);
        if (texture != null) {
            texture.draw(
                    event.getMc(),
                    calculatedShape.x(), calculatedShape.y(),
                    calculatedShape.width(), calculatedShape.height()
            );
        }
    }

    protected void onDrawForeground(MPGuiTickEvent<T> event)       { }
    protected void onDrawText(MPGuiTickEvent<T> event)             { }
    protected void onDrawLast(MPGuiTickEvent<T> event)             { }

    protected void onUpdate(MPGuiTickEvent<T> event)               { }
    protected void onMouseEnter(MPGuiMouseMoveEvent<T> event)      { }
    protected void onMouseLeave(MPGuiMouseMoveEvent<T> event)      { }
    protected void onMousePressed(MPGuiMouseClickEvent<T> event)   { }
    protected void onMouseReleased(MPGuiMouseClickEvent<T> event)  { }
    protected void onMouseDragged(MPGuiMouseDragEvent<T> event)    { }
    protected void onMouseScrolled(MPGuiMouseScrollEvent<T> event) { }

    protected void onKeyTyped(MPGuiKeyEvent<T> event)              { }

    protected void onAnyEventFire(MPGuiEvent<T> event)             { }

    protected void onClick(MPGuiMouseClickEvent<T> event)          { }

    //Интеграция с vanilla
    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) {
        return MPGuiElement.super.mouseHover(mc, mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return MPGuiElement.super.mousePressed(mc, mouseX, mouseY);
    }

    @Override public final int getHoverState(boolean mouseOver)           { return MPGuiElement.super.getHoverState(mouseOver); }
    @Override public void mouseReleased(int mouseX, int mouseY)           { MPGuiElement.super.mouseReleased(mouseX, mouseY); }
    @Override public final void playPressSound(SoundHandler soundHandler) { MPGuiElement.super.playPressSound(soundHandler); }
    @Override public boolean isMouseOver()                                { return MPGuiElement.super.isMouseOver(); }

    public void collectElements() {
        if (screen == null) return;
        for (MPGuiElement<?> child : children) {
            if (child instanceof MPGuiPanel) ((MPGuiPanel<?>) child).collectElements();
            else if (child instanceof GuiButton) screen.getButtonList().add((GuiButton) child);
            else if (child instanceof GuiLabel) screen.getLabelList().add((GuiLabel) child);
            else if (child instanceof GuiTextField) screen.getFieldsList().add((GuiTextField) child);
        }
    }

    public void removeAllChildren() {
        children.clear();
        childMargins.clear();
        childOffsets.clear();
        onChildrenCleared();
    }

    protected void onChildrenCleared() { }
}