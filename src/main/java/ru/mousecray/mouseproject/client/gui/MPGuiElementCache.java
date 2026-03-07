package ru.mousecray.mouseproject.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public <T extends MPGuiElement<T>> T getOrCreate(String key, Class<T> type, Supplier<T> typeSupplier, Consumer<T> createAction, Consumer<T> existAction, Consumer<T> finalAction) {
        T t = get(key, type);
        if (t == null) {
            put(key, t = typeSupplier.get());
            if (createAction != null) createAction.accept(t);
        } else if (existAction != null) existAction.accept(t);
        if (finalAction != null) finalAction.accept(t);
        return t;
    }

    public <T extends MPGuiElement<T>> T getOrCreate(String key, Class<T> type, Supplier<T> typeSupplier, Consumer<T> createAction, Consumer<T> finalAction) {
        return getOrCreate(key, type, typeSupplier, createAction, null, finalAction);
    }

    public <T extends MPGuiElement<T>> T getOrCreate(String key, Class<T> type, Supplier<T> typeSupplier, Consumer<T> createAction) {
        return getOrCreate(key, type, typeSupplier, createAction, null);
    }

    public <T extends MPGuiElement<T>> T getOrCreate(String key, Class<T> type, Supplier<T> typeSupplier) {
        return getOrCreate(key, type, typeSupplier, null);
    }

    public <T extends MPGuiElement<T>> void put(String key, T element) {
        cache.put(key, new WeakReference<>(element));
    }

    public void clear() {
        cache.clear();
    }
}
