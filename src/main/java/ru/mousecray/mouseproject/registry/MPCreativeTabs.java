/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.registry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import ru.mousecray.mouseproject.Tags;

import javax.annotation.Nonnull;

public class MPCreativeTabs extends CreativeTabs {
    public static final MPCreativeTabs MAIN_TAB = new MPCreativeTabs();
    public MPCreativeTabs()                          { super(Tags.MOD_ID); }
    @Nonnull @Override public ItemStack createIcon() { return new ItemStack(MPItems.RUBY_COIN); }
}
