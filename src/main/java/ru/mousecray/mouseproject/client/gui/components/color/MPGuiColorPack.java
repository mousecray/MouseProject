/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.components.color;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.components.state.MPGuiElementStateManager;

@SideOnly(Side.CLIENT)
public class MPGuiColorPack {
    public static MPGuiColorPack EMPTY() {
        return Builder.create(0).build();
    }

    public static MPGuiColorPack CONTROL_SIMPLE() {
        return Builder.create(14737632).addColor(10526880, MPGuiElementState.DISABLED).build();
    }

    public static MPGuiColorPack TEXT_FIELD_SIMPLE() {
        return Builder.create(14737632).addColor(7368816, MPGuiElementState.DISABLED).build();
    }

    private final Int2IntMap colors;
    private final int        defaultColor;

    private MPGuiColorPack(int defaultColor, Int2IntMap colors) {
        this.defaultColor = defaultColor;
        this.colors = colors;
    }

    public int getDefaultColor() { return defaultColor; }

    public int getColor(MPGuiElementState... states) {
        for (Int2IntMap.Entry e : colors.int2IntEntrySet()) {
            if (e.getIntKey() == MPGuiElementStateManager.createMask(states)) return e.getIntValue();
        }
        return -1;
    }

    public int getCalculatedColor(MPGuiElementStateManager stateManager, int packedFGColour) {
        int color = -1;
        if (packedFGColour != 0) color = packedFGColour;
        for (Int2IntMap.Entry e : colors.int2IntEntrySet()) {
            if (stateManager.satisfies(e.getIntKey())) color = e.getIntValue();
        }

        return color == -1 ? defaultColor : color;
    }

    public static class Builder {
        private final Int2IntMap colors = new Int2IntArrayMap();
        private final int        defaultColor;

        private Builder(int defaultColor)              { this.defaultColor = defaultColor; }

        public static Builder create(int defaultColor) { return new Builder(defaultColor); }

        public Builder addColor(int color, MPGuiElementState... states) {
            int mask = MPGuiElementStateManager.createMask(states);
            colors.put(mask, color);
            return this;
        }

        public MPGuiColorPack build() {
            colors.defaultReturnValue(-1);
            return new MPGuiColorPack(defaultColor, colors);
        }
    }
}