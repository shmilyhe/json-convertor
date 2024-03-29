package io.shmilyhe.convert.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 一个高效的简单的json 解析工具
 * 整个解析实现在200行代码内
 * @author eshore
 *
 */
public class SimpleJson {
	Object lv[]=new Object [10];//层级的值
	String lk[]=new String [10];//层级名称
	int lt[]=new int [10];//层级类型
	int maxDeep=10;
	
	public static SimpleJson parse(String str){
		SimpleJson sj = new SimpleJson();
		sj.fromString(str);
		return sj;
	}
	
	public SimpleJson(){}
	
	/**
	 * 
	 * @param deep 支持最大的层级，默认是10
	 */
	public SimpleJson(int deep){
		 lv =new Object [deep];//层级的值
		 lk =new String [deep];//层级名称
		 lt =new int [deep];//层级类型
		 maxDeep=deep;
	}
	
	/**
	 * 获取根
	 * @return root
	 */
	public Object getRoot(){
		return lv[0];
	}
	
	/**
	 * 从字符串解析JSON
	 * @param str text
	 */
  public	void fromString(String str){
		int level=0;
		int flag=0;
		String name=null;
		int line=0;
		while(flag<str.length()){
			flag+=readBlank(str, flag);
			char c =str.charAt(flag);
			//System.out.print(c);
			if(c=='{'){
				Map m =new HashMap();
				add(level,m,name);
				name=null;
				level++;
				flag++;
			}else if(c=='['){
				ArrayList list =new ArrayList();
				add(level,list,name);
				name=null;
				level++;
				flag++;
			}else if(c==']'||c=='}'){
				level--;
				flag++;
			}else if(c==','){
				flag++;
			}else if(c==':'){
				flag++;
			}else if(c=='\r'||c=='\n'){
				flag++;
				char c2=str.charAt(flag);
				if(c2=='\r'||c2=='\n'){
					flag++;
				}
				line++;
			}else{//同级的
				flag+=readBlank(str, flag);
				//if(level==0)System.out.println("line:"+line);
				if(lt[level-1]==0){//JSONOBJECT
					if(name==null){
						Object  text = readString(str, flag,false);
						flag+=tmp_read_len;
						name=String.valueOf(text);
					}else{
						Object  text = readString(str, flag,false);
						flag+=tmp_read_len;
						add(level,text,name);
						name=null;
					}
				}else{//JSONARRAY
					name=null;
					Object  text = readString(str, flag,false);
					flag+=tmp_read_len;
					add(level,text,null);
				}
			}
		}
	}
	
  /**
	 * 添加Object值
	 * @param level leval
	 * @param obj value
	 * @param name key
	 */
	private void add(int level,Object obj,String name){
		lv[level]=obj;
		lk[level]=name;
		lt[level]=0;
		if(level>0){
			if(lt[level-1]==0){
				((Map)lv[level-1]).put(name, obj);
			}else{
				((List)lv[level-1]).add(obj);
			}
		}
	}
	
	/**
	 * 添加Array值
	 * @param level leval
	 * @param obj value
	 * @param name key
	 */
	private void add(int level,List obj,String name){
		lv[level]=obj;
		lk[level]=name;
		lt[level]=1;
		if(level>0){
			if(lt[level-1]==0){
				((Map)lv[level-1]).put(name, obj);
			}else{
				((List)lv[level-1]).add(obj);
			}
		}
	}
	

	/**
	 * 添加字符值
	 * @param level leval
	 * @param obj value
	 * @param name key
	 */
	/*private void add(int level,String obj,String name){
		if(level>0){
			if(lt[level-1]==0){
				((Map)lv[level-1]).put(name, obj);
			}else{
				((List)lv[level-1]).add(obj);
			}
		}
	}
	*/
	

	/**
	 * 读到的长度
	 */
	private int tmp_read_len=0;
	/**
	 * 读一段字符串到stop char 时停止
	 * 以"或' 开头的读到配对的"或'.否则就以空格或逗号]}结束
	 * @param str source string
	 * @param off offset 
	 * @return value
	 */
	private Object readString(String str,int off,boolean asString){
		tmp_read_len=0;
		int i=off;
		char stop=' ';
		boolean isString=false;
		char firstChar=str.charAt(off);
		if(firstChar=='"'||firstChar=='\''){
			stop=firstChar;
			off=i=i+1;
			isString=true;
		}
		char lc=0;//上一个字符
		//是否存在换行符转义，json-line 数据很有必要
		boolean hasReturnWord=false;
		for(;i<str.length();i++){
			char c =str.charAt(i);
			if(stop==c&&lc!='\\')break;
			if(stop==' '&&(c==','||c==':'||c==']'||c=='}'))break;
			if(c=='\\') {
				if(i<str.length()-1) {
					char nc =str.charAt(i+1);
					if(nc=='r'||nc=='n')hasReturnWord=true;
				}
				
			}
			lc=c;
		}
		String value=str.substring(off,i);
		if(hasReturnWord) {
			value = value.replaceAll("[\\r\\n]+", "\r\n");
		}
		tmp_read_len=i-off;
		if(stop!=' ')tmp_read_len+=2;//没有引号的情况下算读取长度要加上两个引号
		if(asString||isString)return firstChar=='"'?escape(value):value;
		return valueOf(value);
	}
	private String escape(String v){
		return v.replaceAll("\\\\\"","\"");
	}

	private Object valueOf(String v){
		if(v==null||v.trim().length()==0||"null".equals(v))return null;
		v=v.trim();
		if("true".equalsIgnoreCase(v))return Boolean.TRUE;
		if("false".equalsIgnoreCase(v))return Boolean.FALSE;
		if(v.indexOf('.')>-1){
			return Double.parseDouble(v);
		}
		if(v.length()<8){
			return Integer.parseInt(v);
		}
		return Long.parseLong(v);
	}
	
	
	/**
	 * 读完连续空格
	 * @param str
	 * @param off
	 * @return count_of_blank 
	 */
	private int readBlank(String str,int off){
		int i=off;
		for(;i<str.length();i++){
			char c =str.charAt(i);
			if(c!=' ')break;
		}
		return i-off;
	}	
	
	/**
	 * 扩展层级
	 * @param level 扩展支持的最深层级
	 */
	public void extendLevel(int level){
		if(maxDeep>level)return;
		int newDeep=maxDeep*2;
		Object lv_[]=new Object [newDeep];
		int lt_[]=new int [newDeep];
		String lk_[]=new String [newDeep];
		System.arraycopy(lv, 0, lv_, 0, lv.length);
		System.arraycopy(lt, 0, lt_, 0, lt.length);
		System.arraycopy(lk, 0, lk_, 0, lk.length);
		lv=lv_;
		lk=lk_;
		lt=lt_;
		maxDeep=newDeep;
	}
	
}
