/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.components.texture;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementStateManager;
import ru.mousecray.mouseproject.client.gui.core.dim.IGuiVector;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MPGuiTexturePack {
    public static MPGuiTexturePack EMPTY() { return new MPGuiTexturePack(new Int2ObjectArrayMap<>()); }

    private final Int2ObjectMap<MPGuiTexture> textures;

    private MPGuiTexturePack(Int2ObjectMap<MPGuiTexture> textures) { this.textures = textures; }

    public MPGuiTexture getCalculatedTexture(MPGuiElementStateManager stateManager) {
        MPGuiTexture bestTexture = null;
        int          maxBits     = -1;

        for (Int2ObjectMap.Entry<MPGuiTexture> e : textures.int2ObjectEntrySet()) {
            int mask = e.getIntKey();
            if (stateManager.satisfies(mask)) {
                int bits = Integer.bitCount(mask);
                if (bits > maxBits) {
                    maxBits = bits;
                    bestTexture = e.getValue();
                }
            }
        }
        return bestTexture;
    }

    @Nullable
    public MPGuiTexture getTexture(MPGuiElementState... states) {
        for (Int2ObjectMap.Entry<MPGuiTexture> e : textures.int2ObjectEntrySet()) {
            if (e.getIntKey() == MPGuiElementStateManager.createMask(states)) return e.getValue();
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static class Builder {
        private final Int2ObjectMap<MPGuiTexture> textures = new Int2ObjectArrayMap<>();
        private final ResourceLocation            baseTexture;
        private final IGuiVector                  textureSize;
        private final IGuiVector                  startPos;
        private final IGuiVector                  elementSize;

        private MPGuiTextureScaleRules scaleRules = new MPGuiTextureScaleRules(MPGuiTextureScaleType.STRETCH);

        private Builder(ResourceLocation baseTexture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
            this.baseTexture = baseTexture;
            this.textureSize = textureSize;
            this.startPos = startPos;
            this.elementSize = elementSize;
        }

        public static Builder create(ResourceLocation baseTexture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
            return new Builder(baseTexture, textureSize, startPos, elementSize);
        }

        public Builder setScaleRules(MPGuiTextureScaleRules scaleRules) {
            this.scaleRules = scaleRules;
            return this;
        }

        public Builder addTexture(int index, float opacity, MPGuiElementState... states) {
            int mask = MPGuiElementStateManager.createMask(states);
            textures.put(mask, new MPGuiTexture(
                    baseTexture, textureSize,
                    MPGuiVector.of(startPos.x(), startPos.y() + elementSize.y() * index),
                    elementSize,
                    scaleRules,
                    Math.max(0.0f, Math.min(1.0f, opacity))
            ));
            return this;
        }

        public Builder addTexture(int index, MPGuiElementState... states) {
            int mask = MPGuiElementStateManager.createMask(states);
            textures.put(mask, new MPGuiTexture(
                    baseTexture, textureSize,
                    MPGuiVector.of(startPos.x(), startPos.y() + elementSize.y() * index),
                    elementSize,
                    scaleRules
            ));
            return this;
        }

        public MPGuiTexturePack build() { return new MPGuiTexturePack(textures); }
    }
}
