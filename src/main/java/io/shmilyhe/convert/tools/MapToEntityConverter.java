package io.shmilyhe.convert.tools;


import java.lang.reflect.Field;
import java.util.Map;

public class MapToEntityConverter {

      public static <T>T getBean(String json, Class<T> clazz){
        try {
            Map<String,Object> root = (Map)SimpleJson.parse(json).getRoot();
            T bean = MapToEntityConverter.mapToEntity(root, clazz);
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将 Map 转换为指定类型的实体对象
     * 
     * @param map   数据源 Map，key 对应字段名，value 对应字段值
     * @param clazz 目标实体类的 Class 对象
     * @param <T>   泛型类型
     * @return 转换后的实体对象
     */
    public static <T> T mapToEntity(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            // 1. 创建实例（要求有无参构造器）
            T entity = clazz.getDeclaredConstructor().newInstance();

            // 2. 遍历 Map，反射设置字段值
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                // 获取字段（包括私有字段）
                Field field = getField(clazz, fieldName);
                if (field != null && value != null) {
                    // 设置可访问（突破 private 限制）
                    field.setAccessible(true);

                    // 类型转换后设置值
                    Object convertedValue = convertType(value, field.getType());
                    field.set(entity, convertedValue);
                }
            }

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Map 转实体失败: " + e.getMessage(), e);
        }
    }

    /**
     * 递归查找字段（包括父类）
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // 当前类没有，去父类找
                currentClass = currentClass.getSuperclass();
            }
        }
        return null; // 找不到该字段
    }

    /**
     * 简单的类型转换处理
     */
    private static Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;

        // 类型已经匹配，直接返回
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // String 类型转换
        if (targetType == String.class) {
            return value.toString();
        }

        // 数字类型转换（示例：Integer）
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        }

        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        }

        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        // 可以扩展更多类型：Date、BigDecimal、Enum 等

        return value; // 无法转换时原样返回（可能会抛出类型不匹配异常）
    }
}