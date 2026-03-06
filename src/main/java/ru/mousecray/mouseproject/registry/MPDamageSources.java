package ru.mousecray.mouseproject.registry;

import net.minecraft.util.DamageSource;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.registry.constants.DamageSourceNames;

public class MPDamageSources {
    public static final MPDamageSources INSTANCE = new MPDamageSources();

    public static DamageSource ON_DROPPED_COIN = MPPlugFactory.CREATE_DAMAGE_SOURCE_PLUG();

    private void onInit() {
        ON_DROPPED_COIN = new DamageSource(Tags.MOD_ID + ":" + DamageSourceNames.ON_DROPPED_COIN_NAME);
    }

    public void register() {
        onInit();
        MouseProject.LOGGER.info("Registered DamageSources");
    }
}