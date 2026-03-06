package ru.mousecray.mouseproject.utils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class MPReflectionUtils {
    private final           Class<?> clazz;
    @Nullable private final Field    modifiers;

    private MPReflectionUtils(Class<?> clazz, @Nullable Field modifiers) {
        this.clazz = clazz;
        this.modifiers = modifiers;
    }

    public static MPReflectionUtils prepare(Class<?> clazz) { return new MPReflectionUtils(clazz, null); }

    public static MPReflectionUtils prepareForFinal(Class<?> clazz) {
        Field modifiersField;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) { throw new RuntimeException(e); }
        modifiersField.setAccessible(true);
        return new MPReflectionUtils(clazz, modifiersField);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getPublicStaticFields() {
        List<T> result = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try { result.add((T) field.get(null)); } catch (IllegalAccessException e) { throw new RuntimeException(e); }
            }
        }
        return result;
    }
}
