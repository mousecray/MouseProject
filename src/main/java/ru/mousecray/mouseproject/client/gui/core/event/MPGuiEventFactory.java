/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.event;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.core.component.sound.MPSoundSourceType;
import ru.mousecray.mouseproject.client.gui.core.misc.MPMoveDirection;
import ru.mousecray.mouseproject.client.gui.core.misc.MPScrollDirection;

@SideOnly(Side.CLIENT)
public class MPGuiEventFactory {
    public static <T extends MPGuiElement<T>> void pushMouseClickEvent(MPGuiMouseClickEvent<T> event, int x, int y) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
    }

    public static <T extends MPGuiElement<T>> void pushMouseMoveEvent(MPGuiMouseMoveEvent<T> event, int x, int y, MPMoveDirection moveDirection) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setMoveDirection(moveDirection);
    }

    public static <T extends MPGuiElement<T>> void pushMouseScrollEvent(MPGuiMouseScrollEvent<T> event, int x, int y, MPScrollDirection scrollDirection, int scrollAmount) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setScrollDirection(scrollDirection);
        event.setScrollAmount(Math.abs(scrollAmount));
    }

    public static <T extends MPGuiElement<T>> void pushMouseDragEvent(MPGuiMouseDragEvent<T> event, int x, int y, MPMoveDirection moveDirection, int diffX, int diffY, int tickDown) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setMoveDirection(moveDirection);
        event.setDiffX(diffX);
        event.setDiffY(diffY);
        event.setTickDown(tickDown);
    }

    public static <T extends MPGuiElement<T>> void pushTickEvent(MPGuiTickEvent<T> event, int x, int y, float partialTick) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setPartialTick(partialTick);
    }

    public static <T extends MPGuiElement<T>> void pushTextTypedEvent(MPGuiTextTypedEvent<T> event, int x, int y, int cursorPos, int selectionEnd, String oldText, String newText) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setCursorPos(cursorPos);
        event.setSelectionEnd(selectionEnd);
        event.setOldText(oldText);
        event.setNewText(newText);
    }

    public static <T extends MPGuiElement<T>> void pushKeyEvent(MPGuiKeyEvent<T> event, int x, int y, char typedChar, int keyCode) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setTypedChar(typedChar);
        event.setKeyCode(keyCode);
    }


    public static <T extends MPGuiElement<T>> void pushSoundEvent(MPGuiSoundEvent<T> event, int x, int y, SoundHandler handler, SoundEvent sound, MPSoundSourceType source) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setSound(sound);
        event.setSource(source);
        event.setHandler(handler);
    }

    public static <T extends MPGuiElement<T>> void pushSliderChangedEvent(MPGuiSliderChangedEvent<T> event, int x, int y, int oldValue, int newValue) {
        event.reset();
        event.setMouseX(x);
        event.setMouseY(y);
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
    }
}