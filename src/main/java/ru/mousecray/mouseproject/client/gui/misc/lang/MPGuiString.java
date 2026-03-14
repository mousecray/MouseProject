/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.misc.lang;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface MPGuiString {
    String get();

    static MPGuiString simple(String text) {
        return new MPSimpleString(text);
    }

    static MPGuiString localized(String key, Object... args) {
        return new MPLocalizedString(key, args);
    }
}
