package io.shmilyhe.convert.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;




public class MapToEntityConverter {
      private static boolean makeAccessible=false;
      static {
        makeAccessible =testMakeAccessible();
        System.out.println("support makeAccessible:"+makeAccessible);
      }

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
            //System.out.println("创建实例:"+clazz);
            T entity =clazz.getDeclaredConstructor().newInstance();

            // 2. 遍历 Map，反射设置字段值
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                // 获取字段（包括私有字段）
                Field field = getField(clazz, fieldName);

                if(field==null){
                    //System.out.println(clazz+ " is null:"+fieldName);
                    continue;
                }
                boolean gtype = field.getGenericType() instanceof ParameterizedType;
                boolean tfield =isTypeVariable(field);
                //System.out.println(field.getName()+":"+field.getGenericType() +"|"+field.getGenericType().getClass());
                if(gtype){
                    //System.out.println("gtype:"+field.getName());
                }
                boolean isList =List.class.isAssignableFrom(field.getType());
                if(isList){
                    //System.out.println("is list:"+field.getName());
                }


                if (field != null && value != null) {
                    // 设置可访问（突破 private 限制）
                    field.setAccessible(true);

                    // 类型转换后设置值
                    Object convertedValue =null;
                    if(gtype&& isList){
                        //System.out.println(getListGenericType(field));
                        convertedValue=convertList(value, field,(Class)getListGenericType(field));
                    }
                    else if(tfield){
                       convertedValue= convertType(value, field,getGenericType(clazz));
                    }else{
                       convertedValue= convertType(value, field,null);
                    }
                     
                    //field.set(entity, convertedValue);
                    setFieldValue(entity,field,convertedValue);
                }
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Map 转实体失败: " + e.getMessage(), e);
        }
    }

    public static boolean isTypeVariable(Field field) {
        return field.getGenericType() instanceof TypeVariable;
    }

    public static <T> Class<T> getGenericType(Class<?> clazz) {
    Type type = ((ParameterizedType) clazz.getGenericSuperclass())
                    .getActualTypeArguments()[0];
    return (Class<T>) type;
}

    public static Type getListGenericType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            if (pt.getRawType() == List.class) {
                Type[] args = pt.getActualTypeArguments();
                return args.length > 0 ? args[0] : null;
            }
        }
        return null;
    }


    /**
     * 递归查找字段（包括父类）
     */
    static Field getField(Class<?> clazz, String fieldName) {
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

    private static List convertList(Object value,Field field ,Class<?> targetType){
        List tlist = new ArrayList<>();
        if(value instanceof List){
            List  slist =(List)value;
            for(Object o:slist){
                if(o instanceof Map){
                    Object t=convertType(o,field,targetType);
                    tlist.add(t);
                }
            }
        }

        return tlist;
    }

    /**
     * 简单的类型转换处理
     */
    private static Object convertType(Object value, Field field ,Class<?>  gtype) {
        Class<?> targetType=field.getType();
        if(gtype!=null){
            targetType=gtype;
        }
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

        // byte数组转换 - 处理JSON数组转换为byte[]
        if (targetType == byte[].class && value instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) value;
            byte[] byteArray = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Number) {
                    byteArray[i] = ((Number) item).byteValue();
                } else if (item instanceof String) {
                    byteArray[i] = Byte.parseByte((String) item);
                } else {
                    // 如果无法转换，尝试转换为字符串再解析
                    byteArray[i] = Byte.parseByte(item.toString());
                }
            }
            return byteArray;
        }

        // 可以扩展更多类型：Date、BigDecimal、Enum 等
        if(value instanceof Map && !Map.class.isAssignableFrom(targetType)){
            //递归处理子层级对像
            return mapToEntity((Map)value,targetType);
        }

        return value; // 无法转换时原样返回（可能会抛出类型不匹配异常）
    }

     private static void setFieldValue(Object entity, Field field, Object value) {
        if(makeAccessible){
             try {
                field.set(entity, value);
             } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
             } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
             }
        }else{
            //System.out.println("set value by setter ");
            String setterName = "set" + capitalize(field.getName());
            if(!invokeSetter(entity, entity.getClass(), setterName, field.getType(), value)){

            }

        }
     }
    private static Method findMethod(Class<?> clazz, String name, Class<?> paramType) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(name, paramType);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static boolean invokeSetter(Object entity, Class<?> clazz, 
                                       String methodName, Class<?> paramType, Object value) {
        try {
            Method method = findMethod(clazz, methodName, paramType);
            if (method != null) {
                method.invoke(entity, value);
                return true;
            }
        } catch (Exception e) {
            // 参数类型不匹配，尝试找兼容的方法
            try {
                Method method = findCompatibleSetter(clazz, methodName, value);
                if (method != null) {
                    method.invoke(entity, value);
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    private static Method findCompatibleSetter(Class<?> clazz, String name, Object value) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && 
                    method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    // 检查类型兼容性
                    if (isCompatibleType(paramType, value)) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
     private static boolean isCompatibleType(Class<?> targetType, Object value) {
        if (value == null) return !targetType.isPrimitive();
        Class<?> valueType = value.getClass();
        
        // 直接兼容
        if (targetType.isAssignableFrom(valueType)) return true;
        
        // 原始类型 <-> 包装类型
        if (targetType.isPrimitive()) {
            return getWrapperType(targetType) == valueType;
        }
        if (valueType.isPrimitive()) {
            return getWrapperType(valueType) == targetType;
        }
        
        // 数字类型兼容
        if (Number.class.isAssignableFrom(targetType) && 
            Number.class.isAssignableFrom(valueType)) {
            return true;
        }
        
        return false;
    }

    private static Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == char.class) return Character.class;
        return primitiveType;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static boolean testMakeAccessible(){
        Field field = getField(TClass.class,"test");
        try {
            field.setAccessible(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class TClass{
        private String test;
    }
}