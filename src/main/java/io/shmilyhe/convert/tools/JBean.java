package io.shmilyhe.convert.tools;

import java.lang.reflect.Field;

public class JBean {
    private static boolean makeAccessible=false;
      static {
        makeAccessible =MapToEntityConverter.testMakeAccessible();
        System.out.println("support makeAccessible:"+makeAccessible);
      }


    public static <T> T toBean(String json, T t){
        try {
            return (T) MapToEntityConverter.getBean(json, t.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

     public static <T> T toBean(String json, Class<T> clazz){
        return MapToEntityConverter.getBean(json, clazz);
    }


    public static Object get(Object origin,String feild){
        Field field = MapToEntityConverter.getField(origin.getClass(), feild);
        if(field==null)return null;

        return null;
    }

    public static void set(Object origin,String feild,Object value){
   
    }



}
