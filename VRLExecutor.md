# VRLExecutor

> VRLExecutor 是 json-convertor 提供的脚本执行器，用于执行 VRL（Variable Rule Language）脚本。VRL 是一门**简洁高效**的脚本语言，专为 JSON 数据转换而生。

## 设计思路

VRLExecutor 的核心用途是：

1. **加载 VRL 脚本**，解析生成 AST（只需一次初始化）
2. **针对不同的输入反复执行**，每次输出处理后的 JSON
3. **通过脚本操作环境变量中的 Java 对象**，完成仿真、测试数据等场景

简单来说，VRLExecutor = **脚本引擎 + 环境变量注入 + POJO 操作**，让你可以用脚本灵活处理不同的输入数据，而无需修改代码。

## 典型场景

### 系统测试桩 / 服务仿真

在测试或仿真环境中，经常需要模拟服务返回不同的结果。传统做法是写多套代码或配置，而使用 VRLExecutor 只需：

- 编写一套 VRL 脚本定义转换逻辑
- 每次调用时传入不同的环境变量（包含不同的 Java POJO 和参数）
- 脚本自动修改 POJO 字段、构造输出 JSON

```
// 同一套脚本，不同的 env 输入，得到不同的输出
ve.execute(env1)  →  {"name":"kit","age":20,...}
ve.execute(env2)  →  {"name":"tom","age":30,...}
```

无需重新编译，灵活应对各种测试场景。

## 快速开始

### 1. 编写 VRL 脚本

见文件 `testfile/test_VRL.script`
[测试脚本，请点击这里](testfile/test_VRL.script "测试脚本")

```vrl
#DEFINE JSONLINE:{"project":"sim","data":[1,2,4,5]}
# 定义初始化的 JSON 初始内容

// 修改 Environment 里的值，可以操作 Java 的 POJO
pojo.name=name|"eric"
pojo.age=age
pojo.info.title="manager"
pojo.info.position="leader"
pojo.active=true

// 构造 JSON，这个是处理完的结果
.staff[0]=pojo
```

### 2. 创建 Java POJO

```java
public class Staff {
    private String name;
    private Info info;
    private int age;
    private boolean active;
    // getters & setters...
}

public class Info {
    private String position;
    private String title;
    // getters & setters...
}
```

### 3. 执行脚本

```java
import io.shmilyhe.convert.VRLExecutor;
import io.shmilyhe.convert.tools.ExpEnv;
import io.shmilyhe.convert.tools.JsonString;
import io.shmilyhe.convert.tools.ResourceReader;

public class TestExecutor {
    public static void main(String[] args) {
        // 加载脚本（一个脚本只需初始化一次，初始化会做内存编译，比较耗时）
        String script = ResourceReader.read("testfile/test_VRL.script");
        VRLExecutor ve = new VRLExecutor(new String[]{script});

        // 创建 POJO
        Staff st = new Staff();
        Info info = new Info();
        st.setInfo(info);

        // 注入环境变量
        ExpEnv env = new ExpEnv(null);
        env.put("age", 20);
        env.put("position", "leader");
        env.put("name", "kit");
        env.put("pojo", st);

        // 执行脚本
        Object result = ve.execute(env);
        System.out.println("脚本的处理结果: " + JsonString.asJsonString(result));
        System.out.println("pojo 修改后: " + JsonString.asJsonString(st));
    }
}
```

### 执行结果

```
脚本的处理结果: {"data":[1,2,4,5],"project":"sim","staff":[{"name":"kit","info":{"position":"leader","title":"manager"},"age":20,"active":true}]}
pojo 修改后:    {"name":"kit","info":{"position":"leader","title":"manager"},"age":20,"active":true}
```

可以看到，脚本同时做了两件事：
1. **修改了传入的 POJO**（`st` 的 name、age、active 等字段被更新）
2. **构造了输出 JSON**（基于 `#DEFINE JSONLINE` 定义的初始结构，将 POJO 放入 `.staff[0]`）

## 脚本说明

### 脚本执行逻辑

VRL 脚本按行顺序执行，主要分为两类操作：

| 操作类型 | 语法特征 | 作用 |
|---------|---------|------|
| 变量操作 | 不以 `.` 开头 | 操作环境变量中的值，可读写 Java POJO |
| 属性操作 | 以 `.` 开头 | 操作输出 JSON 的节点 |

以上面的脚本为例，执行流程是：
1. 先通过 `pojo.name=name|"eric"` 等语句，从环境变量取值修改 POJO 的字段
2. 再通过 `.staff[0]=pojo` 将修改后的 POJO 放入输出 JSON

### #DEFINE JSONLINE

在脚本第一行通过 `#DEFINE JSONLINE` 定义初始 JSON 结构，`execute()` 方法会以此为基础构建输出结果。

```
#DEFINE JSONLINE:{"project":"sim","data":[1,2,4,5]}
```

### 操作 POJO

不以 "." 开头的是变量，可以直接引用环境变量中注入的 Java 对象，并修改其属性。

```
pojo.name=name|"eric"      // 从 env 取 name，如果为 null 则用 "eric"
pojo.age=age               // 从 env 取 age
pojo.info.title="manager"  // 直接赋值字符串
pojo.active=true           // 直接赋值布尔值
```

赋值右侧支持三种形式：
- **变量引用**：`pojo.name=name` — 从环境变量中取值
- **直接赋值**：`pojo.info.title="manager"` — 直接写字符串/数字/布尔值
- **带默认值**：`pojo.name=name|"eric"` — 优先取变量，为 null 时用默认值

### 构造输出 JSON

以 "." 开头表示操作输出 JSON 的根节点。

```
.staff[0]=pojo             // 将 pojo 对象放入输出 JSON 的 staff 数组第 0 位
```

### VRL 完整语法

本文档仅介绍 VRLExecutor 特有的用法。VRL 的完整语法（属性操作、变量、数组、赋值、move、del、if/each 分枝遍历、函数定义、内置函数、运算等）请参阅：

- [VRL 转换指令（README.md）](README.md#转换指令 "VRL 转换指令")
- [内置函数（functions.md）](functions.md "内置函数")

## 注意事项

1. **`#DEFINE JSONLINE` 必须放在脚本第一行**，否则不会被识别
2. **脚本只需初始化一次**：`new VRLExecutor(script)` 会解析脚本生成 AST（抽象语法树），这个过程相对耗时。建议复用实例，多次调用 `execute()` 传入不同的环境变量即可
3. **环境变量可以为 null**，此时执行器会自动创建一个空的 Environment
4. **脚本支持 `//` 和 `#` 两种注释风格**

## 相关类

| 类名 | 说明 |
|------|------|
| `VRLExecutor` | VRL 脚本执行器 |
| `ExpEnv` | 执行环境上下文，用于传递变量和 Java 对象 |
| `DefineJsonParser` | 从脚本中提取 `#DEFINE JSONLINE` 定义的解析器 |
| `AstConvertorFactory` | AST 转换器工厂，将脚本解析为可执行的转换链 |
| `JsonString` | JSON 序列化工具 |
