package io.shmilyhe.convert;

import java.util.List;

import io.shmilyhe.convert.api.IConvertor;
import io.shmilyhe.convert.api.IGet;
import io.shmilyhe.convert.ast.expression.AssignmentExpression;

import io.shmilyhe.convert.ast.expression.CallExpression;
import io.shmilyhe.convert.ast.expression.Expression;
import io.shmilyhe.convert.ast.expression.Identifier;

import io.shmilyhe.convert.ast.parser.VRLParser;
import io.shmilyhe.convert.ast.statement.EachStatement;
import io.shmilyhe.convert.ast.statement.ExpressionStatement;
import io.shmilyhe.convert.ast.statement.IfStatement;
import io.shmilyhe.convert.ast.statement.Statement;

import io.shmilyhe.convert.impl.BaseConvertor;
import io.shmilyhe.convert.impl.ComplexConvertor;

import io.shmilyhe.convert.impl.EachConvertor;
import io.shmilyhe.convert.impl.IfConvertor;
import io.shmilyhe.convert.impl.Setter;
import io.shmilyhe.convert.system.SystemFunction;
import io.shmilyhe.convert.tools.DEBUG;

public class AstConvertorFactory {
    SystemFunction systemfunction=new SystemFunction();
    public IConvertor getConvertor(String commands){
        Statement stat =parse(commands);
        BaseConvertor convertor = new ComplexConvertor().setName("root");
        for(Statement s :stat.getBody()){
            getConvertor(s,convertor);
        }
        return convertor;
    }

    private Statement parse(String commands){
        VRLParser vrl = new VRLParser();
        Statement stat = vrl.parse(commands);
        stat.clearParent();
        //System.out.println("=========================================================");
        //System.out.println(Json.asJsonString(stat));
        //System.out.println("=========================================================");
        return stat;
    }

    private void getConvertor(Statement stat,BaseConvertor parent){
        //System.out.println("type:"+stat.getType());
        if(stat.isCallee()){
         parent.addConvertor(calleeStatement(stat));
        }else if(stat.isExpression()){

         parent.addConvertor(expStatement(stat));
        }else if(stat.isEach()){
         parent.addConvertor(eachStatement(stat));
        }else if(stat.isBlock()){
         parent.addConvertor(blockStatement(stat));
        }else if(stat.isIf()){
         parent.addConvertor(ifStatement(stat));
        }
    }

    private IConvertor calleeStatement(Statement stat){
       
        ExpressionStatement estat = (ExpressionStatement)stat;
        CallExpression callee=(CallExpression)estat.getExperssion();
        String fname=((Identifier)callee.getCallee()).getName();
        List<Expression> args = callee.getArguments();
         //System.out.println("call:"+fname+" args:"+args.size());
        return systemfunction.func(fname, args,estat.getLine());
    }

    private IConvertor expStatement(Statement stat){
        //System.out.println("exp:"+stat.getType());
        ExpressionStatement es = (ExpressionStatement)stat;
        if(es.getExperssion()==null||!es.getExperssion().isAssignment()){
            return null;
        }
        AssignmentExpression ae =(AssignmentExpression)es.getExperssion();
        String a=((Identifier)ae.getLeft()).getName();
        final IGet get = getExp(ae.getRight());
        if(".".equals(a)){
            return (data,env)->{ return get.get(data,env);};
        }
        final Setter set =new Setter(SystemFunction.removeRootString(a));
        set.setVar(!a.startsWith("."));
        return (data,env)->{ 
            set.set(set.isVar()?env:data, get.get(data,env));
            return data;
        };

    }


    private IGet getExp(Expression exp){
        return SystemFunction.getExp(exp);
    }
    private IConvertor ifStatement(Statement stat){
        //System.out.println("if:"+stat.getType());
        IfStatement is = (IfStatement)stat;
        IfConvertor bc=(IfConvertor) new IfConvertor(this.getExp(is.getTest())).setName("if");
        if(is.getBody()!=null){
            for(Statement s :is.getBody()){
                getConvertor(s,bc);
            }
        }
        if(is.getAlternate()!=null){
           Statement alter =  is.getAlternate();
           if(alter.isIf()){
            bc.setAlternate(ifStatement(alter));
           }else if(alter.isEach()){
            bc.setAlternate(eachStatement(alter));
           }else if(alter.isBlock()){
            bc.setAlternate(blockStatement(alter));
           }

        }
        return bc;
    }

    private IConvertor eachStatement(Statement stat){
        //System.out.println("each:"+stat.getType());
        EachStatement es =(EachStatement)stat;
        Identifier id =(Identifier)es.getTarget();
        BaseConvertor eac =new EachConvertor(id.getName()).setName("each");
        if(es.getBody()!=null){
            for(Statement s :stat.getBody()){
                getConvertor(s,eac);
            }
        }
        return eac;
    }

    private IConvertor blockStatement(Statement stat){
        //System.out.println("block:"+stat.getType());
        BaseConvertor block = new ComplexConvertor().setName("block");
        for(Statement s :stat.getBody()){
            getConvertor(s,block);
        }
        return block;
    }


    
}