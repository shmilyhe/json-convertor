package io.shmilyhe.convert.impl;

import java.math.BigDecimal;

import io.shmilyhe.convert.api.IGet;
import io.shmilyhe.convert.log.Log;
import io.shmilyhe.convert.log.api.Logger;
import io.shmilyhe.convert.tools.ExpEnv;

/**
 * 算式计算器
 * 
 */
public class ExpGeter implements IGet{
    static Logger log = Log.getLogger(ExpGeter.class);
    static int TYPE_INT=0;
    static int TYPE_STRING=1;
    static int TYPE_FLOAT=2;
    static int TYPE_BOOLEAN=3;
    static int TYPE_DATE=4;
    static final char[] OPS = {'=', '>', '<', '+', '-', '*', '/', '%', '!', '&', '|'};

    private boolean minus;

    public boolean isMinus() {
        return minus;
    }

    public ExpGeter setMinus(boolean minus) {
        this.minus = minus;
        return this;
    }

    //表达式
    private String expression;
   //参数 代码的行号
    private Integer line;

    //参数1
    private  IGet p1;
    //参数2
    private IGet p2;

    private OperatorType operator;
    public ExpGeter(IGet g1,IGet g2,OperatorType operator){
        p1=g1;
        p2=g2;
        this.operator=operator;
    }

    @Override
    public Object get(Object data,ExpEnv env) {
        Object param1=p1.get(data,env);
        Object param2=p2.get(data,env);
        //if(p1==null)DEBUG.debug("==================================");
        //DEBUG.debug(expression,"?????:",p1.getClass(),p1,operator,p2);
        Object res=null;
        switch(operator){
            case ADD:
            res= add(param1,param2);
            break;
            case SUB:
            res= sub(param1,param2);
            break;
            case DIV:
            res= div(param1,param2);
            break;
            case MULT:
            res= mult(param1,param2);
            break;
            case MOD:
            res= mod(param1,param2);
            break;
            case OR:
            res= or(param1, param2);
            break;
            case AND:
            res= and(param1, param2);
            break;
            case LT:
            res= lt(param1, param2);
            break;
            case GT:
            res= gt(param1, param2);
            break;
            case GE:
            res= ge(param1, param2);
            break;
            case LE:
            res= le(param1, param2);
            break;
            case EQ:
            res= eq(param1, param2);
            break;
            case NEQ:
            res= neq(param1, param2);
            break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
            case U_SHIFT_RIGHT:
            res=shift(param1,getInteger(param2),operator);
            break;
            case BIT_AND:
            case BIT_OR:
            case BIT_XOR:
            res=bitOp(param1, param2, operator);
            break;
        }
        if(minus&&(
            operator.equals(OperatorType.ADD)
            ||operator.equals(OperatorType.DIV)
            ||operator.equals(OperatorType.MULT)
            ||operator.equals(OperatorType.MOD)
            ||operator.equals(OperatorType.SUB)
        )){
            res=mult(-1,res); 
        }else if(minus&&(res instanceof Boolean)
        ){
            res= !((Boolean)res);
        }
        //DEBUG.debug("cal:",param1," ",operator," ",param2,"=",res);
        log.debug("exp: {} {} {} = {} ", param1,operator,param2,res);
        return res;
    }

    public static int getInteger(Object o){
        if(o==null)return 0;
        if(o instanceof Integer)return (Integer)o;
        if(o instanceof Short)return ((Short)o).intValue();
        if(o instanceof Long)return ((Long)o).intValue();
        if(o instanceof String){
            try{
                return Integer.parseInt(o.toString());
            }catch(Exception e){
                return 0;
            }
        }
        return 0;
    }
    public static byte getByte(Object o){
        if(o==null)return 0;
        if(o instanceof Integer)return ((Integer)o).byteValue();
        if(o instanceof Short)return ((Short)o).byteValue();
        if(o instanceof Long)return ((Long)o).byteValue();
        if(o instanceof String){
            try{
                return Byte.valueOf(o.toString());
            }catch(Exception e){
                return 0;
            }
        }
        return 0;
    }
    public static long getLong(Object o){
        if(o==null)return 0;
        if(o instanceof Integer)return ((Integer)o).longValue();
        if(o instanceof Short)return ((Short)o).longValue();
        if(o instanceof Long)return ((Long)o).longValue();
        if(o instanceof String){
            try{
                return Long.valueOf(o.toString());
            }catch(Exception e){
                return 0;
            }
        }
        return 0;
    }

