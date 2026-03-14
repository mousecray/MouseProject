/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.registry;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.entity.potion.PotionMagicWallet;
import ru.mousecray.mouseproject.registry.constants.PotionNames;
import ru.mousecray.mouseproject.registry.constants.PotionTextures;
import ru.mousecray.mouseproject.utils.MPReflectionUtils;

@GameRegistry.ObjectHolder(Tags.MOD_ID)
@Mod.EventBusSubscriber
public class MPPotions {
    public static Potion DOUBLE_CRAFT        = MPPlugFactory.CREATE_POTION_PLUG();
    public static Potion DOUBLE_MYTHIC       = MPPlugFactory.CREATE_POTION_PLUG();
    public static Potion DOUBLE_FISHING      = MPPlugFactory.CREATE_POTION_PLUG();
    public static Potion DOUBLE_FARM_HARVEST = MPPlugFactory.CREATE_POTION_PLUG();
    public static Potion IMMORTALITY         = MPPlugFactory.CREATE_POTION_PLUG();

    private static void onInit() {
        DOUBLE_CRAFT = new PotionMagicWallet(PotionNames.DOUBLE_CRAFT_NAME, 0xCFB53B, PotionTextures.DOUBLE_CRAFT);
        DOUBLE_MYTHIC = new PotionMagicWallet(PotionNames.DOUBLE_MYTHIC_NAME, 0xB300B3, PotionTextures.DOUBLE_MYTHIC);
        DOUBLE_FISHING = new PotionMagicWallet(PotionNames.DOUBLE_FISHING_NAME, 0x0000C2, PotionTextures.DOUBLE_FISHING);
        DOUBLE_FARM_HARVEST = new PotionMagicWallet(PotionNames.DOUBLE_FARM_HARVEST_NAME, 0x2BB52B, PotionTextures.DOUBLE_FARM_HARVEST);
        IMMORTALITY = new PotionMagicWallet(PotionNames.IMMORTALITY_NAME, 0x434A52, PotionTextures.IMMORTALITY);
    }

    @SubscribeEvent
    public static void onRegistryPotion(RegistryEvent.Register<Potion> e) {
        onInit();
        MouseProject.LOGGER.info("Initialized potions");

        IForgeRegistry<Potion> registry = e.getRegistry();
        MPReflectionUtils.prepare(MPPotions.class).<Potion>getPublicStaticFields().forEach(registry::register);
        MouseProject.LOGGER.info("Registered potions");
    }
}