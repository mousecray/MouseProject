/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.event;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.core.component.sound.MPSoundSourceType;

@SideOnly(Side.CLIENT)
public class MPGuiSoundEvent<T extends MPGuiElement<T>> extends MPGuiEvent<T> {
    private SoundEvent        sound;
    private MPSoundSourceType source;
    private SoundHandler      handler;

    void setSource(MPSoundSourceType source) { this.source = source; }
    void setSound(SoundEvent sound)          { this.sound = sound; }
    void setHandler(SoundHandler handler)    { this.handler = handler; }
    public MPSoundSourceType getSource()     { return source; }
    public SoundEvent getSound()             { return sound; }
    public SoundHandler getHandler()         { return handler; }
}