package io.shmilyhe.convert.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.shmilyhe.convert.api.IGet;
import io.shmilyhe.convert.log.Log;
import io.shmilyhe.convert.log.api.Logger;
import io.shmilyhe.convert.tools.ExpEnv;

public class ConstantGetter implements IGet {
    static Logger log = Log.getLogger(ConstantGetter.class);

    private Object v;

    public ConstantGetter(){

    }

    public ConstantGetter setValue(Object o){
        v=o;
        return this;
    }

    public ConstantGetter (String s){
        v=valueOf(s);
    }

    @Override
    public Object get(Object data,ExpEnv evn) {
        return v;
    }

    Pattern pd =Pattern.compile("-?[123456789]\\.[0123456789]*");
    Pattern pl =Pattern.compile("-?[123456789][0123456789]*|0");
    
    private Object valueOf(String v){
		if(v==null||v.trim().length()==0||"null".equals(v))return null;
		v=v.trim();
        if(v.matches("[\"''].*[\"\']"))return v.substring(1,v.length()-1);
		if("true".equalsIgnoreCase(v))return Boolean.TRUE;
		if("false".equalsIgnoreCase(v))return Boolean.FALSE;
		if(isFloat(v)){
			return Double.parseDouble(v);
		}
        if(isInt(v)){
			return Long.parseLong(v);
		}
		return v;
	}
    private boolean isInt(String s){
        return pl.matcher(s).matches();
    }

    private boolean isFloat(String s){
        return pd.matcher(s).matches();
    }

    public String toString(){
        return "constant:"+v;
    }
    

}
