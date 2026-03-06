package ru.mousecray.mouseproject.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.misc.MoveDirection;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public interface MPGuiElement<T extends MPGuiElement<T>> {
    T self();
    MutableGuiShape getElementShape();
    MutableGuiShape getCalculatedElementShape();
    void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available);
    GuiScaleRules getScaleRules();
    void setId(int id);
    int getId();
    String getText();
    void setText(String text);
    void setGuiString(MPGuiString guiString);
    MPGuiString getGuiString();
    boolean applyState(@Nullable GuiButtonPersistentState state);
    void setElementShape(IGuiShape elementShape);
    MPGuiTexturePack getTexturePack();
    void setTexturePack(MPGuiTexturePack texturePack);
    @Nullable GuiButtonActionState getActionState();
    @Nullable GuiButtonPersistentState getPersistentState();
    void onUpdate0(Minecraft mc, int mouseX, int mouseY);
    void onMouseEnter0(Minecraft mc, int mouseX, int mouseY);
    void onMouseLeave0(Minecraft mc, int mouseX, int mouseY);
    void onMousePressed0(Minecraft mc, int mouseX, int mouseY);
    void onMouseReleased0(Minecraft mc, int mouseX, int mouseY);
    void onMouseDragged0(Minecraft mc, int mouseX, int mouseY, MoveDirection direction, int diffX, int diffY);
    boolean mouseHover(Minecraft mc, int mouseX, int mouseY);
    void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    void onDrawLast(Minecraft mc, int mouseX, int mouseY, float partialTicks);
    MPGuiElement<?> findTopHovered(Minecraft mc, int mouseX, int mouseY);
    MPGuiScreen getScreen();
    void setScreen(MPGuiScreen screen);
    MPGuiPanel<?> getParent();
    void setParent(MPGuiPanel<?> parent);
    GuiMargin getPadding();
    void setPadding(GuiPadding padding);
    void setTextOffset(IGuiVector offset);
    MutableGuiVector getTextOffset();
    void setScaleRules(GuiScaleRules scaleRules);
    void measurePreferred(IGuiVector parentDefaultSize, IGuiVector parentContentSize, float suggestedX, float suggestedY, MutableGuiVector result);
    void offsetCalculatedShape(float dx, float dy);
}