    public static Object bitOp(Object num1, Object num2,OperatorType op){
        if(op.equals(OperatorType.BIT_OR)){
            if(num1==null&&num2!=null)return num2;
            if(num1 instanceof String )return num1;
        }
        
        if(num1==null||num2==null)return 0;
        if(num1 instanceof Long||num2 instanceof Long){
            long n1=getLong(num1);
            long n2=getLong(num2);
            switch (op) {
                case BIT_AND:
                    return n1&n2;
                case BIT_OR:
                    return n1|n2;
                case BIT_XOR:
                    return n1^n2;       
                default:
                    return 0;
            }
        }
        if(num1 instanceof Integer||num2 instanceof Integer){
            long n1=getInteger(num1);
            long n2=getInteger(num2);
            switch (op) {
                case BIT_AND:
                    return n1&n2;
                case BIT_OR:
                    return n1|n2;
                case BIT_XOR:
                    return n1^n2;       
                default:
                    return 0;
            }
        }
        if(num1 instanceof Byte||num2 instanceof Byte){
            long n1=getByte(num1);
            long n2=getByte(num2);
            switch (op) {
                case BIT_AND:
                    return n1&n2;
                case BIT_OR:
                    return n1|n2;
                case BIT_XOR:
                    return n1^n2;       
                default:
                    return 0;
            }
        }
        return 0;
    }

    public static Boolean eq(Object num1, Object num2){
        if(num1==null&& num2==null)return true;
        if(num1==null||num2==null)return false;
        if(num1 instanceof Number && num2 instanceof Number){
            Number n1=(Number) num1;
            Number n2=(Number) num2;
            if(num1 instanceof Double||num2 instanceof Double){
                return n1.doubleValue()==n2.doubleValue();
            }
            if(num1 instanceof Float||num2 instanceof Float){
                return n1.floatValue()==n2.floatValue();
            }
            if(num1 instanceof Long||num2 instanceof Long){
                return n1.longValue()==n2.longValue();
            }
            if(num1 instanceof Integer||num2 instanceof Integer){
                return n1.intValue()==n2.intValue();
            }
            if(num1 instanceof Short||num2 instanceof Short){
                return n1.shortValue()==n2.shortValue();
            }
        }
        if(num1!=null){
            return num1.equals(num2);
        }
        return false;
    }

    public static Boolean neq(Object num1, Object num2){
        return !eq(num1,num2);
    }

    public static Boolean and(Object num1, Object num2){
        if(num1==null)num1=false;
        if(num2==null)num2=false;
        if(num1 instanceof Boolean && num2 instanceof Boolean){
            return (Boolean)num1&&(Boolean)num2;
        }
        return false;
    }

    public static Boolean or(Object num1, Object num2){
        if(num1==null)num1=false;
        if(num2==null)num2=false;
        if(num1 instanceof Boolean && num2 instanceof Boolean){
            return (Boolean)num1||(Boolean)num2;
        }
        return false;
    }

