package io.shmilyhe.convert.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.shmilyhe.convert.log.Log;
import io.shmilyhe.convert.log.api.Logger;
import io.shmilyhe.convert.tools.DEBUG;
import io.shmilyhe.convert.tools.ExpEnv;

/**
 * 遍历
 */
public class EachConvertor extends ComplexConvertor {
    static Logger log = Log.getLogger(EachConvertor.class);

    Getter get ;
    Setter set ;
    public EachConvertor(String exp){
        DEBUG.debug("each ===",exp);
        String path=removeRootString(exp);
        get= new Getter(path);
        get.setVar(!exp.startsWith("."));
        set= new Setter(path);
        set.setVar(!exp.startsWith("."));
    }

    @Override
    public Object convert(Object root,ExpEnv env) {
        log.debug("start each:{}", getName());
        //DEBUG.debug("========start:",this.getName(),"========"); 
        ExpEnv p=env;
        Object setroot=root;
        if(set.isVar())setroot=env;
        DEBUG.debug(setroot.getClass());
        env= new ExpEnv(p);
         Object data =get.get(root,env);
         //DEBUG.debug("get:",data," is Var :",get.isVar());
         if(data==null)return root;
        if (data instanceof Collection){
            Collection els =  (Collection)data;
            ArrayList ndata= new ArrayList<>();
            for(Object el:els){
                Object r=each(el,env);
                if(r!=null)ndata.add(r);
            }
            set.set(setroot, ndata);
        }else if (isArray(data)){  
            List d=new ArrayList<>();
            if (data instanceof byte[]) {
                byte[] els =  (byte[])data;
                for(int i=0;i<els.length;i++){
                    d.add(each(els[i],env));
                }
            } else {
                Object[] els =  (Object[])data;
                for(int i=0;i<els.length;i++){
                    d.add(each(els[i],env));
                }
            }
        set.set(setroot, d); 
        }
        //DEBUG.debug("========end:",this.getName(),"========"); 
        log.debug("end each:{}" ,getName());
         return root;
    }

    protected Object each(Object o,ExpEnv env){
        try{
            return super.convert(o,env);
        }catch(Exception e){
            e.printStackTrace();
        }
        return o;
    }

        /**
     * 移除表达式的 “.”
     * @param s
     * @return
     */
    private String removeRootString(String s){
        if(s==null)return null;
        s=s.trim();
        if(s.startsWith("."))return s.substring(1);
        return s;
    }

    private static boolean isArray(Object o){
		if(o==null)return false;
		return o.getClass().isArray();
	}





    
    
}
