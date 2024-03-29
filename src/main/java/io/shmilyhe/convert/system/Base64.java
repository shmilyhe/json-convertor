package io.shmilyhe.convert.system;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import javax.print.DocFlavor.INPUT_STREAM;

import io.shmilyhe.convert.callee.IFunction;
import io.shmilyhe.convert.tools.B64;

public class Base64 {
    private static Object listGet(List list,int index){
        if(list==null)return null;
        if(index<list.size()){
            return list.get(index);
        }else{
            return null;
        }
    }

    public static IFunction encode(){
        return (param,env)->{
            Object arg1= listGet(param,0);
            if(arg1 instanceof String){
                return B64.encode(arg1.toString().getBytes());
            }
            if(arg1 instanceof byte[]){
                return B64.encode((byte[])arg1);
            }
            if(arg1 instanceof Collection){
                try{
                    Collection c=(Collection)arg1;
                    byte[] ar= new byte[c.size()];
                    int i=0;
                    for(Object o:c){
                        ar[i++]=getByte(o);
                    }
                    return B64.encode(ar);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        };
    }

    private static byte getByte(Object o){
        if(o instanceof Byte)return (byte)o;
        if(o instanceof Integer)return ((Integer)o).byteValue();
        if(o instanceof Long)return ((Long)o).byteValue();
        return 0;
    }

    public static IFunction decode(){
        return (param,env)->{
            Object arg1= listGet(param,0);
            if(arg1 instanceof String){
                return  B64.decode(arg1.toString());
            }
            return null;
        };
    }

 static Charset utf8=Charset.forName("utf-8");
    public static IFunction decodeString(){
        return (param,env)->{
            Object arg1= listGet(param,0);
            if(arg1 instanceof String){
                return  new String(B64.decode(arg1.toString()),utf8);
            }
            return null;
        };
    }

}
