/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.registry;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.common.sound.MPDefaultSound;
import ru.mousecray.mouseproject.registry.constants.SoundNames;
import ru.mousecray.mouseproject.utils.MPReflectionUtils;

@Mod.EventBusSubscriber
public class MPSounds {
    public static SoundEvent WALLET_SHUFFLE_HOTBAR = MPPlugFactory.CREATE_SOUND_PLUG();
    public static SoundEvent WALLET_EFFECT_USE     = MPPlugFactory.CREATE_SOUND_PLUG();

    public static SoundEvent COIN_DROP   = MPPlugFactory.CREATE_SOUND_PLUG();
    public static SoundEvent COIN_PICKUP = MPPlugFactory.CREATE_SOUND_PLUG();

    public static SoundEvent TRADE = MPPlugFactory.CREATE_SOUND_PLUG();

    private static void onInit() {
        WALLET_SHUFFLE_HOTBAR = new MPDefaultSound(SoundNames.WALLET_SHUFFLE_HOTBAR);
        WALLET_EFFECT_USE = new MPDefaultSound(SoundNames.WALLET_EFFECT_USE);

        COIN_DROP = new MPDefaultSound(SoundNames.COIN_DROP);
        COIN_PICKUP = new MPDefaultSound(SoundNames.COIN_PICKUP);

        TRADE = new MPDefaultSound(SoundNames.TRADE);
    }

    @SubscribeEvent
    public static void onRegistrySound(RegistryEvent.Register<SoundEvent> e) {
        onInit();
        MouseProject.LOGGER.info("Initialized sounds");

        IForgeRegistry<SoundEvent> registry = e.getRegistry();
        MPReflectionUtils.prepare(MPSounds.class).<SoundEvent>getPublicStaticFields().forEach(registry::register);
        MouseProject.LOGGER.info("Registered sounds");
    }
}
