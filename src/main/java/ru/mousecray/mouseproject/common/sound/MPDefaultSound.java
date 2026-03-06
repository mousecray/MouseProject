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
