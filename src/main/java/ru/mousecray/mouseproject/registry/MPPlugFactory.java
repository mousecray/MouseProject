/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.registry;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.mousecray.mouseproject.common.entity.potion.PotionPass;
import ru.mousecray.mouseproject.common.sound.MPDefaultSound;
import ru.mousecray.mouseproject.registry.constants.PotionNames;
import ru.mousecray.mouseproject.registry.constants.SoundNames;

import javax.annotation.Nonnull;

public class MPPlugFactory {
    private static final Item         PLUG_ITEM;
    private static final Potion       PLUG_POTION;
    private static final SoundEvent   PLUG_SOUND;
    private static final DamageSource PLUG_DAMAGE_SOURCE;


    static {
        PLUG_SOUND = new MPDefaultSound(SoundNames.SILENCE);
        ForgeRegistries.SOUND_EVENTS.register(PLUG_SOUND);
        PLUG_POTION = new PotionPass(PotionNames.PASS);
        ForgeRegistries.POTIONS.register(PLUG_POTION);
        PLUG_ITEM = Items.AIR;
        PLUG_DAMAGE_SOURCE = DamageSource.GENERIC;
    }

    @Nonnull public static Item CREATE_ITEM_PLUG()                  { return PLUG_ITEM; }
    @Nonnull public static SoundEvent CREATE_SOUND_PLUG()           { return PLUG_SOUND; }
    @Nonnull public static Potion CREATE_POTION_PLUG()              { return PLUG_POTION; }
    @Nonnull public static DamageSource CREATE_DAMAGE_SOURCE_PLUG() { return PLUG_DAMAGE_SOURCE; }
}