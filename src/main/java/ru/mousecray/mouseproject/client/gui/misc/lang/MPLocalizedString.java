package ru.mousecray.mouseproject.client.gui.misc.lang;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MPLocalizedString implements MPGuiString {
    private final String   key;
    private final Object[] args;

    public MPLocalizedString(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    @Override
    public String get() {
        return I18n.format(key, args);
    }

    @Override
    public String toString() {
        return get();
    }
}
