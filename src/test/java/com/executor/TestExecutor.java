package com.executor;

import io.shmilyhe.convert.VRLExecutor;
import io.shmilyhe.convert.tools.ExpEnv;
import io.shmilyhe.convert.tools.JsonString;
import io.shmilyhe.convert.tools.ResourceReader;

public class TestExecutor {
    
    public static void main(String[] args) {
        String script =ResourceReader.read("testfile/test_VRL.script");

        /**
         * 加载脚本，VRLExecutor 只有AST 与Environment 是分离，只需要加载一次，当脚本重用时请不要反复创建，AST 花的时间较多。
         */
        VRLExecutor ve = new VRLExecutor(new String[]{script});
        /**
         * 创建java pojo
         */
        Staff st = new Staff();
        Info info = new Info();
        st.setInfo(info);
        /**
         * 往Environment 添加参数
         */
       ExpEnv env =  new ExpEnv(null);
       env.put("age",20);
       env.put("position","leader");
       env.put("name","kit");
       env.put("pojo", st);

       /**
        * 运行脚本
        */
       Object result =  ve.execute(env);
       System.out.println("脚本的处理结果:\t"+JsonString.asJsonString(result));
       System.out.println("pojo 修改后:\t"+JsonString.asJsonString(st));
        

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
