/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.event;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.components.sound.SoundSourceType;

@SideOnly(Side.CLIENT)
public class MPGuiSoundEvent<T extends MPGuiElement<T>> extends MPGuiEvent<T> {
    private SoundEvent      sound;
    private SoundSourceType source;
    private SoundHandler    handler;

    void setSource(SoundSourceType source) { this.source = source; }
    void setSound(SoundEvent sound)        { this.sound = sound; }
    void setHandler(SoundHandler handler)  { this.handler = handler; }
    public SoundSourceType getSource()     { return source; }
    public SoundEvent getSound()           { return sound; }
    public SoundHandler getHandler()       { return handler; }
}