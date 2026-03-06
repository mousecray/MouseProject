package ru.mousecray.mouseproject.client.gui.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.misc.MPClickType;

@SideOnly(Side.CLIENT)
public class MPGuiMouseClickEvent<T extends MPGuiElement<T>> extends MPGuiMouseEvent<T> {
    private final MPClickType clickType;

    public MPGuiMouseClickEvent(MPClickType clickType) { this.clickType = clickType; }
    public MPClickType getClickType()                  { return clickType; }
}