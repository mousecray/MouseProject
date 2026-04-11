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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import ru.mousecray.mouseproject.client.gui.core.control.MPGuiScrollbar;
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
    private       MPGuiScaleRules   scaleRules      = new MPGuiScaleRules(MPGuiScaleType.FLOW);

    @Nullable private MPGuiPanel<?>  content;
    private           MPGuiScrollbar scrollbar;
    private           MPOrientation  orientation = MPOrientation.VERTICAL;

    private float   scrollValue        = 0;
    private float   contentSize        = 0;
    private boolean scrollEnabled      = true;
    private float   scrollbarThickness = 8f;

    private MPGuiScreen   screen;
    private MPGuiPanel<?> parent;
    private int           id;
    private int           tickDown = -1;

    private final MPGuiElementStateManager stateManager = new MPGuiElementStateManager();
    protected     MPGuiTexturePack         texturePack  = MPGuiTexturePack.EMPTY();
    protected     MPGuiColorPack           colorPack    = MPGuiColorPack.EMPTY();
    protected     MPGuiSoundPack           soundPack    = MPGuiSoundPack.EMPTY();

    public MPGuiScrollPanel(MPGuiShape shape) {
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

        MPGuiShape sbShape = new MPGuiShape(0, 0, scrollbarThickness, 100);
        scrollbar = new MPGuiScrollbar(sbShape);

        scrollbar.setOnScroll(val -> {
            float diff = val - scrollValue;
            scrollValue = val;
            if (content != null) {
                if (orientation == MPOrientation.VERTICAL) content.offsetCalculatedShape(0, -diff);
                else content.offsetCalculatedShape(-diff, 0);
            }
        });
    }

    public MPOrientation getOrientation() { return orientation; }

    public void setOrientation(MPOrientation orientation) {
        this.orientation = orientation;
        if (scrollbar != null) {
            scrollbar.setOrientation(orientation);
            if (orientation == MPOrientation.VERTICAL) scrollbar.getShape().withWidth(scrollbarThickness);
            else scrollbar.getShape().withHeight(scrollbarThickness);
        }
    }

    public void setScrollbarThickness(float thickness) {
        scrollbarThickness = thickness;
        if (scrollbar != null) {
            if (orientation == MPOrientation.VERTICAL) scrollbar.getShape().withWidth(thickness);
            else scrollbar.getShape().withHeight(thickness);
        }
    }

    public MPGuiScrollbar getScrollbar()                { return scrollbar; }
    public void setScrollEnabled(boolean scrollEnabled) { this.scrollEnabled = scrollEnabled; }

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
        stateManager.lockForbidden(screen != null || getParent() != null);
        if (content != null) content.setScreen(screen);
        if (scrollbar != null) scrollbar.setScreen(screen);
    }

    @Override @Nullable public MPGuiPanel<?> getParent() { return parent; }

    @Override
    public void setParent(@Nullable MPGuiPanel<?> parent) {
        this.parent = parent;
        stateManager.lockForbidden(parent != null || getScreen() != null);
        if (parent != null && content != null) content.setParent(parent);
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
        MouseProject.LOGGER.warn("MPGuiScrollPanel cannot support custom FontRenderer");
    }

    @Override public MPFontSize getFontSize() { return MPFontSize.NORMAL; }

    @Override
    public void setFontSize(MPFontSize size) {
        MouseProject.LOGGER.warn("MPGuiScrollPanel cannot support custom FontSize");
    }

    @Override public float getTextScaleMultiplayer() { return 0.0f; }

    @Override
    public void setTextScaleMultiplayer(float multiplayer) {
        MouseProject.LOGGER.warn("MPGuiScrollPanel cannot support custom TextScaleMultiplayer");
    }

    //Геометрия
    @Override public MPMutableGuiShape getShape() { return shape; }
    @Override public MPMutableGuiShape getCalculatedShape()         { return calculatedShape; }
    @Override public MPMutableGuiShape getCalculatedInnerShape()    { return calculatedShape; }
    @Override public MPGuiScaleRules getScaleRules()                { return scaleRules; }
    @Override public void setScaleRules(MPGuiScaleRules scaleRules) { this.scaleRules = scaleRules; }
    @Override public MPGuiPadding getPadding()                      { return MPGuiPadding.ZERO(); }

    @Override
    public void setPadding(MPGuiPadding padding) {
        MouseProject.LOGGER.warn("MPGuiScrollPanel cannot support padding");
    }

    @Override public MPMutableGuiVector getTextOffset() { return new MPMutableGuiVector(); }

    @Override
    public void calculate(IGuiVector pDefSize, IGuiVector pContentSize, IGuiShape available) {
        MPGuiRenderHelper.calculateFlowComponentShape(
                calculatedShape, pDefSize, pContentSize,
                shape, scaleRules, available
        );

        if (content != null) {
            MPMutableGuiShape contentAvail = calculatedShape.copy().toMutable();
            boolean           isVert       = orientation == MPOrientation.VERTICAL;

            float sbThickness = scrollbar != null ? (isVert ? scrollbar.getShape().width() : scrollbar.getShape().height()) : 0f;

            if (isVert) {
                contentAvail.withHeight(99999f);
                if (scrollEnabled && scrollbar != null) contentAvail.withWidth(contentAvail.width() - sbThickness);
            } else {
                contentAvail.withWidth(99999f);
                if (scrollEnabled && scrollbar != null) contentAvail.withHeight(contentAvail.height() - sbThickness);
            }

            content.calculate(pDefSize, pContentSize, contentAvail);
            contentSize = isVert ? calculateTrueContentHeight() : calculateTrueContentWidth();

            float viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();
            float maxScroll    = Math.max(0, contentSize - viewportSize);

            if (scrollValue > maxScroll) scrollValue = maxScroll;
            if (scrollValue < 0) scrollValue = 0;

            if (isVert) content.offsetCalculatedShape(0, -scrollValue);
            else content.offsetCalculatedShape(-scrollValue, 0);

            if (scrollEnabled && scrollbar != null) {
                scrollbar.updateSizes(viewportSize, contentSize);
                scrollbar.setScrollValue(scrollValue, false);

                if (isVert) {
                    scrollbar.getShape().withX(shape.width() - sbThickness).withY(0).withHeight(shape.height());
                } else {
                    scrollbar.getShape().withX(0).withY(shape.height() - sbThickness).withWidth(shape.width());
                }
                scrollbar.calculate(pDefSize, pContentSize, calculatedShape);
            }
        }
    }

    @Override
    public void offsetCalculatedShape(float dx, float dy) {
        calculatedShape.offset(dx, dy);
        if (content != null) content.offsetCalculatedShape(dx, dy);
        if (scrollbar != null) scrollbar.offsetCalculatedShape(dx, dy);
    }

    public void applyScroll(float amount) {
        if (content == null) return;

        float oldScroll = scrollValue;
        scrollValue += amount;

        boolean isVert       = orientation == MPOrientation.VERTICAL;
        float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();
        float   maxScroll    = Math.max(0, contentSize - viewportSize);

        if (scrollValue < 0) scrollValue = 0;
        if (scrollValue > maxScroll) scrollValue = maxScroll;

        float diff = scrollValue - oldScroll;
        if (diff != 0) {
            if (isVert) content.offsetCalculatedShape(0, -diff);
            else content.offsetCalculatedShape(-diff, 0);

            if (scrollbar != null) scrollbar.setScrollValue(scrollValue, false);
        }
    }

    private float calculateTrueContentHeight() {
        if (content == null) return 0;
        return Math.max(0, findMaxBottom(content) - calculatedShape.y());
    }

    private float calculateTrueContentWidth() {
        if (content == null) return 0;
        return Math.max(0, findMaxRight(content) - calculatedShape.x());
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

    private float findMaxRight(MPGuiElement<?> element) {
        float max = element.getCalculatedShape().x() + element.getCalculatedShape().width();
        if (element instanceof MPGuiPanel) {
            for (MPGuiElement<?> child : ((MPGuiPanel<?>) element).getChildren()) {
                max = Math.max(max, findMaxRight(child));
            }
        }
        return max;
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

        boolean isVert       = orientation == MPOrientation.VERTICAL;
        float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

        if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
            scrollbar.dispatchUpdate(mc, mouseX, mouseY, partialTicks);
        }
        if (content != null) content.dispatchUpdate(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public final void dispatchProcessHover(Minecraft mc, int mouseX, int mouseY) {
        if (!isVisible()) return;

        boolean isVert       = orientation == MPOrientation.VERTICAL;
        float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

        if (scrollEnabled && scrollbar != null && contentSize > viewportSize) scrollbar.dispatchProcessHover(mc, mouseX, mouseY);
        if (content != null) content.dispatchProcessHover(mc, mouseX, mouseY);

        boolean isHovered = calculatedShape.contains(mouseX, mouseY);
        if (isHovered && !stateManager.has(MPGuiElementState.HOVERED)) dispatchMouseEnter(mc, mouseX, mouseY);
        else if (!isHovered && stateManager.has(MPGuiElementState.HOVERED)) dispatchMouseLeave(mc, mouseX, mouseY);
    }

    @Override
    public void dispatchMouseEnter(Minecraft mc, int mouseX, int mouseY) {
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

        boolean isVert       = orientation == MPOrientation.VERTICAL;
        float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

        if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
            if (scrollbar.dispatchMousePressed(mc, mouseX, mouseY, mouseButton)) return true;
        }

        if (content != null && content.mouseHover(mc, mouseX, mouseY)) {
            if (content.dispatchMousePressed(mc, mouseX, mouseY, mouseButton)) return true;
        }

        if (!isEnabled() || !isVisible()) {
            dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.DISABLED);
            return false;
        }

        if (stateManager.has(MPGuiElementState.FAIL)) dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.FAIL);

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
        stateManager.remove(MPGuiElementState.PRESSED);

        if (scrollEnabled && scrollbar != null) scrollbar.dispatchMouseReleased(mc, mouseX, mouseY, state);
        if (content != null) content.dispatchMouseReleased(mc, mouseX, mouseY, state);

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
    public final boolean dispatchMouseDragged(Minecraft mc, int mouseX, int mouseY, MPMoveDirection direction, int diffX, int diffY) {
        boolean handled      = false;
        boolean isVert       = orientation == MPOrientation.VERTICAL;
        float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

        if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
            if (scrollbar.dispatchMouseDragged(mc, mouseX, mouseY, direction, diffX, diffY)) handled = true;
        }
        if (!handled && content != null) {
            handled = content.dispatchMouseDragged(mc, mouseX, mouseY, direction, diffX, diffY);
        }

        if (!handled && tickDown >= 0) {
            MPGuiEventFactory.pushMouseDragEvent(dragEvent, mouseX, mouseY, direction, diffX, diffY, tickDown);
            onAnyEventFire(dragEvent);
            if (!dragEvent.isCancelled()) {
                dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.DRAG);
                onMouseDragged(dragEvent);
            }
            return !dragEvent.isCancelled();
        }
        return handled;
    }

    @Override
    public final boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll) {
        if (!calculatedShape.contains(mouseX, mouseY)) return false;

        if (content != null) {
            if (content.dispatchMouseScrolled(mc, mouseX, mouseY, scroll)) return true;
        }

        if (scrollEnabled) {
            float oldScroll = scrollValue;
            applyScroll(-scroll / 10f);

            MPGuiEventFactory.pushMouseScrollEvent(scrollEvent, mouseX, mouseY, MPScrollDirection.getScrollDirection(scroll), scroll);
            onAnyEventFire(scrollEvent);
            if (!scrollEvent.isCancelled()) {
                dispatchPlaySound(mc, mc.getSoundHandler(), MPSoundSourceType.SCROLL);
                onMouseScrolled(scrollEvent);
            }
            return Float.compare(oldScroll, scrollValue) != 0 || scrollEvent.isConsumed();
        }
        return false;
    }

    @Override
    public final boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode) {
        if (!isVisible()) return false;

        if (content != null && content.dispatchKeyTyped(mc, mouseX, mouseY, typedChar, keyCode)) return true;

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
            MPGuiEventFactory.pushSoundEvent(soundEvent, moveEvent.getMouseX(), moveEvent.getMouseY(), soundHandler, sound, source);
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

            if (content != null) {
                setupScissor(mc);
                content.dispatchDrawBackground(mc, mouseX, mouseY, partialTicks);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            boolean isVert       = orientation == MPOrientation.VERTICAL;
            float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

            if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
                scrollbar.dispatchDrawBackground(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public final void dispatchDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawFGEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawFGEvent);
        if (!drawFGEvent.isCancelled()) {
            onDrawForeground(drawFGEvent);

            if (content != null) {
                setupScissor(mc);
                content.dispatchDrawForeground(mc, mouseX, mouseY, partialTicks);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            boolean isVert       = orientation == MPOrientation.VERTICAL;
            float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

            if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
                scrollbar.dispatchDrawForeground(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public final void dispatchDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawTextEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawTextEvent);
        if (!drawTextEvent.isCancelled()) {
            onDrawText(drawTextEvent);

            if (content != null) {
                setupScissor(mc);
                content.dispatchDrawText(mc, mouseX, mouseY, partialTicks);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            boolean isVert       = orientation == MPOrientation.VERTICAL;
            float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

            if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
                scrollbar.dispatchDrawText(mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public final void dispatchDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        MPGuiEventFactory.pushTickEvent(drawLastEvent, mouseX, mouseY, partialTicks);
        onAnyEventFire(drawLastEvent);
        if (!drawLastEvent.isCancelled()) {
            onDrawLast(drawLastEvent);

            if (content != null) {
                setupScissor(mc);
                content.dispatchDrawLast(mc, mouseX, mouseY, partialTicks);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            boolean isVert       = orientation == MPOrientation.VERTICAL;
            float   viewportSize = isVert ? calculatedShape.height() : calculatedShape.width();

            if (scrollEnabled && scrollbar != null && contentSize > viewportSize) {
                scrollbar.dispatchDrawLast(mc, mouseX, mouseY, partialTicks);
            }
        }
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

    protected void onPlaySound(MPGuiSoundEvent<T> event) {
        event.getHandler().playSound(PositionedSoundRecord.getMasterRecord(event.getSound(), 1.0F));
    }

    protected void onAnyEventFire(MPGuiEvent<T> event) { }
    public void onClick(MPGuiMouseClickEvent<T> event) { }

    //Интеграция с vanilla
    @Override
    public boolean mouseHover(Minecraft mc, int mouseX, int mouseY) {
        return calculatedShape.contains(mouseX, mouseY);
    }
}