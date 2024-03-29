package io.shmilyhe.convert.impl;

import java.util.ArrayList;
import java.util.List;

import io.shmilyhe.convert.api.IConvertor;
import io.shmilyhe.convert.api.IGet;
import io.shmilyhe.convert.log.Log;
import io.shmilyhe.convert.log.api.Logger;
import io.shmilyhe.convert.tools.DEBUG;
import io.shmilyhe.convert.tools.ExpCalculate;
import io.shmilyhe.convert.tools.ExpEnv;

/**
 * 分支计算
 */
public class IfConvertor extends ComplexConvertor{
    static Logger log = Log.getLogger(IfConvertor.class);

    protected List<IConvertor> elseList = new ArrayList<IConvertor>();

    String exp;
    IGet cond;

    protected IConvertor alternate;

    public IConvertor getAlternate() {
        return alternate;
    }
    public IfConvertor setAlternate(IConvertor alternate) {
        this.alternate = alternate;
        return this;
    }
    public IfConvertor(IGet exp){
            cond=exp;
    }
    public IfConvertor(String exp){
        this.exp=exp;
        //DEBUG.debug("condexp1:",exp);
        
       cond = ExpCalculate.getExpression(exp);
    }

    @Override
    public Object convert(Object root,ExpEnv env) {
        log.debug("start if:{}", getName());
        //DEBUG.debug("========start:",this.getName(),"========"); 
        if(calCondition(root,env)){
            Object o =super.convert(root,new ExpEnv(env));
            log.debug("end if");
            return o;
        }else{
            //System.out.println("条件不能成"+this);
            //DEBUG.debug("condexp:",exp," is ",false);
            if(alternate!=null){
                log.debug("else");
                Object o=this.alternate.convert(root, env);
                log.debug("end if");
                return o;
            }
            log.debug("end if");
            return root;
        }
        
    }

    
    public Object convertElse(Object root,ExpEnv env) {
        Object o=root;
        for(IConvertor c:clist){
            if(c==null)continue;
            o=c.convert(o,env);
        }
        DEBUG.debug("========end:",this.getName(),"========"); 
        return o;
    }

    public void addElseConvertor(IConvertor c){
        parent(c);
        elseList.add(c);
    }



    /**
     * 计算条件是否成立
     * @param root
     * @return
     */
    protected boolean calCondition(Object root,ExpEnv env){
        if(cond==null)return false;
        //DEBUG.debug("info:",cond,cond.getClass());
        Object o = cond.get(root,env);
        if(o==null)return false;
        if(o instanceof Boolean)return (Boolean)o;
        return false;
    }

    

    
}
