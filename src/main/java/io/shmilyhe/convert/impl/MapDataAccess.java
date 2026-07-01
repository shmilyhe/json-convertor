package io.shmilyhe.convert.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.shmilyhe.convert.tools.ReflectionUtils;


public class MapDataAccess extends BaseDataAccess{
    private boolean array;
    private String key;

    public MapDataAccess(String key,boolean array){
        this.key=key;
        this.array=array;
    }

    @Override
    public boolean set(Object v, Object da) {
        //System.out.println("set:"+key+"|"+da+"|"+v);
        if(da instanceof Map){
            ((Map)da).put(key, v);
            return true;
        }else{
            return ReflectionUtils.set(da, key, v);
        }
        //return false;
    }

    @Override
    public Object get(Object da) {
        //System.out.println("get:"+key+"|"+((Map)da).get(key));
        if(da instanceof Map){
           return  ((Map)da).get(key);
        }else{
            return ReflectionUtils.get(da,key);
        }
        //return null;
    }

    @Override
    public Object create() {
        if(array)return new ArrayList();
        return new HashMap();
    }
    public String toString(){
        return (array?"array:": "object:")+key;
    }

    @Override
    public Object remove(Object da) {
        if(da instanceof Map){
              ((Map)da).remove(key);
         }
         return da;
    }
}