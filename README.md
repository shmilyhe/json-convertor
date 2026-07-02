# json-convertor

> 这是一个简单且高效的JSON 转换组件，适合现有的JSON 进行一些加工，如属性，改变层级结构等。在API适配的场景非常有用。

注意事项：
组件实现了一个简单的AST（语法树），针对JSON数据进行转换。适应一些API转换的场景。组件采用在线编译的方法加载脚本，适合不停服务的在线业务。



另外这个组件还提供了一个简单高效的[JSON组件，请点击这里](json.md "JSON组件")，以及支持 Java POJO 操作的[VRL脚本执行器，请点击这里](VRLExecutor.md "VRLExecutor")

## mvn 引入
```
<dependency>
	<groupId>io.github.shmilyhe</groupId>
	<artifactId>json-convert</artifactId>
	<version>3.1.7</version>
</dependency>
```

## 快速开始

给定一个原始的JSON
见文件testfile/test1.json
[测试JSON，请点击这里](testfile/test1.json "测试JSON")

转换脚本
见文件testfile/test1.script
[测试脚本，请点击这里](testfile/test1.script "测试脚本")
测试代码
```
import io.shmilyhe.convert.JsonConvertor;
import io.shmilyhe.convert.tools.ResourceReader;

public class TestJsonConvertor{

    public static void main(String []args){
        String json =ResourceReader.read("testfile/test1.json");
        String commands =ResourceReader.read("testfile/test1.script");
        //一个脚本需只初始化一次，初始化脚本会做一次内存的编译，比较耗时。脚本初始化后执行的效率很高。
        JsonConvertor jc = new JsonConvertor(commands);
        String dest = jc.convert(json);
        System.out.println(dest);
    }
}
```
转换的结果为：
```
{
    "data": {
        "ext": 15129,
        "abc": true,
        "fromvar": 226935.0,
        "odd": [1,3,5,7,9],
        "persons": [
            {"name":"eric1","age":12},
            {"name":"eric2","age":24},
            {"a":[3],"name":"eric3","age":6},
            {"name":"eric4","age":18},
            {"name":"eric5","age":30},
            {"a":[6],"name":"eric6","age":12},
            {"name":"eric7","age":24},
            {"name":"eric8","age":36}
        ],
        "tmp": 1,
        "name": "eric",
        "att5": "203333",
        "id": 1,
        "addr": {
            "province": "gd",
            "ctiy": "gz"
        },
        "success1": false,
        "group":["g19999","g29999","null9999","null9999",10000]
    }
}
```




## 转换指令

### 属性操作
“.”代表JSON 的根
.data 代表JSON 的data属性

```
.name = "eric"
```

### 变量操作
不以 "." 开头的是变量
变量与性属的区别 ：属性会在JSON转换结果里出现，而变量只是中间过程，不会在JSON中体现

```
tmp = 0
```
### 变量作用域
1.代码块内"{}"可以访问 代码块外的变量 
2.代码块外不能访问代码块内的变量
3.变量第一次赋值，就相当于做了声明

### 数组操作  
.data[0] 代表JSON 的data属性的第0 个值
### 序列
支持序列的表达式，可以创建序列
```
array=[1,2,3];

print(array[0])
print(array[1])
print(array[2])
```

### 赋值
给JSON 设置值

#### 给JSON 添加一个常量值

下面这条指令会给JSON 的code 属性设置为200，如果JSON没有code属性将会自动创建
```
.code=200
```

#### 把JSON 的属性值赋给其它属性
下面这条指令会给JSON 的test 属性设置为data.name的值，如果JSON没有code属性将会自动创建
```
.test = .data.name
//{"data":{"name":"eric"}}==>{"test":"eric","data":{"name":"eric"}}
```
#### 往JSON 数组添加值
```
.data[3] = 1
//{"data":[0]}==>{"data":[0,null,null,1]}
```

### move 指令
移动属性

