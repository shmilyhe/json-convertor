

import io.shmilyhe.convert.JsonConvertor;
import io.shmilyhe.convert.tools.ResourceReader;

public class TestMupJsonConvertor{

    public static void main(String []args){
        String json =ResourceReader.read("testfile/test1.json");
        //String commands =ResourceReader.read("testfile/test1.script");
        Object data =JsonConvertor.toData(json);
        JsonConvertor[] jcs = new JsonConvertor[]{
            new JsonConvertor(ResourceReader.read("testfile/test1.script")),
            new JsonConvertor(ResourceReader.read("testfile/test2.script"))
        };
        for(JsonConvertor j:jcs){
            data= j.convert(data,null);
        }
        System.out.println(JsonConvertor.toJsonString(data));
    }
}