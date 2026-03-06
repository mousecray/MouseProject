package ru.mousecray.mouseproject.client.gui.misc.lang;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MPSimpleString implements MPGuiString {
    private final String text;

    public MPSimpleString(String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    public String get() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
