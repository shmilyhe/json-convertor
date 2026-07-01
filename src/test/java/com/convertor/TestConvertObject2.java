package com.convertor;

import java.util.HashMap;

import io.shmilyhe.convert.JsonConvertor;
import io.shmilyhe.convert.tools.ExpEnv;
import io.shmilyhe.convert.tools.JsonString;
import io.shmilyhe.convert.tools.ResourceReader;

public class TestConvertObject2 {
    
    public static void main(String[] args) {
        String script =ResourceReader.read("testfile/testObject2.script");
        JsonConvertor jc = new JsonConvertor(new String[]{script});
        Staff st = new Staff();
        Info info = new Info();
        st.setInfo(info);
       ExpEnv env =  new ExpEnv(null);
       env.put("age",20);
       env.put("position","leader");
       env.put("name","kit");
       env.put("pojo", st);
       Object result =  jc.convert(new HashMap(), env);
        String json = JsonString.asJsonString(st);
        System.out.println(JsonString.asJsonString(result));
        System.out.println(json);
        

    }

    public static class Staff{
        private String name;

        private Info info;

        private int age;

        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Info getInfo() {
            return info;
        }

        public void setInfo(Info info) {
            this.info = info;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        

    }

    public static class Info{
        private String position;
        private String title;
        public String getPosition() {
            return position;
        }
        public void setPosition(String position) {
            this.position = position;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        
    }
}
