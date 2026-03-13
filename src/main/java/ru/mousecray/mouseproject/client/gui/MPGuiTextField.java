package ru.mousecray.mouseproject.client.gui;

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
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.*;
import ru.mousecray.mouseproject.client.gui.misc.*;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiTextField<T extends MPGuiTextField<T>> extends GuiTextField implements MPGuiElement<T> {
    private static final int CURSOR_RECT_COLOR      = -3092272;
    private static final int PLACEHOLDER_TEXT_COLOR = 0x686868;

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
    private final   MPGuiMouseMoveEvent<T> moveEvent      = new MPGuiMouseMoveEvent<>();
    private final   MPGuiMouseDragEvent<T> dragEvent      = new MPGuiMouseDragEvent<>();
    private final   MPGuiTextTypedEvent<T> textTypedEvent = new MPGuiTextTypedEvent<>();
    private final   MPGuiSoundEvent<T>     soundEvent     = new MPGuiSoundEvent<>();
    protected final MutableGuiShape        elementShape   = new MutableGuiShape(),
            calculatedElementShape                        = new MutableGuiShape();
    protected final MutableGuiShape calculatedInnerShape = new MutableGuiShape();
    protected final MPFontSize      fontSize;

    protected StateColorContainer colorContainer = StateColorContainer.Builder
            .create(14737632)
            .addState(GuiButtonPersistentState.DISABLED, 7368816)
            .build();

    @Nullable private GuiButtonActionState     actionState     = null;
    @Nullable private GuiButtonPersistentState persistentState = GuiButtonPersistentState.NORMAL;
    private final     MPGuiTexturePack         texturePack;

    private                 int           tickDown    = -1;
    private                 int           partialTick;
    private                 boolean       isSelecting = false;
    @Nullable private final SoundEvent    soundClick;
    private                 MPGuiString   placeholder = MPGuiString.simple("");
    private                 MPGuiString   guiString   = MPGuiString.simple("");
    private                 boolean       hovered;
    private                 GuiScaleRules scaleRules  = new GuiScaleRules(GuiScaleType.FLOW);
    private                 MPGuiPanel<?> parent;
    private                 GuiPadding    padding     = new GuiPadding(0);
    private                 MPGuiScreen   screen;

    public MPGuiTextField(FontRenderer fontRenderer,
                          @Nullable String placeholder, @Nullable String defaultText,
                          GuiShape elementShape,
                          @Nullable MPGuiTexturePack texturePack,
                          @Nullable SoundEvent soundClick, MPFontSize fontSize) {
        super(0, fontRenderer, (int) elementShape.x(), (int) elementShape.y(), (int) elementShape.width(), (int) elementShape.height());
        this.elementShape.withShape(elementShape);
        this.fontSize = fontSize;
        if (defaultText == null) defaultText = "";
        setText(defaultText);
        setMaxStringLength(999);
        setEnableBackgroundDrawing(true);
        this.placeholder = MPGuiString.simple(placeholder);
        this.texturePack = texturePack == null ? MPGuiTexturePack.EMPTY : texturePack;
        this.soundClick = soundClick;
    }

    @Override public void setId(int id)                          { this.id = id; }
    @Override public int getId()                                 { return id; }

    @SuppressWarnings("unchecked") @Override public T self()     { return (T) this; }
    public MutableGuiShape getDrawShape()                        { return elementShape; }
    @Override public MutableGuiShape getElementShape()           { return elementShape; }
    public MutableGuiShape getCalculatedDrawShape()              { return calculatedElementShape; }
    @Override public MutableGuiShape getCalculatedElementShape() { return calculatedElementShape; }
    @Nullable public String getPlaceholder()                     { return placeholder.get(); }
    public void setPlaceholder(@Nullable String placeholder)     { this.placeholder = MPGuiString.simple(placeholder); }
    public void setPlaceholder(MPGuiString placeholder)          { this.placeholder = placeholder; }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        GuiRenderHelper.calculateFlowComponentShape(calculatedElementShape, parentDefaultSize, parentContentSize, elementShape, scaleRules, available);

        GuiPadding pad  = getPadding();
        float      padL = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getLeft());
        float      padT = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getTop());
        float      padR = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getRight());
        float      padB = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getBottom());

        calculatedInnerShape.withShape(calculatedElementShape);
        calculatedInnerShape.grow(padL, padT, -padL - padR, -padT - padB);

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

    @Override public void setTexturePack(MPGuiTexturePack texturePack) { }
    @Override public void setElementShape(IGuiShape elementShape)      { this.elementShape.withShape(elementShape); }
    @Override public GuiScaleRules getScaleRules()                     { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules)      { this.scaleRules = scaleRules; }
    @Override public void setPadding(GuiPadding padding)               { this.padding = padding; }
    @Override public GuiPadding getPadding()                           { return padding; }
    @Override public void setScreen(MPGuiScreen screen)                { this.screen = screen; }
    @Override public MPGuiScreen getScreen()                           { return screen; }
    @Override public void setParent(MPGuiPanel<?> parent)              { this.parent = parent; }
    @Override public MPGuiPanel<?> getParent()                         { return parent; }
    @Override public void setTextOffset(IGuiVector offset)             { }
    @Override public MutableGuiVector getTextOffset()                  { return new MutableGuiVector(); }

    @Override public void setGuiString(MPGuiString guiString) {
        this.guiString = guiString;
        super.setText(guiString.get());
    }
    @Override public MPGuiString getGuiString()                              { return guiString; }


    @Override @Nullable public GuiButtonActionState getActionState()         { return actionState; }
    @Override @Nullable public GuiButtonPersistentState getPersistentState() { return persistentState; }
    @Override public MPGuiTexturePack getTexturePack()                       { return texturePack; }

    @Override
    public boolean applyState(@Nullable GuiButtonPersistentState state) {
        processVanillaPersistentState(state);
        persistentState = state;
        return true;
    }

    @Override
    public final void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) {
        super.setEnableBackgroundDrawing(true);
    }

    protected boolean applyActionState(@Nullable GuiButtonActionState state) {
        processVanillaActionState(state);
        actionState = state;
        return true;
    }

    private void processVanillaActionState(@Nullable GuiButtonActionState state) {

    }

    private void processVanillaPersistentState(@Nullable GuiButtonPersistentState state) {
        setEnabled(state != GuiButtonPersistentState.DISABLED);
        setVisible(state != null);
    }

    protected void onAnyEventFire(MPGuiEvent<T> event) { }

    @Override public final void onUpdate0(Minecraft mc, int mouseX, int mouseY) {
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
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        String oldText      = getText();
        int    oldCursor    = getCursorPosition();
        int    oldSelection = getSelectionEnd();

        boolean result = super.textboxKeyTyped(typedChar, keyCode);

        if (result) {
            MPGuiEventFactory.pushTextTypedEvent(
                    textTypedEvent, self(), Minecraft.getMinecraft(),
                    moveEvent.getMouseX(), moveEvent.getMouseY(),
                    getCursorPosition(), getSelectionEnd(),
                    oldText, getText()
            );
            onAnyEventFire(textTypedEvent);
            if (textTypedEvent.isCancelled()) {
                setText(oldText);
                setCursorPosition(oldCursor);
                setSelectionPos(oldSelection);
            }
        }

        return result;
    }

    protected final void onKeyTyped0(char typedChar, int keyCode) {
        textboxKeyTyped(typedChar, keyCode);
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
        if (tickDown >= 0) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, self(), mc, mouseX, mouseY, direction, diffX, diffY, tickDown);
            onAnyEventFire(dragEvent);
            if (!dragEvent.isCancelled()) {
                onMouseDragged(dragEvent);
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean onMousePressed0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiEventFactory.pushMouseClickEvent(pressEvent, self(), mc, mouseX, mouseY);
        onAnyEventFire(pressEvent);
        if (!pressEvent.isCancelled()) {
            if (persistentState == null
                    || persistentState == GuiButtonPersistentState.DISABLED) return false;
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

    public void mouseReleased(int mouseX, int mouseY)                 { onMouseReleased0(Minecraft.getMinecraft(), mouseX, mouseY); }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) { }

    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && checkIsOnText(mouseX, mouseY)) {
            int index = getCharIndexAtMouse(mouseX);
            setCursorPosition(index);
            return true;
        }
        return false;
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY);
    }

    protected void onClick(MPGuiMouseClickEvent<T> event) { }

    @Override
    public final void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawTextBoxBackground(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawTextBoxForeground(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawTextBoxText(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawTextBoxLast(mc, mouseX, mouseY, partialTicks);
    }

    public final void drawTextBox(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawTextBox(mc, mouseX, mouseY, partialTicks);
    }

    protected final void onDrawTextBoxBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawBGEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawBGEvent);
        if (persistentState != null && !drawBGEvent.isCancelled()) drawTextBoxBackgroundLayer(drawBGEvent);
    }

    protected final void onDrawTextBoxForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawFGEvent);
        if (persistentState != null && !drawFGEvent.isCancelled()) drawTextBoxForegroundLayer(drawFGEvent);
    }

    protected final void onDrawTextBoxText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawTextEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawTextEvent);
        if (persistentState != null && !drawTextEvent.isCancelled()) drawTextBoxTextLayer(drawTextEvent);
    }

    protected final void onDrawTextBoxLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawLastEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawLastEvent);
        if (persistentState != null && !drawLastEvent.isCancelled()) drawTextBoxLastLayer(drawLastEvent);
    }

    @Override
    public final void drawTextBox() {
        onDrawTextBox(Minecraft.getMinecraft(), moveEvent.getMouseX(), moveEvent.getMouseY(), Minecraft.getMinecraft().getRenderPartialTicks());
    }

    protected final void onDrawTextBox(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawTextBoxBackground(mc, mouseX, mouseY, partialTicks);
        onDrawTextBoxForeground(mc, mouseX, mouseY, partialTicks);
        onDrawTextBoxText(mc, mouseX, mouseY, partialTicks);
        onDrawTextBoxLast(mc, mouseX, mouseY, partialTicks);
    }

    @Override @Nullable
    public MPGuiElement<?> findTopHovered(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY) ? this : null;
    }

    protected void onUpdate(MPGuiTickEvent<T> event) { }

    protected void onMouseDragged(MPGuiMouseDragEvent<T> event) {
        if (isSelecting) {
            setSelectionPos(getCharIndexAtMouse(event.getMouseX()));
            event.setCancelled(true);
        }
    }

    protected void onMouseReleased(MPGuiMouseClickEvent<T> event) {
        isSelecting = false;
    }

    protected void onMouseEnter(MPGuiMouseMoveEvent<T> event) { }
    protected void onMouseLeave(MPGuiMouseMoveEvent<T> event) { }

    protected void onMousePressed(MPGuiMouseClickEvent<T> event) {
        isSelecting = checkIsOnText(event.getMouseX(), event.getMouseY());
        if (isSelecting) {
            int index = getCharIndexAtMouse(event.getMouseX());
            setCursorPosition(index);
        }
    }

    protected boolean checkIsOnText(int mouseX, int mouseY) {
        FontRenderer fr         = Minecraft.getMinecraft().fontRenderer;
        float        scale      = fontSize.getScale();
        float        centerY    = y + height / 2f;
        float        halfHeight = (fr.FONT_HEIGHT + 2) * scale / 2f;

        float textStartX = x + width / 35f;
        float textEndX   = x + width - width / 35f;

        return mouseX >= textStartX && mouseX <= textEndX &&
                mouseY >= centerY - halfHeight && mouseY <= centerY + halfHeight;
    }

    protected int getCharIndexAtMouse(int mouseX) {
        FontRenderer fr           = Minecraft.getMinecraft().fontRenderer;
        float        scale        = fontSize.getScale();
        float        inverseScale = 1.0f / scale;
        float        textX        = x + width / 35f;

        int    relX        = (int) ((mouseX - textX) * inverseScale);
        String visibleText = fr.trimStringToWidth(getText().substring(lineScrollOffset), (int) (width * inverseScale));
        return fr.trimStringToWidth(visibleText, relX).length() + lineScrollOffset;
    }

    protected final int getHoverState(boolean mouseOver) {
        return persistentState == GuiButtonPersistentState.DISABLED ? 0 : mouseOver ? 2 : 1;
    }

    public final boolean isMouseOver() { return hovered; }

    public final void drawTextBoxForegroundLayer(int mouseX, int mouseY) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, self(), Minecraft.getMinecraft(), mouseX, mouseY, Minecraft.getMinecraft().getRenderPartialTicks());
        onAnyEventFire(drawFGEvent);
        if (!drawFGEvent.isCancelled()) drawTextBoxForegroundLayer(drawFGEvent);
    }

    protected void drawTextBoxForegroundLayer(MPGuiTickEvent<T> event) {
        if (persistentState == null) return;

        float scale        = fontSize.getScale();
        float inverseScale = 1.0F / scale;
        if (fontSize != MPFontSize.NORMAL) {
            scale = fontSize.getScale();
            inverseScale = 1.0F / scale;
        }

        FontRenderer fontRenderer = event.getMc().fontRenderer;
        int          textColor    = colorContainer.getCalculatedColor(actionState, persistentState, 0);
        String       visibleText  = getText();
        int          cursorPos    = getCursorPosition() - lineScrollOffset;

        float innerX = calculatedInnerShape.x();
        float innerY = calculatedInnerShape.y();
        float innerH = calculatedInnerShape.height();

        float textX = innerX * inverseScale;
        float textY = (innerY + innerH / 2f) * inverseScale - (fontRenderer.FONT_HEIGHT / 2f);

        int selectionEndPos = getSelectionEnd() - lineScrollOffset;

        GlStateManager.pushMatrix();
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GlStateManager.scale(scale, scale, 1.0F);

        String text = getText();
        if (text != null && text.isEmpty() && placeholder.get() != null && !placeholder.get().isEmpty()) {
            GuiRenderHelper.drawString(
                    fontRenderer, placeholder.get(), textX, textY, PLACEHOLDER_TEXT_COLOR,
                    fontSize != MPFontSize.SMALL
            );
        }

        if (selectionEndPos < 0) selectionEndPos = 0;
        if (selectionEndPos > visibleText.length()) selectionEndPos = visibleText.length();

        boolean showCursor    = isFocused() && partialTick % 20 < 10 && cursorPos >= 0 && cursorPos <= visibleText.length();
        boolean isCursorAtEnd = getCursorPosition() < getText().length() || getText().length() >= getMaxStringLength();

        float cursorX = textX;
        if (!visibleText.isEmpty()) {
            String textBeforeCursor = cursorPos >= 0 && cursorPos <= visibleText.length()
                    ? visibleText.substring(0, cursorPos)
                    : visibleText;
            cursorX = textX + fontRenderer.getStringWidth(textBeforeCursor);
        }

        float maxTextWidth = calculatedInnerShape.width() * inverseScale;

        if (cursorPos < 0) cursorX = textX;
        else if (cursorPos > visibleText.length()) cursorX = textX + maxTextWidth;
        else if (isCursorAtEnd) cursorX--;

        if (showCursor) {
            if (isCursorAtEnd) {
                GuiRenderHelper.drawRect(
                        cursorX, textY - 1, cursorX + 1, textY + 1 + fontRenderer.FONT_HEIGHT, CURSOR_RECT_COLOR
                );
            } else {
                GuiRenderHelper.drawString(
                        fontRenderer, "|", cursorX, textY, textColor, fontSize != MPFontSize.SMALL
                );
            }
        }

        if (selectionEndPos != cursorPos) {
            float selectionEndX = textX + fontRenderer.getStringWidth(visibleText.substring(0, selectionEndPos));

            float maxX = (calculatedInnerShape.x() + calculatedInnerShape.width()) * inverseScale;

            drawSelectionBox(
                    cursorX, textY - 1, selectionEndX - 1,
                    textY + 1 + fontRenderer.FONT_HEIGHT, textX, maxX
            );
        }

        GlStateManager.popMatrix();
    }

    protected void drawTextBoxLastLayer(MPGuiTickEvent<T> event) { }

    protected void drawTextBoxBackgroundLayer(MPGuiTickEvent<T> event) {
        MPGuiTexture texture = texturePack.getCalculatedTexture(actionState, persistentState);

        if (texture != null) {
            texture.draw(
                    event.getMc(),
                    calculatedElementShape.x(), calculatedElementShape.y(),
                    calculatedElementShape.width(), calculatedElementShape.height()
            );
        }
    }

    protected void drawTextBoxTextLayer(MPGuiTickEvent<T> event) {
        if (persistentState == null) return;

        float scale        = fontSize.getScale();
        float inverseScale = 1.0F / scale;
        if (fontSize != MPFontSize.NORMAL) {
            scale = fontSize.getScale();
            inverseScale = 1.0F / scale;
        }

        FontRenderer fontRenderer = event.getMc().fontRenderer;
        int          textColor    = colorContainer.getCalculatedColor(actionState, persistentState, 0);

        float innerX = calculatedInnerShape.x();
        float innerY = calculatedInnerShape.y();
        float innerH = calculatedInnerShape.height();

        int    maxInnerWidth = (int) (calculatedInnerShape.width() * inverseScale);
        String visibleText   = fontRenderer.trimStringToWidth(getText().substring(lineScrollOffset), maxInnerWidth);

        float textX     = innerX * inverseScale;
        float textY     = (innerY + innerH / 2f) * inverseScale - (fontRenderer.FONT_HEIGHT / 2f);
        int   cursorPos = getCursorPosition() - lineScrollOffset;

        if (!visibleText.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GlStateManager.scale(scale, scale, 1.0F);
            String textBeforeCursor = cursorPos >= 0 && cursorPos <= visibleText.length()
                    ? visibleText.substring(0, cursorPos)
                    : visibleText;
            float currentX = GuiRenderHelper.drawString(
                    fontRenderer, textBeforeCursor, textX, textY, textColor, fontSize != MPFontSize.SMALL
            );

            if (cursorPos >= 0 && cursorPos < visibleText.length()) {
                GuiRenderHelper.drawString(
                        fontRenderer, visibleText.substring(cursorPos), currentX, textY, textColor,
                        fontSize != MPFontSize.SMALL
                );
            }
            GlStateManager.popMatrix();
        }
    }

    private void drawSelectionBox(float startX, float startY, float endX, float endY, float minX, float maxX) {
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
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
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
    }

    @Override
    public void setText(String text) {
        MPGuiEventFactory.pushTextTypedEvent(textTypedEvent, self(), Minecraft.getMinecraft(),
                moveEvent.getMouseX(), moveEvent.getMouseY(), getCursorPosition(), getSelectionEnd(), getText(), text);
        onAnyEventFire(textTypedEvent);
        if (!textTypedEvent.isCancelled()) {
            guiString = MPGuiString.simple(text);
            super.setText(text);
        }
    }

    @Override
    public void writeText(String textToWrite) {
        String newText = internalWriteText(textToWrite);
        MPGuiEventFactory.pushTextTypedEvent(textTypedEvent, self(), Minecraft.getMinecraft(),
                moveEvent.getMouseX(), moveEvent.getMouseY(), getCursorPosition(), getSelectionEnd(),
                getText(), newText);
        onAnyEventFire(textTypedEvent);
        if (!textTypedEvent.isCancelled()) {
            guiString = MPGuiString.simple(newText);
            super.writeText(textToWrite);
        }
    }

    @Override
    public void deleteFromCursor(int num) {
        String newText = internalDeleteFromCursor(num);
        MPGuiEventFactory.pushTextTypedEvent(textTypedEvent, self(), Minecraft.getMinecraft(),
                moveEvent.getMouseX(), moveEvent.getMouseY(), getCursorPosition(), getSelectionEnd(),
                getText(), newText);
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
}
