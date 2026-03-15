/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.common.eventhandler.CapabilityHandler;
import ru.mousecray.mouseproject.common.eventhandler.CoinHandler;
import ru.mousecray.mouseproject.common.eventhandler.MagicWalletHandler;
import ru.mousecray.mouseproject.common.eventhandler.PotionEffectHandler;
import ru.mousecray.mouseproject.registry.*;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        MPCapabilities.INSTANCE.register();
        MPPackets.INSTANCE.register();
        MPTriggers.INSTANCE.register();
        MPDamageSources.INSTANCE.register();
        MinecraftForge.EVENT_BUS.register(new CoinHandler());
        MinecraftForge.EVENT_BUS.register(new MagicWalletHandler());
        MinecraftForge.EVENT_BUS.register(new PotionEffectHandler());
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(MouseProject.INSTANCE, new MPGuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}