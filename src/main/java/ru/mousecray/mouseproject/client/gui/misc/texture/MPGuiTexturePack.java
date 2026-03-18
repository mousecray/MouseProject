/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.misc.texture;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.dim.IGuiVector;
import ru.mousecray.mouseproject.client.gui.misc.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.misc.state.MPGuiElementStateManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MPGuiTexturePack {
    public static MPGuiTexturePack EMPTY = new MPGuiTexturePack(Collections.emptyList());

    private static class StateTexture {
        final int          requiredMask;
        final MPGuiTexture texture;

        StateTexture(int requiredMask, MPGuiTexture texture) {
            this.requiredMask = requiredMask;
            this.texture = texture;
        }
    }

    private final List<StateTexture> textures;

    private MPGuiTexturePack(List<StateTexture> textures) {
        this.textures = textures;
    }

    public MPGuiTexture getCalculatedTexture(MPGuiElementStateManager currentManager) {
        for (StateTexture st : textures) {
            if (currentManager.satisfies(st.requiredMask)) {
                return st.texture;
            }
        }
        return null;
    }

    @Nullable
    public MPGuiTexture getTexture(MPGuiElementState state, MPGuiElementState... states) {
        for (StateTexture st : textures) {
            if (st.requiredMask == MPGuiElementStateManager.createMask(state, states)) return st.texture;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static class Builder {
        private final List<StateTexture> textures = new ArrayList<>();
        private final ResourceLocation   baseTexture;
        private final IGuiVector         textureSize;
        private final IGuiVector         startPos;
        private final IGuiVector         elementSize;

        private GuiTextureScaleRules scaleRules = new GuiTextureScaleRules(GuiTextureScaleType.STRETCH);

        private Builder(ResourceLocation baseTexture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
            this.baseTexture = baseTexture;
            this.textureSize = textureSize;
            this.startPos = startPos;
            this.elementSize = elementSize;
        }

        public static Builder create(ResourceLocation baseTexture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
            return new Builder(baseTexture, textureSize, startPos, elementSize);
        }

        public Builder setScaleRules(GuiTextureScaleRules scaleRules) {
            this.scaleRules = scaleRules;
            return this;
        }

        public Builder addTexture(int index, float opacity, MPGuiElementState... requiredStates) {
            int mask = MPGuiElementStateManager.createMask(requiredStates);
            textures.add(new StateTexture(mask, new MPGuiTexture(
                    baseTexture, textureSize,
                    GuiVector.of(startPos.x(), startPos.y() + elementSize.y() * index),
                    elementSize,
                    scaleRules,
                    Math.max(0.0f, Math.min(1.0f, opacity))
            )));
            return this;
        }

        public Builder addTexture(int index, MPGuiElementState... requiredStates) {
            int mask = MPGuiElementStateManager.createMask(requiredStates);
            textures.add(new StateTexture(mask, new MPGuiTexture(
                    baseTexture, textureSize,
                    GuiVector.of(startPos.x(), startPos.y() + elementSize.y() * index),
                    elementSize,
                    scaleRules
            )));
            return this;
        }

        public MPGuiTexturePack build() { return new MPGuiTexturePack(textures); }
    }
}
