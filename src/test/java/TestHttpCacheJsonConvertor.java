

import io.shmilyhe.convert.JsonConvertor;
import io.shmilyhe.convert.ext.HttpFunction;
import io.shmilyhe.convert.tools.ExpEnv;
import io.shmilyhe.convert.tools.ResourceReader;

public class TestHttpCacheJsonConvertor{

    public static void main(String []args){
        TestHttpGetCache cache = new TestHttpGetCache();
        HttpFunction.setCache(cache);
        String json ="{}";//ResourceReader.read("testfile/test1.json");
        //String commands =ResourceReader.read("testfile/test1.script");
        Object data =JsonConvertor.toData(json);
        String[] cmds = new String[]{
            ResourceReader.read("testfile/test5.script"),
        };
        ExpEnv env= new ExpEnv(null);
        env.put("title", "这是一从外部带来的参数测试!");
        //env.put("notExists", 1);
        {
            JsonConvertor j = new JsonConvertor(cmds);
            data= j.convert(data,env);
            System.out.println(env.isExited());
            System.out.println(JsonConvertor.toJsonString(data));
        }

        {
            JsonConvertor j = new JsonConvertor(cmds);
            data= j.convert(data,env);
            System.out.println(env.isExited());
            System.out.println(JsonConvertor.toJsonString(data));
        }
        for(int i=0;i<1000000;i++)
        {
            JsonConvertor j = new JsonConvertor(cmds);
            data= j.convert(data,env);
            System.out.println(env.isExited());
            System.out.println(JsonConvertor.toJsonString(data));
        }
        
    }
}