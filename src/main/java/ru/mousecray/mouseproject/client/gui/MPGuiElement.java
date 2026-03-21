/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;
import ru.mousecray.mouseproject.client.gui.misc.SoundSourceType;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

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
    String getText();
    void setText(@Nullable String text);
    MPGuiString getGuiString();
    void setGuiString(MPGuiString guiString);

    MPGuiElementStateManager getStateManager();

    MPGuiTexturePack getTexturePack();
    void setTexturePack(MPGuiTexturePack texturePack);

    //Геометрия
    void setShape(IGuiShape shape);
    MutableGuiShape getShape();
    MutableGuiShape getCalculatedShape();
    MutableGuiShape getCalculatedInnerShape();

    GuiScaleRules getScaleRules();
    void setScaleRules(GuiScaleRules scaleRules);
    GuiMargin getPadding();
    void setPadding(GuiPadding padding);
    MutableGuiVector getTextOffset();
    void setTextOffset(IGuiVector offset);

    void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available);
    void measurePreferred(
            IGuiVector parentDefaultSize, IGuiVector parentContentSize,
            float suggestedX, float suggestedY, MutableGuiVector result
    );
    void offsetCalculatedShape(float dx, float dy);

    //Диспетчеризация событий
    void dispatchUpdate(Minecraft mc, int mouseX, int mouseY);
    void dispatchProcessHover(Minecraft mc, int mouseX, int mouseY);
    void dispatchMouseEnter(Minecraft mc, int mouseX, int mouseY);
    void dispatchMouseLeave(Minecraft mc, int mouseX, int mouseY);

    boolean dispatchMousePressed(Minecraft mc, int mouseX, int mouseY, int mouseButton);
    void dispatchMouseReleased(Minecraft mc, int mouseX, int mouseY, int state);
    boolean dispatchMouseDragged(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY);
    boolean dispatchMouseScrolled(Minecraft mc, int mouseX, int mouseY, int scroll);
    boolean dispatchKeyTyped(Minecraft mc, int mouseX, int mouseY, char typedChar, int keyCode);
    void dispatchPlaySound(Minecraft mc, SoundHandler soundHandler, @Nullable SoundEvent sound, SoundSourceType source);

    //Рендеринг
    void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks);

    //Интеграция с vanilla
    int getHoverState(boolean mouseOver);
    boolean mouseHover(Minecraft mc, int mouseX, int mouseY);
    boolean mousePressed(Minecraft mc, int mouseX, int mouseY);
    boolean mouseReleased(int mouseX, int mouseY);
    void performClickFromVanilla();
}