/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui;

import mcp.MethodsReturnNonnullByDefault;
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
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.components.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.components.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.components.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.components.sound.SoundSourceType;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.components.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.*;
import ru.mousecray.mouseproject.client.gui.misc.MPClickType;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;
import ru.mousecray.mouseproject.client.gui.misc.ScrollDirection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    private final MPGuiMouseMoveEvent<T>   moveEvent   = new MPGuiMouseMoveEvent<>();
    private final MPGuiMouseDragEvent<T>   dragEvent   = new MPGuiMouseDragEvent<>();
    private final MPGuiMouseScrollEvent<T> scrollEvent = new MPGuiMouseScrollEvent<>();
    private final MPGuiKeyEvent<T>         keyEvent    = new MPGuiKeyEvent<>();
    private final MPGuiSoundEvent<T>       soundEvent  = new MPGuiSoundEvent<>();

    protected final MPGuiElementStateManager stateManager = new MPGuiElementStateManager();

    protected final MutableGuiShape
            shape,
            calculatedShape      = new MutableGuiShape(),
            calculatedInnerShape = new MutableGuiShape();
    protected final MutableGuiVector calculatedTextOffsetTemp = new MutableGuiVector();

    @Nullable protected FontRenderer     fontRenderer;
    @Nullable protected MPFontSize       fontSize;
    protected           MPGuiString      guiString;
    private             MPGuiTexturePack texturePack = MPGuiTexturePack.EMPTY;
    protected           MPGuiColorPack   colorPack   = MPGuiColorPack.CONTROL_SIMPLE();
    private             MPGuiSoundPack   soundPack   = MPGuiSoundPack.EMPTY;

    protected int              tickDown             = -1;
    protected MutableGuiVector textOffset           = new MutableGuiVector();
    protected float            textScaleMultiplayer = 1.0F;
    private   GuiScaleRules    scaleRules           = new GuiScaleRules(GuiScaleType.FLOW);

    private boolean centered;
    private boolean enabled = true, visible = true, hovered = false;

    @Nullable private MPGuiPanel<?> parent;
    private           GuiPadding    padding = new GuiPadding(0);
    @Nullable private MPGuiScreen   screen;

    public MPGuiLabel(MPGuiString text, GuiShape shape) {
        super(
                Minecraft.getMinecraft().fontRenderer, 0,
                (int) shape.x(), (int) shape.y(),
                (int) shape.width(), (int) shape.height(),
                14737632
        );
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

        stateManager.setChangeListener(() -> {
            enabled = !stateManager.has(MPGuiElementState.DISABLED);
            visible = !stateManager.has(MPGuiElementState.HIDDEN);
            hovered = stateManager.has(MPGuiElementState.HOVERED);
        });

        setGuiString(text);
    }

    @SuppressWarnings("unchecked") @Override public T self() { return (T) this; }

    //Идентификация и иерархия
    @Override public void setId(int id) { this.id = id; }
    @Override public int getId()                       { return id; }
    @Override @Nullable public MPGuiScreen getScreen() { return screen; }

    @Override
    public void setScreen(@Nullable MPGuiScreen screen) {
        this.screen = screen;
        if (screen != null) stateManager.lockForbidden();
    }

    @Override @Nullable public MPGuiPanel<?> getParent() { return parent; }

    @Override
    public void setParent(@Nullable MPGuiPanel<?> parent) {
        this.parent = parent;
        if (parent != null) stateManager.lockForbidden();
    }

    //Данные и состояние
    @Override public String getText() { return guiString.get(); }

    @Override
    public void setText(String rawText) {
        guiString = MPGuiString.simple(rawText);
        labels.clear();
        String[] split = rawText.split("\n");
        labels.addAll(Arrays.asList(split));
    }

    @Override public MPGuiString getGuiString() { return guiString; }

    @Override
    public void setGuiString(MPGuiString guiString) {
        this.guiString = guiString;
        labels.clear();
        String[] split = guiString.get().split("\n");
        labels.addAll(Arrays.asList(split));
    }

    @Override public boolean isVisible()                               { return visible; }
    @Override public boolean isEnabled()                               { return enabled; }
    @Override public boolean isHovered()                               { return hovered; }
    @Override public boolean isFocused()                               { return stateManager.has(MPGuiElementState.FOCUSED); }
    @Override public boolean canBeFocused()                            { return !stateManager.isForbidden(MPGuiElementState.FOCUSED); }
    public void setCentered(boolean centered)                          { this.centered = centered; }
    public boolean isCentered()                                        { return centered; }

    @Override public MPGuiElementStateManager getStateManager()        { return stateManager; }

    @Override public MPGuiTexturePack getTexturePack()                 { return texturePack; }
    @Override public void setTexturePack(MPGuiTexturePack texturePack) { this.texturePack = Objects.requireNonNull(texturePack); }
    @Override public MPGuiSoundPack getSoundPack()                     { return soundPack; }
    @Override public void setSoundPack(MPGuiSoundPack soundPack)       { this.soundPack = Objects.requireNonNull(soundPack); }
    @Override public MPGuiColorPack getColorPack()                     { return colorPack; }
    @Override public void setColorPack(MPGuiColorPack colorPack)       { this.colorPack = Objects.requireNonNull(colorPack); }

    @Override
    public FontRenderer getFontRenderer() {
        if (fontRenderer != null) return fontRenderer;
        if (getScreen() != null) return getScreen().getFontRenderer();
        return Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void setFontRenderer(@Nullable FontRenderer fr) {
        if (getScreen() != null) {
            MouseProject.LOGGER.warn(
                    "FontRenderer cannot be setup immediately to MPGuiElement that added to container." +
                            " It set now, but actual element size will be updated on the next gui size calculation."
            );
        }
        fontRenderer = fr;
    }

    @Override
    public MPFontSize getFontSize() {
        if (fontSize != null) return fontSize;
        if (getScreen() != null) return getScreen().getFontSize();
        return MPFontSize.NORMAL;
    }

    @Override
    public void setFontSize(MPFontSize size) {
        if (getScreen() != null) {
            MouseProject.LOGGER.warn(
                    "FontSize cannot be setup immediately to MPGuiElement that added to container." +
                            " It set now, but actual element size will be updated on the next gui size calculation."
            );
        }
        fontSize = size;
    }

    //Геометрия
    @Override public void setShape(IGuiShape shape) { this.shape.withShape(shape); }
    @Override public MutableGuiShape getShape()                   { return shape; }
    @Override public MutableGuiShape getCalculatedShape()         { return calculatedShape; }
    @Override public MutableGuiShape getCalculatedInnerShape()    { return calculatedInnerShape; }

    @Override public GuiScaleRules getScaleRules()                { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules) { this.scaleRules = scaleRules; }
    @Override public GuiPadding getPadding()                      { return padding; }
    @Override public void setPadding(GuiPadding padding)          { this.padding = padding; }
    @Override public MutableGuiVector getTextOffset()             { return textOffset; }
    @Override public void setTextOffset(IGuiVector offset)        { textOffset.withVector(offset); }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        GuiRenderHelper.calculateFlowComponentShape(calculatedShape, parentDefaultSize, parentContentSize, shape, scaleRules, available);

        GuiPadding pad  = getPadding();
        float      padL = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getLeft());
        float      padT = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getTop());
        float      padR = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getRight());
        float      padB = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getBottom());

        calculatedInnerShape.withShape(calculatedShape);
        calculatedInnerShape.grow(-padL, -padT, -padR, -padB);

        if (textOffset != null) {
            GuiRenderHelper.calculateFlowComponentVector(calculatedTextOffsetTemp, parentDefaultSize, parentContentSize, textOffset);
        } else {
            calculatedTextOffsetTemp.withX(0).withY(0);
        }

        x = (int) calculatedShape.x();
        y = (int) calculatedShape.y();
        width = (int) calculatedShape.width();
        height = (int) calculatedShape.height();
    }

    @Override
    public void measurePreferred(IGuiVector pDefSize, IGuiVector pContSize, float sugX, float sugY, MutableGuiVector result) {
        GuiRenderHelper.measurePreferredWithScaleRules(
                pDefSize, pContSize, sugX, sugY, result, shape, scaleRules
        );
        GuiRenderHelper.addPaddingToPreferred(pDefSize, pContSize, result, getPadding(), scaleRules);
    }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedShape.offset(dx, dy);
        calculatedInnerShape.offset(dx, dy);
        x = (int) calculatedShape.x();
        y = (int) calculatedShape.y();
    }

    //Диспетчеризация событий
    @Override
    public final void dispatchUpdate(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (tickDown >= 0) ++tickDown;

        MPGuiEventFactory.pushTickEvent(updateEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(updateEvent);
        if (!updateEvent.isCancelled()) onUpdate(updateEvent);

        int           diffX     = mouseX - moveEvent.getMouseX();
        int           diffY     = mouseY - moveEvent.getMouseY();
        MoveDirection direction = MoveDirection.getMoveDirection(diffX, diffY);
        MPGuiEventFactory.pushMouseMoveEvent(moveEvent, mouseX, mouseY, direction);

        if (tickDown >= 0 && direction != null) dispatchMouseDragged(mc, mouseX, mouseY, direction, diffX, diffY);
    }

    @Override public final void dispatchProcessHover(Minecraft mc, int mouseX, int mouseY) { }

    @Override
    public final void dispatchMouseEnter(Minecraft mc, int mouseX, int mouseY) {
        stateManager.add(MPGuiElementState.HOVERED);
        MPGuiEventFactory.pushMouseMoveEvent(moveEvent, mouseX, mouseY, null);
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.ENTER);
            onMouseEnter(moveEvent);
        }
    }

    @Override
    public final void dispatchMouseLeave(Minecraft mc, int mouseX, int mouseY) {
        stateManager.remove(MPGuiElementState.HOVERED);
        MPGuiEventFactory.pushMouseMoveEvent(moveEvent, mouseX, mouseY, null);
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.LEAVE);
            onMouseLeave(moveEvent);
        }
    }

    @Override
    public final boolean dispatchMousePressed(Minecraft mc, int mouseX, int mouseY, int mouseButton) {
        if (!calculatedShape.contains(mouseX, mouseY)) return false;
        if (!isEnabled() || !isVisible()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.DISABLED);
            return false;
        }

        if (stateManager.has(MPGuiElementState.FAIL)) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.FAIL);
            return true;
        }

        tickDown = 0;
        stateManager.add(MPGuiElementState.PRESSED);

        MPGuiEventFactory.pushMouseClickEvent(pressEvent, mouseX, mouseY);
        onAnyEventFire(pressEvent);
        if (!pressEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.PRESS);
            onMousePressed(pressEvent);
        }

        return true;
    }

    @Override
    public final void dispatchMouseReleased(Minecraft mc, int mouseX, int mouseY, int state) {
        tickDown = -1;
        stateManager.remove(MPGuiElementState.PRESSED);

        MPGuiEventFactory.pushMouseClickEvent(releaseEvent, mouseX, mouseY);
        onAnyEventFire(releaseEvent);

        if (!releaseEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.RELEASE);
            onMouseReleased(releaseEvent);
            if (calculatedShape.contains(mouseX, mouseY)) {
                MPGuiEventFactory.pushMouseClickEvent(clickEvent, mouseX, mouseY);
                onAnyEventFire(clickEvent);
                if (!clickEvent.isCancelled()) {
                    dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.CLICK);
                    onClick(clickEvent);
                }
            }
        }
    }

    @Override
    public final boolean dispatchMouseDragged(Minecraft mc, int mouseX, int mouseY, MoveDirection dir, int diffX, int diffY) {
        if (tickDown >= 0) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, mouseX, mouseY, dir, diffX, diffY, tickDown);
            onAnyEventFire(dragEvent);

            if (!dragEvent.isCancelled()) {
                dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.DRAG);
                onMouseDragged(dragEvent);
            }

            return !dragEvent.isCancelled();
        }
        return false;
    }

    @Override
    public final boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll) {
        MPGuiEventFactory.pushMouseScrollEvent(scrollEvent, mouseX, mouseY, ScrollDirection.getScrollDirection(scroll), scroll);
        onAnyEventFire(scrollEvent);

        if (!scrollEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.SCROLL);
            onMouseScrolled(scrollEvent);
        }

        return scrollEvent.isConsumed();
    }

    @Override
    public final boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode) {
        if (!stateManager.has(MPGuiElementState.FOCUSED)) return false;

        MPGuiEventFactory.pushKeyEvent(keyEvent, mouseX, mouseY, typedChar, keyCode);
        onAnyEventFire(keyEvent);

        if (!keyEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), SoundSourceType.KEY_TYPED);
            onKeyTyped(keyEvent);
        }

        return keyEvent.isConsumed();
    }

    @Override
    public final void dispatchPlaySound(Minecraft mc, SoundHandler soundHandler, SoundSourceType source) {
        SoundEvent sound = soundPack.getSound(source);
        if (sound != null) {
            MPGuiEventFactory.pushSoundEvent(
                    soundEvent, moveEvent.getMouseX(), moveEvent.getMouseY(), soundHandler, sound, source
            );
            onAnyEventFire(soundEvent);
            if (!soundEvent.isCancelled()) onPlaySound(soundEvent);
        }
    }

    @Override
    public void dispatchDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawBGEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawBGEvent);
        if (!drawBGEvent.isCancelled()) onDrawBackground(drawBGEvent);
    }

    @Override
    public void dispatchDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawFGEvent);
        if (!drawFGEvent.isCancelled()) onDrawForeground(drawFGEvent);
    }

    @Override
    public void dispatchDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawTextEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawTextEvent);
        if (!drawTextEvent.isCancelled()) onDrawText(drawTextEvent);
    }

    @Override
    public void dispatchDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawLastEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawLastEvent);
        if (!drawLastEvent.isCancelled()) onDrawLast(drawLastEvent);
    }

    //Обработчики событий
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

    protected void onDrawForeground(MPGuiTickEvent<T> event) { }

    protected void onDrawText(MPGuiTickEvent<T> event) {
        if (labels.isEmpty()) return;

        FontRenderer fr    = getFontRenderer();
        MPFontSize   fs    = getFontSize();
        int          color = colorPack.getCalculatedColor(stateManager, 0xFFFFFF);

        float scale    = fs.getScale() * getTextScaleMultiplayer();
        float invScale = 1.0F / scale;

        float innerX = calculatedInnerShape.x();
        float innerY = calculatedInnerShape.y();
        float innerW = calculatedInnerShape.width();
        float innerH = calculatedInnerShape.height();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.scale(scale, scale, 1.0F);

        float totalTextHeight = labels.size() * fr.FONT_HEIGHT;
        float startY          = (innerY + innerH / 2f) * invScale - (totalTextHeight / 2f) + calculatedTextOffsetTemp.y() * invScale;

        for (int i = 0; i < labels.size(); i++) {
            String line  = labels.get(i);
            float  lineY = startY + (i * fr.FONT_HEIGHT);

            if (centered) {
                GuiRenderHelper.drawCenteredString(
                        fr, line,
                        (innerX + innerW / 2f) * invScale + calculatedTextOffsetTemp.x() * invScale,
                        lineY,
                        color,
                        fs != MPFontSize.SMALL
                );
            } else {
                GuiRenderHelper.drawString(
                        fr, line,
                        innerX * invScale + calculatedTextOffsetTemp.x() * invScale,
                        lineY,
                        color, fontSize != MPFontSize.SMALL
                );
            }
        }

        GlStateManager.popMatrix();
    }

    protected void onDrawLast(MPGuiTickEvent<T> event)             { }

    protected void onUpdate(MPGuiTickEvent<T> event)               { }
    protected void onMouseEnter(MPGuiMouseMoveEvent<T> event)      { }
    protected void onMouseLeave(MPGuiMouseMoveEvent<T> event)      { }
    protected void onMousePressed(MPGuiMouseClickEvent<T> event)   { }
    protected void onMouseReleased(MPGuiMouseClickEvent<T> event)  { }
    protected void onMouseDragged(MPGuiMouseDragEvent<T> event)    { }
    protected void onMouseScrolled(MPGuiMouseScrollEvent<T> event) { }

    protected void onKeyTyped(MPGuiKeyEvent<T> event)              { }

    protected void onPlaySound(MPGuiSoundEvent<T> event) {
        event.getHandler().playSound(PositionedSoundRecord.getMasterRecord(event.getSound(), 1.0F));
    }

    protected void onAnyEventFire(MPGuiEvent<T> event) { }

    public void onClick(MPGuiMouseClickEvent<T> event) { }

    //Интеграция с vanilla
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return isEnabled() && isVisible() && calculatedShape.contains(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        dispatchMouseReleased(Minecraft.getMinecraft(), mouseX, mouseY, 0);
    }

    @Override public final int getHoverState(boolean mouseOver) { return !isEnabled() ? 0 : mouseOver ? 2 : 1; }

    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) {
        return calculatedShape.contains(mouseX, mouseY);
    }

    @Override
    public final void playPressSound(SoundHandler soundHandler) {
        dispatchPlaySound(Minecraft.getMinecraft(), Minecraft.getMinecraft().getSoundHandler(), SoundSourceType.PRESS);
    }

    @Override
    public void drawLabel(Minecraft mc, int mouseX, int mouseY) {
        dispatchDraw(mc, mouseX, mouseY, mc.getRenderPartialTicks());
    }

    @Override
    protected void drawLabelBackground(Minecraft mc, int mouseX, int mouseY) {
        dispatchDrawBackground(mc, mouseX, mouseY, mc.getRenderPartialTicks());
    }

    @Override
    public void performClickFromVanilla() {
        if (!isEnabled() || !isVisible()) return;

        int centerX = (int) (x + width / 2f);
        int centerY = (int) (y + height / 2f);

        Minecraft mc = Minecraft.getMinecraft();
        dispatchMousePressed(mc, centerX, centerY, 0);
        dispatchMouseReleased(mc, centerX, centerY, 0);
    }

    @Override
    public GuiLabel setCentered() {
        setCentered(true);
        return this;
    }
}
