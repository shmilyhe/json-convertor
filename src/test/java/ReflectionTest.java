import java.util.Date;

import io.shmilyhe.convert.tools.ReflectionUtils;

public class ReflectionTest {
    
    public static void main(String[] args) {
        // 创建测试对象
        User user = new User();
        user.setId(1001L);
        user.setName("张三");
        user.setAge(25);
        user.setActive(true);
        user.setBirthday(new Date());
        
        Address address = new Address();
        address.setCity("北京");
        address.setStreet("朝阳区建国路");
        user.setAddress(address);
        
        // ============ 测试 get 方法 ============
        System.out.println("=== 测试 get 方法 ===");
        
        // 获取简单属性
        Long id = (Long) ReflectionUtils.get(user, "id");
        System.out.println("id: " + id);  // 1001
        
        String name = (String) ReflectionUtils.get(user, "name");
        System.out.println("name: " + name);  // 张三
        
        Integer age = (Integer) ReflectionUtils.get(user, "age");
        System.out.println("age: " + age);  // 25
        
        Boolean active = (Boolean) ReflectionUtils.get(user, "active");
        System.out.println("active: " + active);  // true
        
        // 获取嵌套属性
        String city = (String) ReflectionUtils.get(user, "address.city");
        System.out.println("city: " + city);  // 北京
        
        String street = (String) ReflectionUtils.get(user, "address.street");
        System.out.println("street: " + street);  // 朝阳区建国路
        
        // ============ 测试 set 方法 ============
        System.out.println("\n=== 测试 set 方法 ===");
        
        // 设置简单属性
        ReflectionUtils.set(user, "name", "李四");
        System.out.println("修改后 name: " + user.getName());  // 李四
        
        ReflectionUtils.set(user, "age", 30);
        System.out.println("修改后 age: " + user.getAge());  // 30
        
        // 设置嵌套属性
        ReflectionUtils.set(user, "address.city", "上海");
        System.out.println("修改后 city: " + user.getAddress().getCity());  // 上海
        
        // 设置不存在的属性（静默失败）
        boolean result = ReflectionUtils.set(user, "nonexistent", "test");
        System.out.println("设置不存在属性结果: " + result);  // false
    }
}

// ============ 测试用的 POJO ============

class User {
    private Long id;
    private String name;
    private int age;
    private boolean active;
    private Date birthday;
    private Address address;
    
    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}

class Address {
    private String city;
    private String street;
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
}