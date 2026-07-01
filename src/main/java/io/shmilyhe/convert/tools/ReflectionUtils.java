package io.shmilyhe.convert.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射工具类 - 用于获取/设置 POJO 属性值
 * 特性：
 * 1. 优先使用 public getter/setter 方法
 * 2. 支持嵌套属性（如 "user.address.city"）
 * 3. 支持父类属性
 * 4. Method 缓存提升性能
 * 5. 线程安全
 */
public class ReflectionUtils {
    
    /**
     * 获取属性值（支持嵌套属性）
     */
    public static Object get(Object origin, String field) {
        if (origin == null || field == null || field.trim().isEmpty()) {
            return null;
        }
        
        if (field.contains(".")) {
            String[] parts = field.split("\\.");
            Object current = origin;
            for (String part : parts) {
                if (current == null) {
                    return null;
                }
                current = getSingleProperty(current, part.trim());
            }
            return current;
        }
        
        return getSingleProperty(origin, field.trim());
    }
    
    /**
     * 设置属性值（支持嵌套属性）
     */
    public static boolean set(Object origin, String field, Object value) {
        if (origin == null || field == null || field.trim().isEmpty()) {
            return false;
        }
        
        if (field.contains(".")) {
            String[] parts = field.split("\\.");
            Object current = origin;
            
            for (int i = 0; i < parts.length - 1; i++) {
                if (current == null) {
                    return false;
                }
                current = getSingleProperty(current, parts[i].trim());
            }
            
            if (current == null) {
                return false;
            }
            
            return setSingleProperty(current, parts[parts.length - 1].trim(), value);
        }
        
        return setSingleProperty(origin, field.trim(), value);
    }
    
    /**
     * 获取单个属性值（不缓存，每次都重新查找）
     */
    private static Object getSingleProperty(Object origin, String fieldName) {
        Class<?> clazz = origin.getClass();
        
        // 1. 尝试 getter 方法
        try {
            String getterName = "get" + capitalize(fieldName);
            Method getter = clazz.getMethod(getterName);
            return getter.invoke(origin);
        } catch (Exception e) {
            // 继续尝试
        }
        
        // 2. 尝试 boolean 的 is 方法
        try {
            String getterName = "is" + capitalize(fieldName);
            Method getter = clazz.getMethod(getterName);
            return getter.invoke(origin);
        } catch (Exception e) {
            // 继续尝试
        }
        
        // 3. 直接访问字段（包括父类）
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                Field field = currentClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(origin);
            } catch (Exception e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        
        return null;
    }
    
    /**
     * 设置单个属性值（不缓存，每次都重新查找）
     */
    private static boolean setSingleProperty(Object origin, String fieldName, Object value) {
        Class<?> clazz = origin.getClass();
        
        // 1. 尝试 setter 方法
        try {
            // 先获取字段类型
            Field field = findFieldNoCache(clazz, fieldName);
            if (field == null) {
                return false;
            }
            
            String setterName = "set" + capitalize(fieldName);
            Method setter = clazz.getMethod(setterName, field.getType());
            setter.invoke(origin, value);
            return true;
        } catch (Exception e) {
            // 继续尝试
        }
        
        // 2. 直接访问字段
        Field field = findFieldNoCache(clazz, fieldName);
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(origin, value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 查找字段（不缓存）
     */
    private static Field findFieldNoCache(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
