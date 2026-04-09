/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.misc;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.core.MPGuiScreen;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class MPGuiElementCache {
    public static final MPGuiElementCache INSTANCE = new MPGuiElementCache();

    private final Map<String, WeakReference<MPGuiElement<?>>> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends MPGuiElement<?>> T get(MPGuiScreen screen, String key, Class<T> type) {
        String                         actualKey = screen.getScreenName() + ":" + key;
        WeakReference<MPGuiElement<?>> ref       = cache.get(actualKey);
        if (ref != null) {
            MPGuiElement<?> element = ref.get();
            if (type.isInstance(element)) {
                return (T) element;
            } else if (element != null) {
                cache.remove(actualKey);
            }
        }
        return null;
    }

    public <T extends MPGuiElement<?>> T getOrCreate(MPGuiScreen screen, String key, Class<T> type, Supplier<T> typeSupplier, Consumer<T> createAction, Consumer<T> existAction, Consumer<T> finalAction) {
        T t = get(screen, key, type);
        if (t == null) {
            put(screen, key, t = typeSupplier.get());
            if (createAction != null) createAction.accept(t);
        } else if (existAction != null) existAction.accept(t);
        if (finalAction != null) finalAction.accept(t);
        return t;
    }

    public <T extends MPGuiElement<?>> T getOrCreate(MPGuiScreen screen, String key, Class<T> type, Supplier<T> typeSupplier, Consumer<T> createAction, Consumer<T> finalAction) {
        return getOrCreate(screen, key, type, typeSupplier, createAction, null, finalAction);
    }

    public <T extends MPGuiElement<?>> T getOrCreate(MPGuiScreen screen, String key, Class<T> type, Supplier<T> typeSupplier, Consumer<T> createAction) {
        return getOrCreate(screen, key, type, typeSupplier, createAction, null);
    }

    public <T extends MPGuiElement<?>> T getOrCreate(MPGuiScreen screen, String key, Class<T> type, Supplier<T> typeSupplier) {
        return getOrCreate(screen, key, type, typeSupplier, null);
    }

    public <T extends MPGuiElement<?>> void put(MPGuiScreen screen, String key, T element) {
        cache.put(screen.getScreenName() + ":" + key, new WeakReference<>(element));
    }

    public void clear() {
        cache.clear();
    }
}
