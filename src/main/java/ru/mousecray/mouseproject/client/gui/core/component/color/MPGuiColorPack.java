/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.component.color;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.component.state.MPGuiElementStateManager;

@SideOnly(Side.CLIENT)
public class MPGuiColorPack {
    public static MPGuiColorPack EMPTY() {
        return Builder.create(0).build();
    }

    public static MPGuiColorPack LABEL_SIMPLE() {
        return Builder.create(14737632).addColor(7368816, MPGuiElementState.DISABLED).build();
    }

    public static MPGuiColorPack CONTROL_SIMPLE() {
        return Builder.create(14737632).addColor(10526880, MPGuiElementState.DISABLED).build();
    }

    public static MPGuiColorPack TEXT_FIELD_SIMPLE() {
        return Builder.create(14737632).addColor(7368816, MPGuiElementState.DISABLED).build();
    }

    public static MPGuiColorPack TEXT_FIELD_PLACEHOLDER() {
        return Builder.create(0x686868).addColor(7368816, MPGuiElementState.DISABLED).build();
    }

    public static MPGuiColorPack TEXT_FIELD_CURSOR() {
        return Builder.create(-3092272).addColor(7368816, MPGuiElementState.DISABLED).build();
    }

    public static MPGuiColorPack TEXT_FIELD_SELECTION() {
        return Builder.create(1275068671).addColor(1280337116, MPGuiElementState.DISABLED).build();
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

    public int getCalculatedColor(MPGuiElementStateManager stateManager) { return getCalculatedColor(stateManager, 0); }

    public int getCalculatedColor(MPGuiElementStateManager stateManager, int packedFGColour) {
        if (packedFGColour != 0) return packedFGColour;
        int color    = defaultColor;
        int maxBits  = -1;
        int bestMask = -1;
        for (Int2IntMap.Entry e : colors.int2IntEntrySet()) {
            int mask = e.getIntKey();
            if (stateManager.satisfies(mask)) {
                int bits = Integer.bitCount(mask);
                if (bits > maxBits || (bits == maxBits && mask > bestMask)) {
                    maxBits = bits;
                    bestMask = mask;
                    color = e.getIntValue();
                }
            }
        }
        return color;
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

    public static int colorToInt(float red, float green, float blue, float alpha) {
        int r = (int) red & 0xFF;
        int g = (int) green & 0xFF;
        int b = (int) blue & 0xFF;
        int a = (int) alpha & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static float[] intToColor(int color) {
        if ((color & 0xFF000000) == 0) color |= 0xFF000000;

        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = (color >> 24 & 0xFF) / 255.0F;

        return new float[]{ r, g, b, a };
    }
}