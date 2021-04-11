package de.cerus.packetmaps.nmsbase.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static void setField(final Object o, final Class<?> cls, final String field, final Object value) {
        try {
            final Field declaredField = cls.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(o, value);
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
