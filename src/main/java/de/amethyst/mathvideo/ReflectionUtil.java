package de.amethyst.mathvideo;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {
    public static Iterable<Field> getFieldsUpTo(@NonNull Class<?> startClass, Class<?> exclusiveParent) {

        List<Field> currentClassFields = new ArrayList<>(List.of(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !parentClass.equals(exclusiveParent)) {
            List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }
}
