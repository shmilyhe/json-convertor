package io.shmilyhe.convert;

import java.util.HashMap;
import java.util.Map;

import io.shmilyhe.convert.ast.parser.DefineJsonParser;
import io.shmilyhe.convert.tools.ExpEnv;
import io.shmilyhe.convert.tools.SimpleJson;

/**
 * VRL 执行器
 * VRLExecutor
 */
public class VRLExecutor extends JsonConvertor {
    private String initJson;
    
    public VRLExecutor(String command) {
        super(command);
        initJson=DefineJsonParser.findDefineJsonLine(command);
    }

    public VRLExecutor(String[] commands) {
        super(commands);
        if(commands!=null)
        for(String cmd:commands){
            if(initJson!=null)break;
            if(cmd!=null){
                initJson=DefineJsonParser.findDefineJsonLine(cmd);
            }
        }
    }

    /**
     * 执行脚本
     * @param env Environment
     * @return 执行后的结果
     */
    public Object execute(ExpEnv env){
            if(convertor==null)return null;
            if(env==null){env= new ExpEnv(null);}
            Map jdata=null;
            if(initJson!=null){
                SimpleJson sj = SimpleJson.parse(initJson);
                jdata=(Map) sj.getRoot();
            }else{
                jdata=new HashMap<>();
            }
            convertor.convert(jdata,env);
            return jdata;
    }


    
}
