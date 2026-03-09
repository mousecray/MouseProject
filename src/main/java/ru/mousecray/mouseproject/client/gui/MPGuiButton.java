package ru.mousecray.mouseproject.client.gui;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
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
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexture;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper.measurePreferredWithScaleRules;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiButton<T extends MPGuiButton<T>> extends GuiButton implements MPGuiElement<T> {
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

    protected final MutableGuiShape elementShape, calculatedElementShape = new MutableGuiShape();
    protected final MutableGuiShape  calculatedInnerShape     = new MutableGuiShape();
    protected final MutableGuiVector calculatedTextOffsetTemp = new MutableGuiVector();
    protected final MPFontSize       fontSize;

    protected MPGuiString guiString = MPGuiString.simple("");

    @Nullable protected GuiButtonActionState     actionState     = null;
    @Nullable protected GuiButtonPersistentState persistentState = GuiButtonPersistentState.NORMAL;
    private             MPGuiTexturePack         texturePack;

    private   int                 tickDown             = -1;
    private   int                 partialTick;
    protected MutableGuiVector    textOffset           = new MutableGuiVector();
    protected float               textScaleMultiplayer = 1.0F;
    protected StateColorContainer colorContainer       = StateColorContainer.createDefault();
    private   GuiScaleRules       scaleRules           = new GuiScaleRules(GuiScaleType.FLOW);

    @Nullable private final SoundEvent soundClick;

    private MPGuiPanel<?> parent;
    private GuiPadding    padding = new GuiPadding(0);

    private MPGuiScreen screen;

    public MPGuiButton(
            @Nullable String text,
            GuiShape elementShape,
            @Nullable MPGuiTexturePack texturePack,
            @Nullable SoundEvent soundClick,
            MPFontSize fontSize) {

        super(0,
                (int) elementShape.x(), (int) elementShape.y(),
                (int) elementShape.width(), (int) elementShape.height(),
                text == null ? "" : text);

        this.elementShape = elementShape.toMutable();
        this.fontSize = fontSize;
        this.texturePack = texturePack == null ? MPGuiTexturePack.EMPTY : texturePack;
        this.soundClick = soundClick;
        guiString = MPGuiString.simple(text);
    }

    @Override public void setId(int id)                           { this.id = id; }

    @SuppressWarnings("unchecked") @Override public T self()      { return (T) this; }

    @Override public void setTextOffset(IGuiVector offset)        { textOffset.withVector(offset); }
    @Override public MutableGuiVector getTextOffset()             { return textOffset; }
    protected void setTextScaleMultiplayer(float multiplayer)     { textScaleMultiplayer = multiplayer; }
    @Override public void setElementShape(IGuiShape elementShape) { this.elementShape.withShape(elementShape); }
    @Override public MutableGuiShape getElementShape()            { return elementShape; }
    @Override public MutableGuiShape getCalculatedElementShape()  { return calculatedElementShape; }
    @Override public int getId()                                  { return id; }
    @Override public String getText()                             { return guiString.get(); }
    @Override public void setText(String text) {
        guiString = MPGuiString.simple(text);
        displayString = text;
    }
    @Override public void setGuiString(MPGuiString guiString) {
        this.guiString = guiString;
        displayString = guiString.get();
    }
    @Override public MPGuiString getGuiString()                              { return guiString; }
    @Override public GuiScaleRules getScaleRules()                           { return scaleRules; }
    @Override public void setScaleRules(GuiScaleRules scaleRules)            { this.scaleRules = scaleRules; }
    @Override public void setPadding(GuiPadding padding)                     { this.padding = padding; }
    @Override public GuiPadding getPadding()                                 { return padding; }

    @Override @Nullable public GuiButtonActionState getActionState()         { return actionState; }
    @Override @Nullable public GuiButtonPersistentState getPersistentState() { return persistentState; }
    @Override public MPGuiTexturePack getTexturePack()                       { return texturePack; }
    @Override public void setTexturePack(MPGuiTexturePack texturePack)       { this.texturePack = texturePack; }


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
        enabled = state != GuiButtonPersistentState.DISABLED;
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
    public final void onMouseEnter0(Minecraft mc, int mouseX, int mouseY) {
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            if (persistentState == null
                    || persistentState == GuiButtonPersistentState.DISABLED
                    || actionState == GuiButtonActionState.PRESSED) return;
            hovered = true;
            applyActionState(GuiButtonActionState.HOVER);
            onMouseEnter(moveEvent);
        }
    }

    @Override
    public final void onMouseLeave0(Minecraft mc, int mouseX, int mouseY) {
        onAnyEventFire(moveEvent);
        if (!moveEvent.isCancelled()) {
            if (actionState == GuiButtonActionState.HOVER) applyActionState(null);
            hovered = false;
            onMouseLeave(moveEvent);
        }
    }

    @Override
    public final void onMouseReleased0(Minecraft mc, int mouseX, int mouseY) {
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
            }
        }
    }

    @Override
    public final void onMousePressed0(Minecraft mc, int mouseX, int mouseY) {
        MPGuiEventFactory.pushMouseClickEvent(pressEvent, self(), mc, mouseX, mouseY);
        onAnyEventFire(pressEvent);
        if (!pressEvent.isCancelled()) {
            if (persistentState == null || persistentState == GuiButtonPersistentState.DISABLED) return;
            applyActionState(GuiButtonActionState.PRESSED);
            tickDown = 0;
            onMousePressed(pressEvent);
            onPlaySound0(mc, mc.getSoundHandler(), soundClick, SoundSourceType.PRESS);
        }
    }

    @Override
    public void onMouseDragged0(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY) {
        if (tickDown >= 0) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, self(), mc, mouseX, mouseY, direction, diffX, diffY, tickDown);
            onAnyEventFire(dragEvent);
            if (!dragEvent.isCancelled()) onMouseDragged(dragEvent);
        }
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


    @Override public void mouseReleased(int mouseX, int mouseY) { onMouseReleased0(Minecraft.getMinecraft(), mouseX, mouseY); }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (tickDown >= 0) {
            int           diffX     = mouseX - moveEvent.getMouseX();
            int           diffY     = mouseY - moveEvent.getMouseY();
            MoveDirection direction = MoveDirection.getMoveDirection(diffX, diffY);
            if (direction != null) onMouseDragged0(mc, mouseX, mouseY, direction, diffX, diffY);
        }
    }

    protected void onAnyEventFire(MPGuiEvent<T> event) { }

    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public abstract void onClick(MPGuiMouseClickEvent<T> event);

    @Override
    public final void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawButton(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawBGEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawBGEvent);
        if (persistentState != null && !drawBGEvent.isCancelled()) drawButtonBackgroundLayer(drawBGEvent);
    }

    @Override
    public final void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawFGEvent);
        if (persistentState != null && !drawFGEvent.isCancelled()) drawButtonForegroundLayer(drawFGEvent);
    }

    @Override
    public final void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawTextEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawTextEvent);
        if (persistentState != null && !drawTextEvent.isCancelled()) drawButtonTextLayer(drawTextEvent);
    }

    @Override
    public final void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawLastEvent, self(), mc, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawLastEvent);
        if (persistentState != null && !drawLastEvent.isCancelled()) drawButtonLastLayer(drawLastEvent);
    }

    protected final void onDrawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        onDrawBackground(mc, mouseX, mouseY, partialTicks);
        onDrawForeground(mc, mouseX, mouseY, partialTicks);
        onDrawText(mc, mouseX, mouseY, partialTicks);
        onDrawLast(mc, mouseX, mouseY, partialTicks);
    }

    protected void onUpdate(MPGuiTickEvent<T> event)              { }
    protected void onMouseDragged(MPGuiMouseDragEvent<T> event)   { }
    protected void onMouseReleased(MPGuiMouseClickEvent<T> event) { }
    protected void onMouseEnter(MPGuiMouseMoveEvent<T> event)     { }
    protected void onMouseLeave(MPGuiMouseMoveEvent<T> event)     { }
    protected void onMousePressed(MPGuiMouseClickEvent<T> event)  { }

    protected void drawButtonTextLayer(MPGuiTickEvent<T> event) {
        String text = guiString.get();
        if (text != null && !text.isEmpty()) {
            FontRenderer fr    = event.getMc().fontRenderer;
            int          color = colorContainer.getCalculatedColor(actionState, persistentState, packedFGColour);

            float scale    = fontSize.getScale() * textScaleMultiplayer;
            float invScale = 1.0F / scale;

            float innerX = calculatedInnerShape.x();
            float innerY = calculatedInnerShape.y();
            float innerW = calculatedInnerShape.width();
            float innerH = calculatedInnerShape.height();

            GlStateManager.pushMatrix();
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GlStateManager.scale(scale, scale, 1.0F);

            GuiRenderHelper.drawCenteredString(
                    fr, text,
                    (innerX + innerW / 2f) * invScale + calculatedTextOffsetTemp.x() * invScale,
                    (innerY + innerH / 2f) * invScale - (fr.FONT_HEIGHT / 2f) + calculatedTextOffsetTemp.y() * invScale,
                    color,
                    fontSize != MPFontSize.SMALL
            );

            GlStateManager.popMatrix();
        }
    }

    @Override
    public final void drawButtonForegroundLayer(int mouseX, int mouseY) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, self(), Minecraft.getMinecraft(), mouseX, mouseY, Minecraft.getMinecraft().getRenderPartialTicks());
        onAnyEventFire(drawFGEvent);
        if (!drawFGEvent.isCancelled()) drawButtonForegroundLayer(drawFGEvent);
    }

    @Override
    protected final int getHoverState(boolean mouseOver) {
        return persistentState == GuiButtonPersistentState.DISABLED ? 0 : mouseOver ? 2 : 1;
    }

    @Override public final boolean isMouseOver() { return hovered; }

    @Override
    public final void playPressSound(SoundHandler soundHandler) {
        onPlaySound0(Minecraft.getMinecraft(), Minecraft.getMinecraft().getSoundHandler(), soundClick, SoundSourceType.PRESS);
    }

    protected void drawButtonLastLayer(MPGuiTickEvent<T> event)       { }
    protected void drawButtonForegroundLayer(MPGuiTickEvent<T> event) { }

    protected void drawButtonBackgroundLayer(MPGuiTickEvent<T> event) {
        MPGuiTexture texture = texturePack.getCalculatedTexture(actionState, persistentState);
        if (texture != null) {
            texture.draw(
                    event.getMc(),
                    calculatedElementShape.x(), calculatedElementShape.y(),
                    calculatedElementShape.width(), calculatedElementShape.height()
            );
        }
    }

    @Override @Nullable
    public MPGuiElement<?> findTopHovered(Minecraft mc, int mouseX, int mouseY) {
        return calculatedElementShape.contains(mouseX, mouseY) ? this : null;
    }

    @Override public void setParent(MPGuiPanel<?> parent) { this.parent = parent; }
    @Override public MPGuiPanel<?> getParent()            { return parent; }
    @Override public void setScreen(MPGuiScreen screen)   { this.screen = screen; }
    @Override public MPGuiScreen getScreen()              { return screen; }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        GuiRenderHelper.calculateFlowComponentShape(
                calculatedElementShape, parentDefaultSize, parentContentSize,
                elementShape, scaleRules, available
        );

        GuiPadding pad  = getPadding();
        float      padL = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getLeft());
        float      padT = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getTop());
        float      padR = GuiRenderHelper.calculateFlowComponentX(parentDefaultSize, parentContentSize, pad.getRight());
        float      padB = GuiRenderHelper.calculateFlowComponentY(parentDefaultSize, parentContentSize, pad.getBottom());

        calculatedInnerShape.withShape(calculatedElementShape);
        calculatedInnerShape.grow(padL, padT, -padL - padR, -padT - padB);

        if (textOffset != null) {
            GuiRenderHelper.calculateFlowComponentVector(
                    calculatedTextOffsetTemp, parentDefaultSize, parentContentSize, textOffset
            );
        } else calculatedTextOffsetTemp.withX(0).withY(0);

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
    public void measurePreferred(
            IGuiVector parentDefaultSize, IGuiVector parentContentSize,
            float suggestedX, float suggestedY, MutableGuiVector result
    ) {
        measurePreferredWithScaleRules(parentDefaultSize, parentContentSize, suggestedX, suggestedY, result, elementShape, scaleRules);
    }
}
