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
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;
import ru.mousecray.mouseproject.client.gui.state.IGuiButtonState;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MPGuiTexturePack {
    public static MPGuiTexturePack EMPTY = new MPGuiTexturePack(new HashMap<>());

    private final Map<IGuiButtonState, MPGuiTexture> textures;

    private MPGuiTexturePack(Map<IGuiButtonState, MPGuiTexture> textures) { this.textures = textures; }

    public MPGuiTexture getTexture(IGuiButtonState state)                 { return textures.get(state); }

    public MPGuiTexture getCalculatedTexture(GuiButtonActionState actionState, GuiButtonPersistentState persistentState) {
        MPGuiTexture texture;
        if (actionState != null) {
            texture = textures.get(actionState.combine(persistentState));
            if (texture == null) {
                texture = textures.get(actionState);
                if (texture == null) texture = textures.get(persistentState);
            }
        } else texture = textures.get(persistentState);

        return texture;
    }
    @SideOnly(Side.CLIENT)
    public static class Builder {
        private final Map<IGuiButtonState, MPGuiTexture> textures = new HashMap<>();
        private final ResourceLocation                   baseTexture;
        private final IGuiVector                         textureSize;
        private final IGuiVector                         startPos;
        private final IGuiVector                         elementSize;

        private Builder(ResourceLocation baseTexture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
            this.baseTexture = baseTexture;
            this.textureSize = textureSize;
            this.startPos = startPos;
            this.elementSize = elementSize;
        }

        public static Builder create(ResourceLocation baseTexture, IGuiVector textureSize, IGuiVector startPos, IGuiVector elementSize) {
            return new Builder(baseTexture, textureSize, startPos, elementSize);
        }

        public Builder addTexture(IGuiButtonState state, int index) {
            textures.put(state,
                    new MPGuiTexture(
                            baseTexture, textureSize,
                            new GuiVector(startPos.x(), startPos.y() + elementSize.y() * index),
                            elementSize
                    )
            );
            return this;
        }

        public MPGuiTexturePack build() { return new MPGuiTexturePack(textures); }
    }
}
