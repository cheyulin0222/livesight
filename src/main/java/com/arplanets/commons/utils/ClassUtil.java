package com.arplanets.commons.utils;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ClassUtil {

    public static HashMap<String , Field> getAllField(Object model ) {

        Class<?> clazz = model.getClass();
        HashMap<String , Field> list = new HashMap<>();

        while( clazz != null ) {
            Field[] files = clazz.getDeclaredFields();

            for (Field file : files) {
                list.put(file.getName(), file);
            }

            clazz = clazz.getSuperclass();
        }
        return list ;
    }
}