#### 修改属性名字
下面这条指令会给JSON 的test 属性名 改为 test2
```
move(.test,.test2)
//{"test":1}==>{"test2":1}
```

#### 把JSON 往下移
下面这条指令会给JSON的移到 data 下面

```
move(.,.data)
//{"name":"eric"}==>{"data":{"name":"eric"}}
```

### del 指令
移除某个属性

```
del(.data)
//{"data":"eric"}==>{}
```

### 分枝
if (表达式){
        操作指令
}

例：
```
if (.name == "eric" ){
        .name=.name+"0000"
}

//{"name":"eric"}==>{"name":"eric0000"}
```



### 遍历
each(属性|变量){
        操作指令
}
说明：遍历代码块的"." 代表被遍历对像。如果在遍历块内要访问上层属性可以先将上层的属性赋给一个变量

示例：
```
removecount=0
each(.persons){
        # 移除18岁以下的人员
        if (.age<18){
                removecount=removecount+1
                del(.)
        }
}

.removes = removecount

```
### 退出
函数：exit,
退出转换，exit 后面的逻辑将不再执行。当多个脚本合并执行时，后面的脚本也不会执行。

示例：
```
.name="Alice"

if (.name == "Alice") {
    exit()
}
.name="Bob"

```
执行的结果为：
```
{"name":"Alice"}
```

### 强制全局
函数：global
变量 强制为全局
示例：
```
max=0;
each(.persons){
        # 移除18岁以下的人员
        if (.age>max){
                max=.age
                global(maxage,max);
        }
}
```

### 命名空间
函数：namespace
设置消息的命名空间，在执行器外可以获取命令空间

示例：
```

if (messageType == "online") {
    namespace("onOffLine");
}

```

### 运算

#### 四则运算
```
str="string";
str=str+100
print(str)//string100

num=10
num=(1+2)/3*4-num

```

#### 位运算

```
//位移
a=1
a1=a>>1
//数符号右移
a1=a>>>1
a1=a<<1

//与、或、异或
b=1^2
c=1|2
d=1&2
print(b)
print(c)
print(d)

```

#### 布尔运算

```
a  = 2;
b1 = 2>2;
b2 = 2>=2;
b3 = a==2&&false;
b4 = a==2||false;

```



### 函数
支持自定义函数
```

//定义函数 ee
function ee(){
    print('ee');
     return bb(1,2);
}

//定义函数 bb
function bb(a,b){
    print(a*3);print(b);return "llll===========";
}

//调用函数
ee()
```

### 内置函数
为了方便对数据进行转换，内置了一些常用函数
[内置函数，请点击这里](functions.md "内置函数")

### 函数扩展
内置函数不满足的时候，可以使用JAVA语言添加函数，也可以覆盖内置函数

#### 扩展函数定义
扩展函需要实现IFunction 接口，IFunction定义如下：
```
/**
 * 扩展函数
 */
public interface IFunction {
    /**
     * 函数调用入口
     * @param args 参数LIST
     * @param env 环境
     * @return 返回值
     */
    Object call(List args,ExpEnv env);
}
```
##### 参数说明：
args ，调用函数的参数。调用函数时转换器会把参数从args传入。
env，函数调用上下文，可以调用如全局变量，内置函数等

##### 示例
实现一个打印

```
import java.util.List;

import io.shmilyhe.convert.callee.IFunction;
import io.shmilyhe.convert.tools.ExpEnv;

/**
 * 类似  c 的printf 方法
 */
public class PrintFFunction implements IFunction{

    @Override
    public Object call(List args, ExpEnv env) {
        try{
            String text =(String)args.get(0);
            Object a[] = new Object[args.size()-1];
            int i=0;
            for(Object o:args){
                if(i>0){
                    a[i-1]=o;
                }
                i++;
            }
            String str=String.format(text,a);
            return str;
        }catch(Exception e){
            return null;
        }
        
    }
    //format
}
```






# 计划支持的特性
* 1.支持输入输出流（不生成字符串）
* 2.自定义函数




