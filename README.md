## 基于Zookeeper实现的分布式锁
        本项目属于一个spring boot starter,是基于Zookeeper,以Curator客户端
    来实现分布式锁的。同时，提供@DistributedLock来配合使用，对代码是无侵害
    的，降低了耦合度。使用的时候只需要在方法上添加注解即可。
    
    下面的锁节点的结构。
    命名空间(总节点)
        |- 父节点(项目节点)
            |- 锁节点 (锁的节点,临时节点)
                |- 锁的自增序号子节点 (真正的分布式锁的节点,临时节点)

### 使用教程

1.添加POM依赖
```
<dependency>
    <groupId>com.cat</groupId>
    <artifactId>distributed-lock-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2.配置Curator
```
spring:
  curator:
    enabled: true # 是否启动分布式锁
```
 
3.通过注解@DistributedLock(推荐)
```
@DistributedLock
public void func(){
    //...业务代码
}
```     
 
4.通过注解代码

    @Autowired
    private ZookeeperClient zookeeperClient;

    public void byCode() {
        String lockName = zookeeperClient + "/" + name;
        InterProcessMutex lock = new InterProcessMutex(zookeeperClient.getClient(), lockName);
        try {
            if (lock.acquire(2000, TimeUnit.MILLISECONDS)) {
            //...业务代码
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

### 文档说明
1.配置文件说明
    
    默认的的配置属性值
    spring:
      application:
        name: application_name
      curator:
        enabled: true               # 是否启动分布式锁
        service: 127.0.0.1:2181     # zookeeper地址,","分割
        retryCount: 5               # 重试次数
        retryTime: 5000             # 重试间隔时间,单位ms
        sessionTimeout: 5000        # 会话超时时间,单位ms
        connectionTimeout: 5000     # 连接超时时间,单位ms
        lock:
          namespace: DISTRIBUTED_LOCK_NAMESPACE # 分布式锁的命名空间
          node: DEFAULT_LOCK_NODE               # 分布式锁的父节点
          timeout: 2000                         # 获取分布式锁的超时时间
    
    唯一一个需要配置的是spring.curator.enabled,这里只需要设置为true即可，表示启动使用分布式锁。
    
    分布式锁的命名空间可以自定义，系统默认是“DISTRIBUTED_LOCK_NAMESPACE”。
    分布式锁的命名空间命名优先级：
    spring.curator.lock.namespace > 系统默认
    
    
    所以配置的时候可以根据自身要求去设置分布式锁的命名空间。
    
    同理，分布式锁的父节点也是如此。在分布式环境中，该节点可以是项目节点的名称，也可以自己设置，系统默认是“DEFAULT_LOCK_NODE”。
    分布式锁的父节点命名优先级：
    spring.curator.lock.node > spring.application.name > 系统默认
    
    以上是两个关于节点的设置。其余的属性，可自行配置，如若不配置则会采用默认值。
 
2.@DistributedLock注解说明
 
        这是推荐使用的方法，因为它可以降低耦合度，同时可以做到对代码的无侵害，只需要在方法上添加注解即可。
    该实现想必你是知道了，就基于AOP的完成的。
    下面是注解的属性：
        value   // 锁的名称
        index   // 方法参数的序号
        filed   // 字段
        target  // 实体
        timeout // 获取锁的超时时间
    timeout很好理解，这里讲讲优先级的问题。采用注解的话，timeout的设置可以来自注解上的值，
    配置文件中timeout的值以及系统默认的值2000.它们的优先级是：注解 > 配置文件 > 系统默认。
    
    下面是关于设置锁的名称的问题。采用注解的话，设置锁的名称就变得麻烦起来了，如果不能像通过
    代码形式的去使用，那就带来很大的局限性了。这里，注解提供了3种方式来设置分布式锁的名称，
    相信已经可以满足绝大部分的业务需求了。
    第1种：@DistributedLock(value = "分布式锁")
    第2种：@DistributedLock(index = 1)
    第3种：@DistributedLock(filed="id",target=ClassName.class)
    
    第1种即value的值；
    第2种则是方法的第几个参数，index=1即第一个参数的值，index=2即第二个参数的值；
    第3种则是配置方法参数是实体的情况下使用的，加入参数是一个User类。User类如下：
        public class User{
            private String id;
            private String name;
            private int age;
            // ....
        }
    如果想用user的id作为分布式锁的名称，只需要将注解设置为：
    @DistributedLock(filed="id",target=User.class)
    
    通过注解最终得到分布式锁的名称是："类的全路径名.方法名称:设置分布式锁的名称"
    
    3种方法的优先级：第3种 > 第2种 > 第1种

### 分布式锁的实现原理
