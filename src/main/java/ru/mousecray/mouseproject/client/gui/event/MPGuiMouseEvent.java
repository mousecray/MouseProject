package ru.mousecray.mouseproject.client.gui.event;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;

@SideOnly(Side.CLIENT)
public abstract class MPGuiMouseEvent<T extends MPGuiElement<T>> extends MPGuiEvent<T> { }
