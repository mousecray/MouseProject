/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.components.state;

public class MPGuiElementStateManager {
    private int
            states    = 0,
            forbidden = 0;
    private boolean  forbiddenLocked = false;
    private Runnable changeListener;

    private static final int INTERACTIVE_MASK =
            MPGuiElementState.HOVERED.mask |
                    MPGuiElementState.PRESSED.mask |
                    MPGuiElementState.FOCUSED.mask;

    public void setChangeListener(Runnable listener) { changeListener = listener; }
    private void notifyChange()                      { if (changeListener != null) changeListener.run(); }

    public void add(MPGuiElementState state) {
        if ((forbidden & state.mask) != 0) return;

        if (has(MPGuiElementState.DISABLED) || has(MPGuiElementState.HIDDEN)) {
            if ((INTERACTIVE_MASK & state.mask) != 0) return;
        }

        int oldStates = states;

        if (state == MPGuiElementState.DISABLED || state == MPGuiElementState.HIDDEN) states &= ~INTERACTIVE_MASK;

        states |= state.mask;

        if (oldStates != states) notifyChange();
    }

    public void remove(MPGuiElementState state) {
        int oldStates = states;
        states &= ~state.mask;
        if (oldStates != states) notifyChange();
    }

    public boolean has(MPGuiElementState state) { return (states & state.mask) != 0; }

    public void setForbidden(MPGuiElementState state, boolean isForbidden) {
        if (forbiddenLocked) {
            throw new IllegalStateException(
                    "Forbidden states cannot be modified after " +
                            "the element has been added to the GUI tree (setParent/setScreen)!"
            );
        }
        if (isForbidden) {
            forbidden |= state.mask;
            remove(state);
        } else forbidden &= ~state.mask;
    }

    public boolean isForbidden(MPGuiElementState state) { return (forbidden & state.mask) != 0; }
    public boolean satisfies(int requiredMask)          { return (states & requiredMask) == requiredMask; }

    public static int createMask(MPGuiElementState... statesToCombine) {
        int mask = 0;
        for (MPGuiElementState s : statesToCombine) mask |= s.mask;
        return mask;
    }

    public void lockForbidden(boolean lock) { forbiddenLocked = lock; }
}