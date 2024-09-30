package com.twentyfive.apaapilayer.utils;

import java.lang.reflect.Field;

public class ReflectionUtilities {

    public static void updateNonNullFields(Object source, Object target) {
        Class<?> sourceClass = source.getClass();
        while (sourceClass != null) {
            for (Field field : sourceClass.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(source);
                    if (value != null) {
                        field.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            sourceClass = sourceClass.getSuperclass(); // Passa alla superclasse
        }
    }

}
