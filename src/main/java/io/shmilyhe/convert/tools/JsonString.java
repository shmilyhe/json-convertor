package io.shmilyhe.convert.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class JsonString {
    // 静态方法入口
    public static String asJsonString(Object o) {
        StringBuilder json = new StringBuilder();
        asJson(json, o, 0);
        return json.toString();
    }

    // 核心递归序列化
    private static void asJson(StringBuilder json, Object o, int level) {
        if (o == null) {
            json.append("null");
        } else if (o instanceof String) {
            String resp = (String) o;
            if (resp.indexOf('"') > -1) {
                resp = resp.replaceAll("\\\"", "\\\\\"");
            }
            json.append('"').append(resp).append('"');
        } else if (o instanceof Integer
                || o instanceof Double
                || o instanceof Long
                || o instanceof Float
                || o instanceof Short
                || o instanceof Byte
                || o instanceof BigDecimal
                || o instanceof Boolean
                || o.getClass().equals(boolean.class)) {
            json.append(o);
        } else if (o instanceof Character) {
            // 字符转为带引号的字符串
            json.append('"').append(o).append('"');
        } else if (o instanceof Date) {
            json.append('"').append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(o)).append('"');
        } else if (o instanceof LocalDateTime) {
            LocalDateTime lt = (LocalDateTime) o;
            json.append('"').append(lt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append('"');
        } else if (o instanceof LocalDate) {
            LocalDate ld = (LocalDate) o;
            json.append('"').append(ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append('"');
        } else if (o.getClass().isArray()) {
            // 统一处理所有数组类型（包括基本类型数组）
            jsonArray(json, o, level);
        } else if (o instanceof Map) {
            jsonMap(json, (Map<?, ?>) o, level);
        } else if (o instanceof Collection) {
            jsonCollection(json, (Collection<?>) o, level);
        } else {
            jsonObject(json, o, level);
        }
    }

    // 处理 Collection
    private static void jsonCollection(StringBuilder json, Collection<?> co, int level) {
        json.append('[');
        boolean isFirst = true;
        for (Object o : co) {
            if (isFirst) {
                isFirst = false;
            } else {
                json.append(',');
            }
            asJson(json, o, level + 1);
        }
        json.append(']');
    }

    // 统一处理所有数组类型（使用反射）
    private static void jsonArray(StringBuilder json, Object array, int level) {
        json.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                json.append(',');
            }
            Object element = Array.get(array, i);
            asJson(json, element, level + 1);
        }
        json.append(']');
    }

    // 处理 Map
    private static void jsonMap(StringBuilder json, Map<?, ?> map, int level) {
        json.append('{');
        boolean isFirst = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (e.getValue() == null) continue;
            if (isFirst) {
                isFirst = false;
            } else {
                json.append(',');
            }
            json.append('"').append(e.getKey()).append("\":");
            asJson(json, e.getValue(), level + 1);
        }
        json.append('}');
    }

    // 处理普通 Java 对象（JavaBean）
    private static void jsonObject(StringBuilder json, Object o, int level) {
        json.append('{');
        boolean isFirst = true;
        Method[] methods = o.getClass().getMethods();
        for (Method m : methods) {
            String methodName = m.getName();
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
            if (m.getParameterCount() != 0) continue;

            String propName = null;
            boolean isGetter = false;
            if (methodName.startsWith("get") && methodName.length() > 3) {
                isGetter = true;
                propName = getPropertyName(methodName, false);
            } else if (methodName.startsWith("is") && methodName.length() > 2) {
                isGetter = true;
                propName = getPropertyName(methodName, true);
            }

            if (!isGetter) continue;

            // 过滤不需要的属性
            if ("class".equals(propName)
                    || "annotations".equals(propName)
                    || "annotatedInterfaces".equals(propName)
                    || "annotatedOwnerType".equals(propName)) {
                continue;
            }

            try {
                Object value = m.invoke(o);
                if (value == null) continue;
                if (isFirst) {
                    isFirst = false;
                } else {
                    json.append(',');
                }
                json.append('"').append(propName).append("\":");
                asJson(json, value, level + 1);
            } catch (Exception e) {
                // 忽略无法读取的属性
            }
        }
        json.append('}');
    }

    /**
     * 根据 getter 方法名推导 JavaBean 属性名
     * @param methodName 方法名
     * @param isIsMethod 是否为 isXxx 形式的 boolean getter
     * @return 属性名
     */
    private static String getPropertyName(String methodName, boolean isIsMethod) {
        int start = isIsMethod ? 2 : 3;
        String name = methodName.substring(start);
        // 如果前两个字母都是大写，保持原样；否则首字母小写
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        } else {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
    }
}