    public static Boolean lt(Object num1, Object num2){
        if(!isNumber(num1)||!isNumber(num2))return false;
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() < n2.longValue();
        } else {
            return n1.doubleValue() < n2.doubleValue();
        }
    }

     public static Boolean gt(Object num1, Object num2){
        if(!isNumber(num1)||!isNumber(num2))return false;
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() > n2.longValue();
        } else {
            return n1.doubleValue() > n2.doubleValue();
        }
    }

    public static Boolean le(Object num1, Object num2){
        if(!isNumber(num1)||!isNumber(num2))return false;
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() <= n2.longValue();
        } else {
            return n1.doubleValue() <= n2.doubleValue();
        }
    }

     public static Boolean ge(Object num1, Object num2){
        if(!isNumber(num1)||!isNumber(num2))return false;
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() >= n2.longValue();
        } else {
            return n1.doubleValue() >= n2.doubleValue();
        }
    }

    public static Object add(Object num1, Object num2) {
        if(num1==null)num1="null";
        if(num2==null)num2="null";
        if(isString(num1)||isString(num1)||!isNumber(num1)||!isNumber(num2)){
            return new StringBuilder().append(num1).append(num2).toString();
        }
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() + n2.longValue();
        } else {
            return n1.doubleValue() + n2.doubleValue();
        }
    }


    public static Object div(Object num1, Object num2) {
        if(!isNumber(num1)||!isNumber(num2)){
            return "NaN";
        }
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if(n2.doubleValue()==0)return "NaN";
        return n1.doubleValue() / n2.doubleValue();
        
    }

    public static Object sub(Object num1, Object num2) {
        if(!isNumber(num1)||!isNumber(num2)){
            return "NaN";
        }
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() - n2.longValue();
        } else {
            return n1.doubleValue()-n2.doubleValue();
        }
    }

     public static Object mult(Object num1, Object num2) {
        if(!isNumber(num1)||!isNumber(num2)){
            return "NaN";
        }
        Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue() * n2.longValue();
        } else {
            BigDecimal result = new BigDecimal(n1.doubleValue()).multiply(new BigDecimal(n2.doubleValue()));
            return result.doubleValue();
        }
    }
    public static Object mod(Object num1, Object num2) {
         if(!isNumber(num1)||!isNumber(num2)){
            return "NaN";
        }
         Number n1=(Number)num1;
        Number n2=(Number)num2;
        if (isInteger(n1) && isInteger(n2)) {
            return n1.longValue()%n2.longValue();
        } else {
            return "NaN";
        }
    }

    public static Object shift(Object num1, int num2,OperatorType op){
        if(num1==null)return 0;
        if(num1 instanceof Byte){
            switch(op){
                case SHIFT_LEFT:
                return (Byte)num1<<num2;
                case SHIFT_RIGHT:
                return (Byte)num1>>num2;
                case U_SHIFT_RIGHT:
                return (Byte)num1>>>num2;
                default :return 0;
            }
        }
        if(num1 instanceof Integer){
            switch(op){
                case SHIFT_LEFT:
                return (Integer)num1<<num2;
                case SHIFT_RIGHT:
                return (Integer)num1>>num2;
                case U_SHIFT_RIGHT:
                return (Integer)num1>>>num2;
                default :return 0;
            }
        }
        if(num1 instanceof Long){
            switch(op){
                case SHIFT_LEFT:
                return (Long)num1<<num2;
                case SHIFT_RIGHT:
                return (Long)num1>>num2;
                case U_SHIFT_RIGHT:
                return (Long)num1>>>num2;
                default :return 0;
            }
        }
        return 0;
    }

    private static boolean isInteger(Number num) {
        return num instanceof Integer || num instanceof Long || num instanceof Short || num instanceof Byte;
    }
     private static boolean isNumber(Object num) {
        if(num instanceof Number){
            return true;
        }
        return false;
     }
    private static boolean isString(Object o){
        if(o==null)return false;
        if(o instanceof String){
            return true;
        }
        return false;
    }

    public Integer getLine() {
        return line;
    }

    public ExpGeter setLine(Integer line) {
        this.line = line;
        return this;
    }
     public String getExpression() {
        return expression;
    }

    public ExpGeter setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    @Override
    public String toString() {
        if(line!=null) return "line:"+line+"\t"+expression;
        return expression;
    }

    
    
}

