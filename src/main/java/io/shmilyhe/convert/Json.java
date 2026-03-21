package io.shmilyhe.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.shmilyhe.convert.api.IGet;
import io.shmilyhe.convert.api.IRemove;
import io.shmilyhe.convert.api.ISet;
import io.shmilyhe.convert.impl.Getter;
import io.shmilyhe.convert.impl.Remove;
import io.shmilyhe.convert.impl.Setter;
import io.shmilyhe.convert.tools.JsonString;
import io.shmilyhe.convert.tools.MapToEntityConverter;
import io.shmilyhe.convert.tools.SimpleJson;
import io.shmilyhe.convert.tools.StringValue;
/**
 * JSON 
 */
public class Json {
    protected Object raw=new HashMap();
    protected Map<String,IGet> getMap= new HashMap<String,IGet>();
    protected Map<String,ISet> setMap= new HashMap<String,ISet>();
    protected Map<String,IRemove> mMap= new HashMap<String,IRemove>();
    boolean array;

    /**
     * parse json-string
     * @param json json-string
     * @return Json 
     */
    public static Json parse(String json){
        SimpleJson sj = SimpleJson.parse(json);
        Json j = new Json();
        j.raw=sj.getRoot();
        return j;
    }

    /**
     * wrap Object to raw data;
     * @param o object
     */
    public void wrap(Object o){
        raw=o;
        array=isArray(o)||isCollection(o);
    }


    /**
     * set json value
     * @param path property path
     * @param value value
     */
    public void set(String path,Object value){
        ISet s =setMap.get(path);
        if(s==null){
            s= new Setter(path);
            setMap.put(path, s);
        }
        s.set(raw, value);
    }    

    /**
     * remove json property
     * @param path path
     */
    public void remove(String path){
        IRemove s =mMap.get(path);
        if(s==null){
            s= new Remove(path);
            mMap.put(path, s);
        }
        s.remove(raw);
    }

    /**
     * get raw object
     * @return raw
     */
    public Object getRaw(){return raw;}

    /**
     * query value
     * @param path path
     * @return Json
     */
    public Json Q(String path){
        IGet g =getMap.get(path);
        if(g==null){
            g= new Getter(path);
            getMap.put(path, g);
        }
        Object o=g.get(raw,null);
        Json j = new Json();
        j.wrap(o);
        return j;
    }

    /**
     * read intValue
     * @return int value
     */
    public Integer asInt(){
        if(raw==null)return null;
        if(raw instanceof Integer)return (Integer)raw;
        if(raw instanceof Boolean)return (Boolean)raw?1:0;
        if(raw instanceof Date)return (int)((Date)raw).getTime();
        if(raw instanceof Number)return ((Number)raw).intValue();
        if(raw instanceof String)return Integer.parseInt((String)raw);
        return null;
    }

    /**
     * read doubleValue
     * @return doubleValue
     */
    public Double asDouble(){
        if(raw==null)return null;
        if(raw instanceof Boolean)return (double)((Boolean)raw?1:0);
        if(raw instanceof Date)return (double)((Date)raw).getTime();
        if(raw instanceof Number)return ((Number)raw).doubleValue();
        if(raw instanceof String)return Double.valueOf((String)raw);
        return null;
    }

    /**
     * read as Date
     * @return date
     */
    public Date asDate(){
        if(raw==null)return null;
        if(raw instanceof Boolean)return null;
        if(raw instanceof Date)return (Date)raw;
        if(raw instanceof Number)return new Date(((Number)raw).longValue());
        if(raw instanceof String)return StringValue.toDate((String)raw);
        return null;
    }

    /**
     * read bigIntValue
     * @return bigIntValue
     */
    public Long asLong(){
        if(raw==null)return null;
        if(raw instanceof Boolean)return (long)((Boolean)raw?1:0);
        if(raw instanceof Date)return (long)((Date)raw).getTime();
        if(raw instanceof Number)return ((Number)raw).longValue();
        if(raw instanceof String)return Long.valueOf((String)raw);
        return null;
    }

    /**
     *  read booleanValue
     * @return booleanValue
     */
    public Boolean asBoolean(){
        if(raw==null)return null;
        if(raw instanceof Boolean)return (Boolean)raw;
        if(raw instanceof Number)return ((Number)raw).intValue()>0;
        if(raw instanceof String)return "true".equalsIgnoreCase((String)raw);
        return null;
    }

    /**
     * read string
     * @return string
     */
    public String asString(){
        if(raw==null)return null;
        if(raw instanceof Boolean)return raw.toString();
        if(raw instanceof Date)return raw.toString();
        if(raw instanceof Number)return raw.toString();
        if(raw instanceof String)return  raw.toString();
        return toString();
    }

    /**
     * 数据作为list
     * @return jsonList
     */
    public List<Json> asList(){
        if(!array)return null;
        List<Json> jlist = new ArrayList<Json>();
        if(isArray(raw)){
            Object[] oa =(Object[]) raw;
            for(Object o:oa){
                Json j = new Json();
                j.wrap(o);
                jlist.add(j);
            }
        }else if(isCollection(raw)){
            for(Object o:(Collection)raw){
                Json j = new Json();
                j.wrap(o);
                jlist.add(j);
            }
        }
        return jlist;
    }

    /**
     * read as Bean
     * @param <T> type
     * @param t class
     * @return Bean
     */
    public <T> T asBean(Class<T> t){
        try {
            //return (T) JBean.mapToBean((Map)raw, t.newInstance());
           return (T) MapToEntityConverter.mapToEntity((Map)raw, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json to string
     */
    public String toString(){
        return JsonString.asJsonString(raw);
    }

    private static boolean isArray(Object o){
		if(o==null)return false;
		return o.getClass().isArray();
	}
    private static boolean isCollection(Object o){
        if (o instanceof Collection){return true;}
        return false;
    }

    /**
     * convert object to jsonString
     * @param obj object
     * @return jsonString
     */
    public static String asJsonString(Object obj){
        return JsonString.asJsonString(obj);
    }
}
