import io.shmilyhe.convert.tools.ReflectionUtils;

public class PerformanceTest {
    public static void main(String[] args) {
        User user = new User();
        user.setName("测试");
        
        int iterations = 1000000;
        
        // 测试直接调用（基准）
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            user.getName();
        }
        long time1 = System.currentTimeMillis() - start1;
        
        // 测试反射（带缓存）
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            ReflectionUtils.get(user, "name");
        }
        long time2 = System.currentTimeMillis() - start2;
        
        System.out.println("直接调用: " + time1 + "ms");
        System.out.println("反射(缓存): " + time2 + "ms");
        System.out.println("性能比: " + (double) time2 / time1 + "x");
    }
}