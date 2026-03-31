/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.components.sound;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MPGuiSoundPack {
    public static MPGuiSoundPack EMPTY() { return new MPGuiSoundPack(new Object2ObjectArrayMap<>()); }

    public static MPGuiSoundPack CONTROL_SIMPLE() {
        return Builder.create().addSound(SoundSourceType.PRESS, SoundEvents.UI_BUTTON_CLICK).build();
    }

    private final Object2ObjectMap<SoundSourceType, SoundEvent> sounds;

    private MPGuiSoundPack(Object2ObjectMap<SoundSourceType, SoundEvent> sounds) { this.sounds = sounds; }

    @Nullable public SoundEvent getSound(SoundSourceType sourceType)             { return sounds.get(sourceType); }

    @SideOnly(Side.CLIENT)
    public static class Builder {
        private final Object2ObjectMap<SoundSourceType, SoundEvent> sounds = new Object2ObjectArrayMap<>();

        private Builder()                             { }

        public static MPGuiSoundPack.Builder create() { return new MPGuiSoundPack.Builder(); }

        public MPGuiSoundPack.Builder addSound(SoundSourceType sourceType, SoundEvent sound) {
            sounds.put(sourceType, sound);
            return this;
        }

        public MPGuiSoundPack build() { return new MPGuiSoundPack(sounds); }
    }
}