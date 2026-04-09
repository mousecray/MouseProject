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
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper;
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
import java.util.Objects;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiTextField<T extends MPGuiTextField<T>> extends GuiTextField implements MPGuiElement<T> {
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
    private final MPGuiMouseMoveEvent<T>   moveEvent      = new MPGuiMouseMoveEvent<>();
    private final MPGuiMouseDragEvent<T>   dragEvent      = new MPGuiMouseDragEvent<>();
    private final MPGuiMouseScrollEvent<T> scrollEvent    = new MPGuiMouseScrollEvent<>();
    private final MPGuiKeyEvent<T>         keyEvent       = new MPGuiKeyEvent<>();
    private final MPGuiSoundEvent<T>       soundEvent     = new MPGuiSoundEvent<>();
    private final MPGuiTextTypedEvent<T>   textTypedEvent = new MPGuiTextTypedEvent<>();

    protected final MPGuiElementStateManager stateManager = new MPGuiElementStateManager();

    protected final MutableGuiShape
            shape,
            calculatedShape      = new MutableGuiShape(),
            calculatedInnerShape = new MutableGuiShape();
    protected final MutableGuiVector calculatedTextOffsetTemp = new MutableGuiVector();

    @Nullable protected FontRenderer fontRenderer;
    @Nullable protected MPFontSize   fontSize;
    protected           MPGuiString  guiString   = MPGuiString.EMPTY();
    protected           MPGuiString  placeholder = MPGuiString.EMPTY();

    private   MPGuiTexturePack texturePack          = MPGuiTexturePack.EMPTY();
    protected MPGuiColorPack   colorPack            = MPGuiColorPack.TEXT_FIELD_SIMPLE();
    protected MPGuiColorPack   placeholderColorPack = MPGuiColorPack.TEXT_FIELD_PLACEHOLDER();
    protected MPGuiColorPack   cursorColorPack      = MPGuiColorPack.TEXT_FIELD_CURSOR();
    protected MPGuiColorPack   selectionColorPack   = MPGuiColorPack.TEXT_FIELD_SELECTION();
    private   MPGuiSoundPack   soundPack            = MPGuiSoundPack.EMPTY();

    protected int              tickDown             = -1;
    protected MutableGuiVector textOffset           = new MutableGuiVector();
    protected float            textScaleMultiplayer = 1.0F;
    private   GuiScaleRules    scaleRules           = new GuiScaleRules(MPGuiScaleType.FLOW);

    private boolean hasSelection = false;

    private MPGuiPanel<?> parent;
    private MPGuiPadding  padding = new MPGuiPadding(0);
    private MPGuiScreen   screen;

    public MPGuiTextField(MPGuiShape shape) {
        super(
                0, Minecraft.getMinecraft().fontRenderer, (int) shape.x(), (int) shape.y(),
                (int) shape.width(), (int) shape.height()
        );
        this.shape = shape.toMutable();

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

        stateManager.setChangeListener(() -> super.setFocused(stateManager.has(MPGuiElementState.FOCUSED)));

        super.setEnableBackgroundDrawing(true);
    }

    @SuppressWarnings("unchecked") @Override public T self() { return (T) this; }

    //Идентификация и иерархия
    @Override public void setId(int id) { this.id = id; }
    @Override public int getId()                       { return id; }
    @Override @Nullable public MPGuiScreen getScreen() { return screen; }

    @Override
    public void setScreen(@Nullable MPGuiScreen screen) {
        this.screen = screen;
        stateManager.lockForbidden(screen != null || getParent() != null);
    }

    @Override @Nullable public MPGuiPanel<?> getParent() { return parent; }

    @Override
    public void setParent(@Nullable MPGuiPanel<?> parent) {
        this.parent = parent;
        stateManager.lockForbidden(parent != null || getScreen() != null);
    }

    //Данные и состояние
    @Override public MPGuiString getGuiString() { return guiString; }

    @Override
    public void setGuiString(MPGuiString guiString) {
        Objects.requireNonNull(guiString, "guiString cannot be null. Use MPGuiString.EMPTY() instead.");
        String oldText      = getText();
        int    oldCursor    = getCursorPosition();
        int    oldSelection = getSelectionEnd();

        MPGuiEventFactory.pushTextTypedEvent(
                textTypedEvent, moveEvent.getMouseX(), moveEvent.getMouseY(),
                getCursorPosition(), getSelectionEnd(), oldText, guiString.get()
        );
        onAnyEventFire(textTypedEvent);

        if (!textTypedEvent.isCancelled()) {
            this.guiString = guiString;
            super.setText(guiString.get());
        } else {
            super.setText(oldText);
            setCursorPosition(oldCursor);
            setSelectionPos(oldSelection);
        }
    }

    public String getPlaceholder()                           { return placeholder.get(); }
    public void setPlaceholder(@Nullable String placeholder) { this.placeholder = MPGuiString.simple(placeholder); }

    public void setPlaceholder(MPGuiString placeholder) {
        Objects.requireNonNull(placeholder, "placeholder cannot be null. Use MPGuiString.EMPTY() instead.");
        this.placeholder = placeholder;
    }

    public boolean isHasSelection()                             { return hasSelection; }

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

    public MPGuiColorPack getPlaceholderColorPack() { return placeholderColorPack; }

    public void setPlaceholderColorPack(MPGuiColorPack colorPack) {
        Objects.requireNonNull(colorPack, "colorPack cannot be null. Use MPGuiColorPack.EMPTY() instead.");
        placeholderColorPack = colorPack;
    }

    public MPGuiColorPack getCursorColorPack() { return cursorColorPack; }

    public void setCursorColorPack(MPGuiColorPack colorPack) {
        Objects.requireNonNull(colorPack, "colorPack cannot be null. Use MPGuiColorPack.EMPTY() instead.");
        cursorColorPack = colorPack;
    }

    public MPGuiColorPack getSelectionColorPack() { return selectionColorPack; }

    public void setSelectionColorPack(MPGuiColorPack colorPack) {
        Objects.requireNonNull(colorPack, "colorPack cannot be null. Use MPGuiColorPack.EMPTY() instead.");
        selectionColorPack = colorPack;
    }

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
    public void setFontSize(@Nullable MPFontSize size) {
        if (getScreen() != null) {
            MouseProject.LOGGER.warn(
                    "FontSize cannot be setup immediately to MPGuiElement that added to container." +
                            " It set now, but actual element size will be updated on the next gui size calculation."
            );
        }
        fontSize = size;
    }

    @Override public float getTextScaleMultiplayer()                 { return textScaleMultiplayer; }
    @Override public void setTextScaleMultiplayer(float multiplayer) { textScaleMultiplayer = multiplayer; }

    //Геометрия
    @Override public MutableGuiShape getShape() { return shape; }
    @Override public MutableGuiShape getCalculatedShape()         { return calculatedShape; }
    @Override public MutableGuiShape getCalculatedInnerShape()    { return calculatedInnerShape; }

    @Override public GuiScaleRules getScaleRules()                { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules) { this.scaleRules = Objects.requireNonNull(scaleRules); }
    @Override public MPGuiPadding getPadding()                    { return padding; }
    @Override public void setPadding(MPGuiPadding padding)        { this.padding = Objects.requireNonNull(padding); }
    @Override public MutableGuiVector getTextOffset()             { return textOffset; }

    @Override
    public void calculateTextOffset(IGuiVector pDefSize, IGuiVector pContentSize) {
        MPGuiRenderHelper.calculateFlowComponentVector(
                calculatedTextOffsetTemp, pDefSize, pContentSize, getTextOffset()
        );
    }

    @Override
    public void setupShapeToVanilla(IGuiShape result) {
        x = (int) result.x();
        y = (int) result.y();
        width = (int) result.width();
        height = (int) result.height();
    }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedShape.offset(dx, dy);
        calculatedInnerShape.offset(dx, dy);
        x = (int) calculatedShape.x();
        y = (int) calculatedShape.y();
    }

    @Override
    public final void setEnableBackgroundDrawing(boolean enableBackgroundDrawing) {
        MouseProject.LOGGER.warn("backgroundDrawing is permanently enabled for MPGuiTextField. " +
                "If you are attempting to set it manually, please keep in mind that doing so will have no effect.");
    }

    //Диспетчеризация событий
    @Override
    public final void dispatchUpdate(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (tickDown >= 0) ++tickDown;
        super.updateCursorCounter();

        MPGuiEventFactory.pushTickEvent(updateEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(updateEvent);
        if (!updateEvent.isCancelled()) onUpdate(updateEvent);

        int             diffX     = mouseX - moveEvent.getMouseX();
        int             diffY     = mouseY - moveEvent.getMouseY();
        MPMoveDirection direction = MPMoveDirection.getMoveDirection(diffX, diffY);
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
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.ENTER);
            onMouseEnter(moveEvent);
        }
    }

    @Override
    public final void dispatchMouseLeave(Minecraft mc, int mouseX, int mouseY) {
        stateManager.remove(MPGuiElementState.HOVERED);
        MPGuiEventFactory.pushMouseMoveEvent(moveEvent, mouseX, mouseY, null);
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.LEAVE);
            onMouseLeave(moveEvent);
        }
    }

    @Override
    public final boolean dispatchMousePressed(Minecraft mc, int mouseX, int mouseY, int mouseButton) {
        if (!calculatedShape.contains(mouseX, mouseY)) return false;
        if (!isEnabled() || !isVisible()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.DISABLED);
            return false;
        }

        if (stateManager.has(MPGuiElementState.FAIL)) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.FAIL);
            return false;
        }

        hasSelection = checkIsOnText(mouseX, mouseY);
        if (hasSelection && mouseButton == 0) setCursorPosition(getCharIndexAtMouse(mouseX));

        tickDown = 0;
        stateManager.add(MPGuiElementState.PRESSED);

        MPGuiEventFactory.pushMouseClickEvent(pressEvent, mouseX, mouseY);
        onAnyEventFire(pressEvent);
        if (!pressEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.PRESS);
            onMousePressed(pressEvent);
        }
        return true;
    }

    @Override
    public final void dispatchMouseReleased(Minecraft mc, int mouseX, int mouseY, int state) {
        tickDown = -1;
        hasSelection = false;
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
        if (tickDown >= 0) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, mouseX, mouseY, dir, diffX, diffY, tickDown);
            onAnyEventFire(dragEvent);

            if (!dragEvent.isCancelled()) {
                if (hasSelection) {
                    setSelectionPos(getCharIndexAtMouse(mouseX));
                    dragEvent.consume();
                }

                dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.DRAG);
                onMouseDragged(dragEvent);
            }
            return dragEvent.isConsumed();
        }
        return false;
    }

    @Override
    public final boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll) {
        MPGuiEventFactory.pushMouseScrollEvent(scrollEvent, mouseX, mouseY, MPScrollDirection.getScrollDirection(scroll), scroll);
        onAnyEventFire(scrollEvent);
        if (!scrollEvent.isCancelled()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.SCROLL);
            onMouseScrolled(scrollEvent);
        }
        return scrollEvent.isConsumed();
    }

    @Override
    public final boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode) {
        if (!isFocused() || !isVisible()) return false;

        String oldText      = getText();
        int    oldCursor    = getCursorPosition();
        int    oldSelection = getSelectionEnd();

        boolean handled = super.textboxKeyTyped(typedChar, keyCode);

        if (handled) {
            if (!oldText.equals(getText())) {
                MPGuiEventFactory.pushTextTypedEvent(
                        textTypedEvent, mouseX, mouseY, getCursorPosition(), getSelectionEnd(), oldText, getText()
                );
                onAnyEventFire(textTypedEvent);
                if (!textTypedEvent.isCancelled()) onTextTyped(textTypedEvent);

                if (textTypedEvent.isCancelled()) {
                    super.setText(oldText);
                    setCursorPosition(oldCursor);
                    setSelectionPos(oldSelection);
                } else {
                    if (!textTypedEvent.isConsumed()) guiString = MPGuiString.simple(getText());
                }
            }

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
            MPGuiEventFactory.pushSoundEvent(soundEvent, moveEvent.getMouseX(), moveEvent.getMouseY(), soundHandler, sound, source);
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
        if (texture != null)
            texture.draw(event.getMc(), calculatedShape.x(), calculatedShape.y(), calculatedShape.width(), calculatedShape.height());
    }

    protected void onDrawForeground(MPGuiTickEvent<T> event) { }

    protected void onDrawText(MPGuiTickEvent<T> event) {
        if (getText().isEmpty() && !isFocused() && placeholder.get() != null && !placeholder.get().isEmpty()) {
            FontRenderer fr     = getFontRenderer();
            int          pColor = placeholderColorPack.getCalculatedColor(stateManager);

            MPFontSize fs           = getFontSize();
            float      scale        = fs.getScale() * textScaleMultiplayer;
            float      inverseScale = 1.0F / scale;

            float innerX = calculatedInnerShape.x();
            float innerY = calculatedInnerShape.y();
            float innerH = calculatedInnerShape.height();

            float logicalX = innerX * inverseScale;
            float logicalY = (innerY + innerH / 2f) * inverseScale - (fr.FONT_HEIGHT / 1.4f) * inverseScale;

            float textX = Math.round(logicalX * scale) / scale;
            float textY = Math.round(logicalY * scale) / scale;

            GlStateManager.pushMatrix();
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager.scale(scale, scale, 1.0F);

            MPGuiRenderHelper.drawString(fr, placeholder.get(), textX, textY, pColor, fs != MPFontSize.SMALL);

            GlStateManager.popMatrix();
        } else {
            drawCustomTextLayer(event);
        }
    }

    private void drawCustomTextLayer(MPGuiTickEvent<T> event) {
        float scale        = getFontSize().getScale() * getTextScaleMultiplayer();
        float inverseScale = 1.0F / scale;

        FontRenderer fontRenderer = getFontRenderer();
        int          textColor    = colorPack.getCalculatedColor(stateManager);
        String       fullText     = getText();

        int cursorPos       = getCursorPosition() - lineScrollOffset;
        int selectionEndPos = getSelectionEnd() - lineScrollOffset;

        float innerX = calculatedInnerShape.x();
        float innerY = calculatedInnerShape.y();
        float innerH = calculatedInnerShape.height();

        int scaledAvailableWidth = (int) (calculatedInnerShape.width() * inverseScale);

        float logicalX = innerX * inverseScale;
        float logicalY = (innerY + innerH / 2f) * inverseScale - (fontRenderer.FONT_HEIGHT / 2f);

        float textX = Math.round(logicalX * scale) / scale;
        float textY = Math.round(logicalY * scale) / scale;

        GlStateManager.pushMatrix();
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.scale(scale, scale, 1.0F);

        String visibleText = fontRenderer.trimStringToWidth(fullText.substring(lineScrollOffset), scaledAvailableWidth);

        if (selectionEndPos < 0) selectionEndPos = 0;
        if (selectionEndPos > visibleText.length()) selectionEndPos = visibleText.length();

        boolean showCursor    = isFocused() && cursorCounter / 6 % 2 == 0 && cursorPos >= 0 && cursorPos <= visibleText.length();
        boolean isCursorAtEnd = getCursorPosition() < fullText.length() || fullText.length() >= getMaxStringLength();

        float  cursorX          = textX;
        String textBeforeCursor = null;
        if (!visibleText.isEmpty()) textBeforeCursor = cursorPos >= 0 && cursorPos <= visibleText.length()
                ? visibleText.substring(0, cursorPos) : visibleText;

        if (!visibleText.isEmpty()) cursorX = textX + fontRenderer.getStringWidth(textBeforeCursor);

        if (cursorPos < 0) cursorX = textX;
        else if (cursorPos > visibleText.length()) cursorX = textX + (float) scaledAvailableWidth;
        else if (isCursorAtEnd) cursorX--;

        if (!visibleText.isEmpty()) {
            float currentX = MPGuiRenderHelper.drawString(
                    fontRenderer, textBeforeCursor, textX, textY, textColor, fontSize != MPFontSize.SMALL
            );

            if (cursorPos >= 0 && cursorPos < visibleText.length()) MPGuiRenderHelper.drawString(
                    fontRenderer, visibleText.substring(cursorPos), currentX,
                    textY, textColor, fontSize != MPFontSize.SMALL
            );
        }

        if (showCursor) {
            int cursorColor = cursorColorPack.getCalculatedColor(stateManager);
            if (isCursorAtEnd) MPGuiRenderHelper.drawRect(
                    cursorX, textY - 1, cursorX + 1, textY + 1 + fontRenderer.FONT_HEIGHT, cursorColor
            );
            else MPGuiRenderHelper.drawString(
                    fontRenderer, "|", cursorX, textY, cursorColor, fontSize != MPFontSize.SMALL
            );
        }

        if (selectionEndPos != cursorPos) {
            float selectionEndX  = textX + fontRenderer.getStringWidth(visibleText.substring(0, selectionEndPos));
            float maxX           = textX + (float) scaledAvailableWidth;
            int   selectionColor = selectionColorPack.getCalculatedColor(stateManager);
            drawSelectionBox(
                    cursorX, textY - 1, selectionEndX - 1,
                    textY + 1 + fontRenderer.FONT_HEIGHT, textX, maxX,
                    selectionColor
            );
        }

        GlStateManager.popMatrix();
    }

    private void drawSelectionBox(float startX, float startY, float endX, float endY, float minX, float maxX, int color) {
        if (startX < endX) {
            float i = startX;
            startX = endX;
            endX = i;
        }
        if (startY < endY) {
            float j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > maxX) endX = maxX;
        if (startX > maxX) startX = maxX;
        if (endX < minX) endX = minX;
        if (startX < minX) startX = minX;

        Tessellator   tessellator   = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float[]       glColors      = MPGuiColorPack.intToColor(color);
        GlStateManager.color(glColors[0], glColors[1], glColors[2], glColors[3]);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void onDrawLast(MPGuiTickEvent<T> event)             { }

    protected void onUpdate(MPGuiTickEvent<T> event)               { }
    protected void onMouseEnter(MPGuiMouseMoveEvent<T> event)      { }
    protected void onMouseLeave(MPGuiMouseMoveEvent<T> event)      { }
    protected void onMousePressed(MPGuiMouseClickEvent<T> event)   { }
    protected void onMouseReleased(MPGuiMouseClickEvent<T> event)  { }
    protected void onMouseDragged(MPGuiMouseDragEvent<T> event)    { }
    protected void onMouseScrolled(MPGuiMouseScrollEvent<T> event) { }

    protected void onKeyTyped(MPGuiKeyEvent<T> event) {
        if (!event.isCancelled() && (event.getKeyCode() == Keyboard.KEY_RETURN || event.getKeyCode() == Keyboard.KEY_NUMPADENTER)) {
            dispatchMousePressed(event.getMc(), x + width / 2, y + height / 2, 0);
            dispatchMouseReleased(event.getMc(), x + width / 2, y + height / 2, 0);
            event.consume();
        }
    }

    protected void onTextTyped(MPGuiTextTypedEvent<T> event) { }

    protected void onPlaySound(MPGuiSoundEvent<T> event) {
        event.getHandler().playSound(PositionedSoundRecord.getMasterRecord(event.getSound(), 1.0F));
    }

    protected void onAnyEventFire(MPGuiEvent<T> event) { }

    public void onClick(MPGuiMouseClickEvent<T> event) { }

    protected boolean checkIsOnText(int mouseX, int mouseY) {
        FontRenderer fr    = getFontRenderer();
        float        scale = getFontSize().getScale();

        float centerY    = calculatedInnerShape.y() + calculatedInnerShape.height() / 2f;
        float halfHeight = (fr.FONT_HEIGHT + 2) * scale / 2f;

        float textStartX = calculatedInnerShape.x();
        float textEndX   = calculatedInnerShape.x() + calculatedInnerShape.width();

        return mouseX >= textStartX && mouseX <= textEndX && mouseY >= centerY - halfHeight && mouseY <= centerY + halfHeight;
    }

    protected int getCharIndexAtMouse(int mouseX) {
        FontRenderer fr           = getFontRenderer();
        float        scale        = getFontSize().getScale();
        float        inverseScale = 1.0f / scale;

        float textX = calculatedInnerShape.x();
        int   relX  = (int) ((mouseX - textX) * inverseScale);

        String visibleText = fr.trimStringToWidth(
                getText().substring(lineScrollOffset),
                (int) (calculatedInnerShape.width() * inverseScale)
        );
        return fr.trimStringToWidth(visibleText, relX).length() + lineScrollOffset;
    }

    //Интеграция с vanilla
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return dispatchMousePressed(Minecraft.getMinecraft(), mouseX, mouseY, mouseButton);
    }
    @Override
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        return dispatchKeyTyped(Minecraft.getMinecraft(), moveEvent.getMouseX(), moveEvent.getMouseY(), typedChar, keyCode);
    }
    @Override
    public final void drawTextBox() {
        dispatchDraw(
                Minecraft.getMinecraft(), moveEvent.getMouseX(), moveEvent.getMouseY(),
                Minecraft.getMinecraft().getRenderPartialTicks()
        );
    }
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

    @Override
    public void writeText(String textToWrite) {
        String newText = internalWriteText(textToWrite);
        MPGuiEventFactory.pushTextTypedEvent(
                textTypedEvent, moveEvent.getMouseX(), moveEvent.getMouseY(),
                getCursorPosition(), getSelectionEnd(), getText(), newText
        );
        onAnyEventFire(textTypedEvent);
        if (!textTypedEvent.isCancelled()) {
            guiString = MPGuiString.simple(newText);
            super.writeText(textToWrite);
        }
    }

    @Override
    public void deleteFromCursor(int num) {
        String newText = internalDeleteFromCursor(num);
        MPGuiEventFactory.pushTextTypedEvent(
                textTypedEvent, moveEvent.getMouseX(), moveEvent.getMouseY(), getCursorPosition(), getSelectionEnd(),
                getText(), newText
        );
        onAnyEventFire(textTypedEvent);
        if (!textTypedEvent.isCancelled()) {
            guiString = MPGuiString.simple(newText);
            super.deleteFromCursor(num);
        }
    }

    private String internalWriteText(String textToWrite) {
        String s  = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int    i  = Math.min(getCursorPosition(), getSelectionEnd());
        int    j  = Math.max(getCursorPosition(), getSelectionEnd());
        int    k  = getMaxStringLength() - getText().length() - (i - j);

        if (!getText().isEmpty()) s = s + getText().substring(0, i);
        if (k < s1.length()) s = s + s1.substring(0, k);
        else s = s + s1;
        if (!getText().isEmpty() && j < getText().length()) s = s + getText().substring(j);

        return s;
    }

    private String internalDeleteFromCursor(int num) {
        if (!getText().isEmpty()) {
            if (getSelectionEnd() != getCursorPosition()) return internalWriteText("");
            else {
                boolean flag = num < 0;
                int     i    = flag ? getCursorPosition() + num : getCursorPosition();
                int     j    = flag ? getCursorPosition() : getCursorPosition() + num;
                String  s    = "";

                if (i >= 0) s = getText().substring(0, i);
                if (j < getText().length()) s = s + getText().substring(j);
                return s;
            }
        }
        return "";
    }

    @Override
    public void setSelectionPos(int position) {
        int textLength = getText().length();

        if (position > textLength) position = textLength;
        if (position < 0) position = 0;

        selectionEnd = position;

        FontRenderer fr = getFontRenderer();
        if (lineScrollOffset > textLength) {
            lineScrollOffset = textLength;
        }

        float scale                = getFontSize().getScale() * getTextScaleMultiplayer();
        int   scaledAvailableWidth = (int) (calculatedInnerShape.width() / scale);

        String s = fr.trimStringToWidth(getText().substring(lineScrollOffset), scaledAvailableWidth);
        int    k = s.length() + lineScrollOffset;

        if (position == lineScrollOffset) {
            lineScrollOffset -= fr.trimStringToWidth(getText(), scaledAvailableWidth, true).length();
        }

        if (position > k) lineScrollOffset += position - k;
        else if (position <= lineScrollOffset) lineScrollOffset -= lineScrollOffset - position;

        lineScrollOffset = net.minecraft.util.math.MathHelper.clamp(lineScrollOffset, 0, textLength);
    }

    @Override public boolean getVisible() { return isVisible(); }

    @Override
    public void setVisible(boolean isVisible) {
        if (isVisible) stateManager.remove(MPGuiElementState.HIDDEN);
        else stateManager.add(MPGuiElementState.HIDDEN);
    }
}
