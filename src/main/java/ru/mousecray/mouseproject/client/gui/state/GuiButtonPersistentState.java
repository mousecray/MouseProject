package ru.mousecray.mouseproject.client.gui.state;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public enum GuiButtonPersistentState implements IGuiButtonState {
    NORMAL,
    DISABLED,
    SELECTED,
    FAIL,
    ;

    @Nonnull @Override
    public IGuiButtonState combine(IGuiButtonState state) {
        if (state instanceof GuiButtonCombinedState) throw new UnsupportedOperationException();
        return GuiButtonCombinedState.create(this, state);
    }

    @Override public boolean isPersistent() { return true; }
    @Override public boolean isAction()     { return false; }
}