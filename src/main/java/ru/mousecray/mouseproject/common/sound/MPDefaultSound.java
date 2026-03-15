/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import ru.mousecray.mouseproject.Tags;

public class MPDefaultSound extends SoundEvent {
    public MPDefaultSound(String key) {
        super(new ResourceLocation(
                Tags.MOD_ID,
                key));
        setRegistryName(key);
    }
}
