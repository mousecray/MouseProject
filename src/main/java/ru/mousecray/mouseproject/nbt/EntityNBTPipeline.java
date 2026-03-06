package ru.mousecray.mouseproject.nbt;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class EntityNBTPipeline {
    static final String                             TAG_FAKE_KEY              = "Fake";
    static final Function<ResourceLocation, String> TAG_POTION_FIRST_TICK_KEY = i -> {
        if (i != null) return "PotionFirstTick_" + i;
        else return "PotionFirstTick";
    };

    private final MouseProjectNBT.MouseProjectNBTEntity container;

    private EntityNBTPipeline(MouseProjectNBT.MouseProjectNBTEntity container) { this.container = container; }

    static EntityNBTPipeline get(MouseProjectNBT.MouseProjectNBTEntity base)   { return new EntityNBTPipeline(base); }

    public void removePotionFirstTick(Potion potion) {
        if (container.hasModTag()) container.getModTag().removeTag(TAG_POTION_FIRST_TICK_KEY.apply(potion.getRegistryName()));
        container.removeAllTagIfEmpty();
    }

    public void savePotionFirstTick(Potion potion) {
        container.getModTag().setBoolean(TAG_POTION_FIRST_TICK_KEY.apply(potion.getRegistryName()), true);
    }

    public boolean loadPotionFirstTick(Potion potion) {
        if (container.hasModTag()) {
            String key = TAG_POTION_FIRST_TICK_KEY.apply(potion.getRegistryName());
            return container.getModTag().hasKey(key)
                    && container.getModTag().getBoolean(TAG_POTION_FIRST_TICK_KEY.apply(potion.getRegistryName()));
        } else return false;
    }

    public void saveFake() {
        container.getModTag().setBoolean(TAG_FAKE_KEY, true);
    }

    public boolean loadFake() {
        return container.hasModTag()
                && container.getModTag().hasKey(TAG_FAKE_KEY)
                && container.getModTag().getBoolean(TAG_FAKE_KEY);
    }
}