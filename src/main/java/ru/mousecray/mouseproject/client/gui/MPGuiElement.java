/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.components.GuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.components.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.components.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.components.sound.SoundSourceType;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static ru.mousecray.mouseproject.client.gui.components.GuiRenderHelper.*;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface MPGuiElement<T extends MPGuiElement<T>> {
    T self();

    //Идентификация и иерархия
    void setId(int id);
    int getId();
    @Nullable MPGuiScreen getScreen();
    void setScreen(@Nullable MPGuiScreen screen);
    @Nullable MPGuiPanel<?> getParent();
    void setParent(@Nullable MPGuiPanel<?> parent);

    //Данные и состояние
    default String getText() { return getGuiString().get(); }
    default void setText(String text) { setGuiString(MPGuiString.simple(text)); }
    MPGuiString getGuiString();
    void setGuiString(MPGuiString guiString);
    default boolean isVisible()       { return !getStateManager().has(MPGuiElementState.HIDDEN); }
    default boolean isEnabled()       { return !getStateManager().has(MPGuiElementState.DISABLED); }
    default boolean isHovered()       { return getStateManager().has(MPGuiElementState.HOVERED); }
    default boolean isFocused()       { return getStateManager().has(MPGuiElementState.FOCUSED); }
    default boolean canBeFocused()    { return !getStateManager().isForbidden(MPGuiElementState.FOCUSED); }

    MPGuiElementStateManager getStateManager();

    MPGuiTexturePack getTexturePack();
    void setTexturePack(MPGuiTexturePack texturePack);
    MPGuiSoundPack getSoundPack();
    void setSoundPack(MPGuiSoundPack texturePack);
    MPGuiColorPack getColorPack();
    void setColorPack(MPGuiColorPack colorPack);
    FontRenderer getFontRenderer();
    void setFontRenderer(@Nullable FontRenderer fontRenderer);
    MPFontSize getFontSize();
    void setFontSize(@Nullable MPFontSize fontSize);
    float getTextScaleMultiplayer();
    void setTextScaleMultiplayer(float multiplayer);

    //Геометрия
    default void setShape(IGuiShape shape) { getShape().withShape(shape); }
    MutableGuiShape getShape();
    MutableGuiShape getCalculatedShape();
    MutableGuiShape getCalculatedInnerShape();

    GuiScaleRules getScaleRules();
    void setScaleRules(GuiScaleRules scaleRules);
    GuiPadding getPadding();
    void setPadding(GuiPadding padding);
    MutableGuiVector getTextOffset();
    default void setTextOffset(IGuiVector offset) { getTextOffset().withVector(offset); }

    default void calculate(IGuiVector pDefSize, IGuiVector pContentSize, IGuiShape available) {
        MutableGuiShape calcShape = getCalculatedShape();

        calculateFlowComponentShape(
                calcShape, pDefSize, pContentSize, getShape(), getScaleRules(), available
        );

        if (calcShape.width() <= 0 || calcShape.height() <= 0) return;

        GuiPadding pad  = getPadding();
        float      padL = calculateFlowComponentX(pDefSize, pContentSize, pad.getLeft());
        float      padT = calculateFlowComponentY(pDefSize, pContentSize, pad.getTop());
        float      padR = calculateFlowComponentX(pDefSize, pContentSize, pad.getRight());
        float      padB = calculateFlowComponentY(pDefSize, pContentSize, pad.getBottom());

        MutableGuiShape calcInnerShape = getCalculatedInnerShape();
        calcInnerShape.withShape(calcShape);
        calcInnerShape.grow(-padL, -padT, -padR, -padB);

        calculateTextOffset(pDefSize, pContentSize);

        setupShapeToVanilla(calcShape);

        onCalculated(pDefSize, pContentSize, calcInnerShape);
    }

    default void measurePreferred(IGuiVector pDefSize, IGuiVector pContentSize, float sugX, float sugY, MutableGuiVector result) {
        GuiScaleRules sr = getScaleRules();
        GuiRenderHelper.measurePreferredWithScaleRules(
                pDefSize, pContentSize, sugX, sugY,
                result, getShape(), sr
        );
        GuiRenderHelper.addPaddingToPreferred(pDefSize, pContentSize, result, getPadding(), sr);
    }

    void offsetCalculatedShape(float dx, float dy);
    default void calculateTextOffset(IGuiVector pDefSize, IGuiVector pContentSize)                    { }
    default void setupShapeToVanilla(IGuiShape result)                                                { }
    default void onCalculated(IGuiVector pDefSize, IGuiVector pContentSize, IGuiShape innerCalcShape) { }

    //Диспетчеризация событий
    void dispatchUpdate(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void dispatchProcessHover(Minecraft mc, int mouseX, int mouseY);
    void dispatchMouseEnter(Minecraft mc, int mouseX, int mouseY);
    void dispatchMouseLeave(Minecraft mc, int mouseX, int mouseY);

    boolean dispatchMousePressed(Minecraft mc, int mouseX, int mouseY, int mouseButton);
    void dispatchMouseReleased(Minecraft mc, int mouseX, int mouseY, int state);
    boolean dispatchMouseDragged(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY);
    boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll);
    boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode);
    void dispatchPlaySound(Minecraft mc, SoundHandler soundHandler, SoundSourceType source);

    //Рендеринг
    default void dispatchDraw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (isVisible()) {
            dispatchDrawBackground(mc, mouseX, mouseY, partialTicks);
            dispatchDrawForeground(mc, mouseX, mouseY, partialTicks);
            dispatchDrawText(mc, mouseX, mouseY, partialTicks);
            dispatchDrawLast(mc, mouseX, mouseY, partialTicks);
        }
    }
    void dispatchDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void dispatchDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void dispatchDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void dispatchDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks);

    //Интеграция с vanilla
    default int getHoverState(boolean mouseOver) { return !isEnabled() ? 0 : mouseOver ? 2 : 1; }
    default boolean mouseHover(Minecraft mc, int mouseX, int mouseY)   { return getCalculatedShape().contains(mouseX, mouseY); }
    default boolean mousePressed(Minecraft mc, int mouseX, int mouseY) { return isEnabled() && isVisible() && getCalculatedShape().contains(mouseX, mouseY); }
    default void mouseReleased(int mouseX, int mouseY)                 { dispatchMouseReleased(Minecraft.getMinecraft(), mouseX, mouseY, 0); }
    default void playPressSound(SoundHandler soundHandler)             { dispatchPlaySound(Minecraft.getMinecraft(), Minecraft.getMinecraft().getSoundHandler(), SoundSourceType.PRESS); }
    default boolean isMouseOver()                                      { return getStateManager().has(MPGuiElementState.HOVERED); }

    default void performClickFromVanilla() {
        if (!isEnabled() || !isVisible()) return;

        MutableGuiShape calcShape = getCalculatedShape();
        int             centerX   = (int) (calcShape.x() + calcShape.width() / 2f);
        int             centerY   = (int) (calcShape.y() + calcShape.height() / 2f);

        Minecraft mc = Minecraft.getMinecraft();
        dispatchMousePressed(mc, centerX, centerY, 0);
        dispatchMouseReleased(mc, centerX, centerY, 0);
    }
}