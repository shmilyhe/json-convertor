package io.shmilyhe.convert.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.shmilyhe.convert.api.IConvertor;
import io.shmilyhe.convert.api.IGet;
import io.shmilyhe.convert.ast.expression.BinaryExpression;
import io.shmilyhe.convert.ast.expression.CallExpression;
import io.shmilyhe.convert.ast.expression.Expression;
import io.shmilyhe.convert.ast.expression.Identifier;
import io.shmilyhe.convert.ast.expression.Literal;
import io.shmilyhe.convert.ast.expression.SequenceExpression;
import io.shmilyhe.convert.callee.Callee;
import io.shmilyhe.convert.impl.ArrayGeter;
import io.shmilyhe.convert.impl.ConstantGetter;
import io.shmilyhe.convert.impl.ExpGeter;
import io.shmilyhe.convert.impl.Getter;
import io.shmilyhe.convert.impl.OperatorType;
import io.shmilyhe.convert.impl.Remove;
import io.shmilyhe.convert.impl.SelfGetter;
import io.shmilyhe.convert.impl.Setter;
import io.shmilyhe.convert.log.Log;
import io.shmilyhe.convert.log.api.Logger;
import io.shmilyhe.convert.tools.DEBUG;

/**
 * 系统的内置方法
 */
public class SystemFunction {
    static Logger log = Log.getLogger(SystemFunction.class);
    public static String removeRootString(String s){
        if(s==null)return null;
        s=s.trim();
        if(s.startsWith("."))return s.substring(1);
        return s;
    }


    /**
     * 表达式转成执行器
     * @param exp
     * @return
     */
    public static IGet getExp(Expression exp){
        String type=exp.getType();
        if(Expression.TYPE_ID.equals(type)){
            String a =((Identifier)exp).getName();
            IGet g = null;
            if(".".equals(a)){
                g= new SelfGetter(".");
            }else{
                g=new Getter(SystemFunction.removeRootString(a)).setMinus(exp.isMinus())
                .setVar(!a.startsWith("."));
            }
            return g;
        }else if(Expression.TYPE_LIT.equals(type)){
            Object a =((Literal)exp).getValue();
            IGet g = new ConstantGetter().setValue(a);
            return g;
        }else if(Expression.TYPE_CALL.equals(type)){
            CallExpression ce =(CallExpression)exp;
            List<IGet> args = new ArrayList<>();
            List<Expression> eps =ce.getArguments();
            if(eps!=null)for(Expression ex:eps){
                args .add(getExp(ex));
            }
            String name =((Identifier)ce.getCallee()).getName();
            Callee callee = new Callee(name,args).setMinus(exp.isMinus());
            return callee;
        }else if(Expression.TYPE_BIN.equals(type)){
            BinaryExpression be = (BinaryExpression)exp;
            IGet left=getExp(be.getLeft());
            IGet right=getExp(be.getRight());
            //System.out.println("getOperater:"+be.getOperater());
            OperatorType oper =OperatorType.find(be.getOperater());
            return new ExpGeter(left,right,oper).setMinus(be.isMinus());
        }else if(Expression.TYPE_ARRAY.equals(type)){
            SequenceExpression be = (SequenceExpression)exp;
            ArrayGeter ag = new ArrayGeter();
            List<Expression> es = be.getExpressions();
            if(es!=null)
            for(Expression e:es){
                ag.addGeter(getExp(e));
            }
            return ag;
        }
        return null;
    }


    /**
     * 取反
     * @param o
     * @return
     */
    public static Object revert(Object o){
        if(o instanceof Boolean){
            return !(Boolean)o;  
        }
        if(o instanceof Integer){
            return -(Integer)o;  
        }
        if(o instanceof Long){
            return -(Long)o;  
        }
        if(o instanceof Float){
            return -(Float)o;  
        }
        if(o instanceof Double){
            return -(Double)o;  
        }
        return o;
    }

