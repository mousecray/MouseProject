package ru.mousecray.mouseproject.client.gui.misc;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;
import ru.mousecray.mouseproject.client.gui.state.IGuiButtonState;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class StateColorContainer {
    private final Map<IGuiButtonState, Integer> map;
    private final int                           defaultColor;

    private StateColorContainer(int defaultColor, Map<IGuiButtonState, Integer> map) {
        this.defaultColor = defaultColor;
        this.map = map;
    }

    public static StateColorContainer createDefault() {
        return Builder.create(14737632).addState(GuiButtonPersistentState.DISABLED, 10526880).build();
    }

    public int getDefaultColor() { return defaultColor; }

    public int getColor(IGuiButtonState state) {
        Integer i = map.get(state);
        return i == null ? -1 : i;
    }

    public int getCalculatedColor(GuiButtonActionState actionState, GuiButtonPersistentState persistentState, int packedFGColour) {
        int color;
        if (packedFGColour != 0) color = packedFGColour;
        else if (actionState != null) {
            color = getColor(actionState.combine(persistentState));
            if (color == -1) {
                color = getColor(actionState);
                if (color == -1) color = getColor(persistentState);
            }
        } else color = getColor(persistentState);

        if (color == -1) return defaultColor;

        return color;
    }

    public static class Builder {
        private final Map<IGuiButtonState, Integer> map = new HashMap<>();
        private final int                           defaultColor;

        private Builder(int defaultColor)              { this.defaultColor = defaultColor; }

        public static Builder create(int defaultColor) { return new Builder(defaultColor); }

        public Builder addState(IGuiButtonState state, int color) {
            map.put(state, color);
            return this;
        }

        public StateColorContainer build() { return new StateColorContainer(defaultColor, map); }
    }
}