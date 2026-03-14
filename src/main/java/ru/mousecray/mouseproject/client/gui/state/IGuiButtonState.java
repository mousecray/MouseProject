/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.state;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public interface IGuiButtonState {
    @Nonnull IGuiButtonState combine(IGuiButtonState state);
    boolean isPersistent();
    boolean isAction();
}
