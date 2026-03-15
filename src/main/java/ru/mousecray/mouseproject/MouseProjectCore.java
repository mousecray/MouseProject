/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("MouseProjectCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1000)
public class MouseProjectCore implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        System.out.println("[MouseProject] Registering mixin config: mixins.${mod_id}.json");
        List<String> configs = new ArrayList<>();
        configs.add("mixins." + Tags.MOD_ID + ".json");
        return configs;
    }

    @Override public String[] getASMTransformerClass()         { return new String[0]; }
    @Override public String getModContainerClass()             { return null; }
    @Nullable @Override public String getSetupClass()          { return null; }
    @Override public void injectData(Map<String, Object> data) { }
    @Override public String getAccessTransformerClass()        { return null; }
}