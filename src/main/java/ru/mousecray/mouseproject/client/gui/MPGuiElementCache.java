package ru.mousecray.mouseproject.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MPGuiElementCache {
    private final Map<String, WeakReference<MPGuiElement<?>>> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends MPGuiElement<T>> T get(String key, Class<T> type) {
        WeakReference<MPGuiElement<?>> ref = cache.get(key);
        if (ref != null) {
            MPGuiElement<?> element = ref.get();
            if (type.isInstance(element)) {
                return (T) element;
            } else if (element != null) {
                cache.remove(key);
            }
        }
        return null;
    }

    public <T extends MPGuiElement<T>> void put(String key, T element) {
        cache.put(key, new WeakReference<>(element));
    }

    public void clear() {
        cache.clear();
    }
}
