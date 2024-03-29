package io.shmilyhe.convert.system;

import java.nio.charset.Charset;
import java.util.Collection;

import io.shmilyhe.convert.callee.IFunction;
import io.shmilyhe.convert.log.impl.Formater;

public class Strings {
    
    public static IFunction toLowerCase(){
        return (param,env)->{
            try{
                Object o =param.get(0);
                if(o==null)return null;
                String text =null;
                if(o instanceof String){
                    text=(String)o;
                }else {
                    text =String.valueOf(o);
                }
                return text.toLowerCase();
            }catch(Exception e){
                return null;
            }
        };
    }

    public static IFunction toUpperCase(){
        return (param,env)->{
            try{
                Object o =param.get(0);
                if(o==null)return null;
                String text =null;
                if(o instanceof String){
                    text=(String)o;
                }else {
                    text =String.valueOf(o);
                }
                return text.toUpperCase();
            }catch(Exception e){
                return null;
            }
        };
    }

    static Charset utf8=Charset.forName("utf-8");
    public static IFunction string(){
        return (param,env)->{
            try{
                Object o =param.get(0);
                if(o==null)return null;
                String text =null;
                if(o instanceof String){
                    return o;
                }else if(isArray(o)){
                    try{
                        return new String((byte[])o,utf8);
                    }catch(Exception e){
                        return null;
                    }
                }else {
                    return String.valueOf(o);
                }
            }catch(Exception e){
                return null;
            }
        };
    }

    public static IFunction getBytes(){
        return (param,env)->{
            try{
                Object o =param.get(0);
                if(o==null)return null;
                if(o instanceof String){
                    return ((String)o).getBytes(utf8);
                }else {
                    return String.valueOf(o).getBytes(utf8);
                }
            }catch(Exception e){
                return null;
            }
        };
    }

    public static IFunction substring(){
        return (args,env)->{
            String text=null;
            try{
                Object o =args.get(0);
                if(o==null)return o;
                if(o instanceof String){
                    text =(String)o;
                }else{
                    text =String.valueOf(o);
                }
                if(args.size()==2){
                    Integer start =(Integer)args.get(1);
                    if(start==null||start==0)return text;
                    if(start<0){
                        int end=text.length()+start;
                        if(end<0)return "";
                        return text.substring(0, end);

                    }
                    return text.substring(start);
                }else if(args.size()==3){
                    Integer start =(Integer)args.get(1);
                    Integer end =(Integer)args.get(2);
                    if(end==null||end==0)end=text.length();
                    if(end<0)end=text.length()+end;
                    if(end<0||end<=start)return "";
                    if(end>text.length())end=text.length();
                    return text.substring(start,end);
                }
                return text;
            }catch(Exception e){
                return text;
            }
        };
    }

    public static IFunction split(){
        return (args,env)->{
            String text=null;
            try{
                Object o =args.get(0);
                if(o==null)return o;
                if(o instanceof String){
                    text =(String)o;
                }else{
                    text =String.valueOf(o);
                }
                if(text==null)return new String[0];
                if(args.size()>1){
                    String str =(String)args.get(1);
                    if(str==null)return new String[]{text};
                    return text.split(str);
                }
                return  new String[]{text};
            }catch(Exception e){
                return  new String[]{text};
            }
        };
    }

    public static IFunction printf(){
        return (args,env)->{
            String format=null;
            try{
                //Object o =args.get(0);
               
                Object[] ar = new Object[args.size()-1];
                int i=0;
                for(Object o:args){
                    if(i==0){
                        if(o instanceof String){
                            format=(String)o;
                        }else{
                            format=String.valueOf(o);
                        }
                    }else{
                        ar[i-1]=o;
                    }
                    i++;
                }
                return Formater.format(format, ar);

            }catch(Exception e){
                return  format;
            }
        };
    }



    public static IFunction contains(){
        return (args,env)->{
            String text=null;
            try{
                Object o =args.get(0);
                if(o==null)return o;
                if(o instanceof String){
                    text =(String)o;
                }else{
                    text =String.valueOf(o);
                }
                if(text==null)return new String[0];
                if(args.size()>1){
                    String str =(String)args.get(1);
                    return text.contains(str);
                }
                return  false;
            }catch(Exception e){
                return  false;
            }
        };
    }

    public static IFunction join(){
        return (args,env)->{
            if(args==null|args.size()==0)return null;
            Object first=args.get(0);
            Object second=null;
            if(args.size()>1)second=args.get(1);
            if(isArray(first)){
                if(second!=null)return join((Object [])first,String.valueOf(second));
                return join((Object [])first);
            }
            if(first instanceof Collection){
                if(second!=null)return join((Collection)first,String.valueOf(second));
                return join((Collection)first);
            }
            try{
                StringBuilder sb = new StringBuilder();
                if(args!=null)for(Object o:args){
                    sb.append(o);
                }
                return sb.toString();
            }catch(Exception e){
                return null;
            }
        };
    }


    public static String join(Object[] args){
        StringBuilder sb = new StringBuilder();
        if(args!=null)for(Object o:args){
            sb.append(o);
        }
        return sb.toString();
    }

    public static String join(Collection args){
        StringBuilder sb = new StringBuilder();
        if(args!=null)for(Object o:args){
            sb.append(o);
        }
        return sb.toString();
    }

    public static String join(Collection args,String j){
        StringBuilder sb = new StringBuilder();
        boolean first=true;
        if(args!=null)for(Object o:args){
            if(first){
                first=false;
            }else{
                sb.append(j);
            }
            sb.append(o);
        }
        return sb.toString();
    }

    public static String join(Object[] args,String j){
        StringBuilder sb = new StringBuilder();
        boolean first=true;
        if(args!=null)for(Object o:args){
            if(first){
                first=false;
            }else{
                sb.append(j);
            }
            sb.append(o);
        }
        return sb.toString();
    }

    private static boolean isArray(Object o){
		if(o==null)return false;
		return o.getClass().isArray();
	}
}
