/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.item;

import net.minecraft.item.Item;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.registry.MPCreativeTabs;

public class MPDefaultItem extends Item {
    public MPDefaultItem(String key) {
        setCreativeTab(MPCreativeTabs.MAIN_TAB);
        setRegistryName(key);
        setTranslationKey(Tags.MOD_ID + "." + key);
    }
}
