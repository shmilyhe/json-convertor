package io.shmilyhe.convert.tools;
public class JBean {



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



}
