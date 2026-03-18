package ru.mousecray.mouseproject.client.gui.misc.state;

import javax.annotation.Nonnull;

public class MPGuiElementStateManager {
    private int states    = 0;
    private int forbidden = 0;

    private static final int INTERACTIVE_MASK =
            MPGuiElementState.HOVERED.mask | MPGuiElementState.PRESSED.mask | MPGuiElementState.FOCUSED.mask;

    public void add(MPGuiElementState state) {
        int bit = state.mask;

        if ((forbidden & bit) != 0) return;
        if (has(MPGuiElementState.DISABLED) && (INTERACTIVE_MASK & bit) != 0) return;
        if (state == MPGuiElementState.DISABLED) states &= ~INTERACTIVE_MASK;

        states |= bit;
    }

    public void remove(MPGuiElementState state) { states &= ~state.mask; }
    public boolean has(MPGuiElementState state) { return (states & state.mask) != 0; }

    public void setForbidden(MPGuiElementState state, boolean isForbidden) {
        int bit = state.mask;
        if (isForbidden) {
            forbidden |= bit;
            states &= ~bit;
        } else forbidden &= ~bit;
    }

    public boolean isForbidden(MPGuiElementState state) {
        return (forbidden & state.mask) != 0;
    }

    public boolean satisfies(int requiredMask) { return (states & requiredMask) == requiredMask; }

    public static int createMask(@Nonnull MPGuiElementState... statesToCombine) {
        int mask = 0;
        for (MPGuiElementState s : statesToCombine) mask |= s.mask;
        return mask;
    }

    public static int createMask(@Nonnull MPGuiElementState state, MPGuiElementState... statesToCombine) {
        int mask = state.mask;
        for (MPGuiElementState s : statesToCombine) mask |= s.mask;
        return mask;
    }
}