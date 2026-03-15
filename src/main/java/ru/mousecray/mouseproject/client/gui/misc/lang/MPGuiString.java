/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.misc.lang;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.Tags;

@SideOnly(Side.CLIENT)
public interface MPGuiString {
    String get();

    static MPGuiString simple(String text) {
        return new MPSimpleString(text);
    }

    static MPGuiString localized(String key, Object... args) {
        return new MPLocalizedString(key, args);
    }

    static MPGuiString localizedGuiTag(String key, Object... args) {
        return localized("gui." + Tags.MOD_ID + "." + key, args);
    }
}
