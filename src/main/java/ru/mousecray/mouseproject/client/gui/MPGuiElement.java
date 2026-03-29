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
import ru.mousecray.mouseproject.client.gui.components.color.MPGuiColorPack;
import ru.mousecray.mouseproject.client.gui.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.components.sound.MPGuiSoundPack;
import ru.mousecray.mouseproject.client.gui.components.sound.SoundSourceType;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;

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
    boolean isVisible();
    boolean isEnabled();
    boolean isHovered();
    boolean isFocused();
    boolean canBeFocused();

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
    void setFontSize(MPFontSize fontSize);
    float getTextScaleMultiplayer();
    void setTextScaleMultiplayer(float multiplayer);

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
    int getHoverState(boolean mouseOver);
    boolean mouseHover(Minecraft mc, int mouseX, int mouseY);
    boolean mousePressed(Minecraft mc, int mouseX, int mouseY);
    void mouseReleased(int mouseX, int mouseY);
    void performClickFromVanilla();
    void playPressSound(SoundHandler soundHandler);
    boolean isMouseOver();
}