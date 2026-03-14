/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.gui.state;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiButtonCombinedState implements IGuiButtonState {
    private final IGuiButtonState state1;
    private final IGuiButtonState state2;

    private GuiButtonCombinedState(IGuiButtonState state1, IGuiButtonState state2) {
        this.state1 = state1;
        this.state2 = state2;
    }

    public static GuiButtonCombinedState create(IGuiButtonState state1, IGuiButtonState state2) {
        return new GuiButtonCombinedState(state1, state2);
    }

    @Nonnull @Override
    public IGuiButtonState combine(IGuiButtonState state) {
        throw new UnsupportedOperationException();
    }

    public IGuiButtonState getState1()      { return state1; }
    public IGuiButtonState getState2()      { return state2; }

    @Override public boolean isPersistent() { return state1.isPersistent() && state2.isPersistent(); }
    @Override public boolean isAction()     { return state1.isAction() && state2.isAction(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuiButtonCombinedState)) return false;
        GuiButtonCombinedState that = (GuiButtonCombinedState) o;
        return (state1 == that.state1 && state2 == that.state2) ||
                (state1 == that.state2 && state2 == that.state1);
    }

    @Override public int hashCode() { return state1.hashCode() + state2.hashCode(); }
}