    /**
     * 内置的方法
     * @param name 方法名
     * @param args 参数
     * @param line 脚本行数
     * @return  执行器
     */
    public IConvertor func(String name,List<Expression>  args,int line){
        String f=name;
        int argCount=0;
        if(args!=null){
            argCount = args.size();
        }
        if("set".equals(f.trim())){
            Identifier a1=(Identifier)args.get(0);
            Expression a2=args.get(1);
            if(argCount !=2 ){throw  new RuntimeException("syntax error:Invaild argument  "+f+" at line:"+line);}
            //final IGet get =str[2].startsWith(".")?new Getter(removeRootString(str[2])):new ConstantGetter(str[2]);
            final IGet get =getExp(a2);//ExpCalculate.getExpression(str[2], line);
            final Setter set = new Setter(removeRootString(a1.getName()));
            set.setVar(!a1.getName().startsWith("."));
            
            return (data,env)->{ 
                Object d = get.get(data,env);
                set.set(set.isVar()?env:data,d);
                return data;
            };
        }else if("move".equals(f.trim())){
            Expression ar1=args.get(0);
            if(ar1 instanceof Literal){
                System.out.println("Literal:"+((Literal)ar1).getRaw());
            }
            String gStr=((Identifier)args.get(0)).getName();
            String dest=((Identifier)args.get(1)).getName();
            if(argCount !=2
            ||!gStr.startsWith(".")
            ||!dest.startsWith(".")){throw  new RuntimeException("syntax error(move): at line:"+line+" near :"+name+"("+gStr+","+dest+") paramter must start with '.' ");}
            
            if(".".equals(gStr)){
                final Setter set = new Setter(removeRootString(dest));
                final SelfGetter get = new SelfGetter(".");
                return (data,env)->{
                    HashMap m= new HashMap(); set.set(m, get.get(data,env));
                    return m;};
            }
            final Getter get = new Getter(removeRootString(gStr));
            final Setter set = new Setter(removeRootString(dest));
            final Remove remove= new Remove(removeRootString(gStr));
            return (data,env)->{ 
                set.set(data, get.get(data,env));remove.remove(data);
                return data;
                
            };
        }else if("del".equals(f.trim())||"remove".equals(f.trim())){
            if(argCount!=1)throw  new RuntimeException("syntax error(del) at line:"+line);
            String gStr=((Identifier)args.get(0)).getName();
            if(!gStr.startsWith(".")){
                throw  new RuntimeException("syntax error invaild argument (del): at line:"+line);
            }
            if(".".equals(gStr)){
                return (data,env)->{return null;};
            }
            final Remove remove= new Remove(removeRootString(gStr));
            return (data,env)->{remove.remove(data);return data;};
        }else if("setNotExists".equals(f.trim())){
            Identifier a1=(Identifier)args.get(0);
            Expression a2=args.get(1);
            if(argCount !=2 ){throw  new RuntimeException("syntax error:Invaild argument  "+f+" at line:"+line);}
            //final IGet get =str[2].startsWith(".")?new Getter(removeRootString(str[2])):new ConstantGetter(str[2]);
            final IGet get =getExp(a2);//ExpCalculate.getExpression(str[2], line);
            final Getter old =new Getter(removeRootString(a1.getName()));
            final Setter set = new Setter(removeRootString(a1.getName()));
            set.setVar(!a1.getName().startsWith("."));
            old.setVar(!a1.getName().startsWith("."));
            return (data,env)->{ 
                Object oldvalue=old.get(data,env);
                if(oldvalue==null||"".equals(oldvalue)){
                    set.set(data, get.get(data,env));
                }  
                return data; 
            };
        }else if("print".equals(f.trim())){
            if(argCount !=1){throw  new RuntimeException("syntax error:print  at line:"+line);}
            //final IGet get =str[2].startsWith(".")?new Getter(removeRootString(str[2])):new ConstantGetter(str[2]);
            final IGet get =getExp(args.get(0));
            return (data,env)->{ 
                Object oldvalue=get.get(data,env);
                //System.out.println("[print]:"+oldvalue); 
                log.info("console:{}",oldvalue);
                return data; 
            };
        }else if("namespace".equalsIgnoreCase(f.trim())){
            if(argCount !=1){throw  new RuntimeException("syntax error:namespace at line:"+line);}
            final IGet get =getExp(args.get(0));
            return (data,env)->{ 
                Object v=get.get(data,env);
                if(env!=null)
                env.nameSpace(String.valueOf(v));
                return data; 
            };
        }else if("global".equals(f.trim())){
            if(argCount !=2){throw  new RuntimeException("syntax error:global at line:"+line);}
            //final IGet get =ExpCalculate.getExpression(str[1], line);
            final IGet get2 =getExp(args.get(1));//ExpCalculate.getExpression(str[2], line);
            final String key=((Identifier)args.get(0)).getName();
            return (data,env)->{ 
                Object v=get2.get(data,env);
                if(env!=null)env.global(key,v);
                return data; 
            };
        }else if("exit".equals(f.trim())){
            if(argCount>0){
                return (data,env)->{
                    env.exit(((Literal)args.get(0)).getRaw());
                    DEBUG.debug("exit at line ",line);
                    return data; 
                };
            }else{
                return (data,env)->{
                    env.exit();
                    DEBUG.debug("exit at line ",line);
                    return data; 
                };
            }
        }
        
        List<IGet> as = new ArrayList<>();
        if(args!=null){
            for(Expression ex:args){
                as.add(getExp(ex));
            }
        }
        final Callee callee = new Callee(name,as);
        return (data,env)->{
            callee.get(data, env);
            return data;
        };
    }
    
}
