package com.cat.distributed.lock;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: neojiang
 * @date: 2019/09/25
 * @description: Curator的属性配置类
 **/
@Setter
@Getter
@ToString
@ConfigurationProperties(prefix = "spring.curator")
public class CuratorProperties {

    /**
     * 是否启动分布式锁
     */
    private boolean enabled;

    /**
     * zookeeper地址,集群以","分割
     */
    private String service = Constants.SERVICE;

    /**
     * 重试次数
     */
    private int retryCount = Constants.RETRY_COUNT;

    /**
     * 重试间隔时间,单位ms
     */
    private int retryTime = Constants.RETRY_TIME;

    /**
     * 会话超时时间,单位ms
     */
    private int sessionTimeout = Constants.SESSION_TIMEOUT;

    /**
     * 连接超时时间,单位ms
     */
    private int connectionTimeout = Constants.CONNECTION_TIMEOUT;

    /**
     * 分布式锁的配置
     */
    private Lock lock = new Lock();

    @Setter
    @Getter
    @ToString
    public static class Lock {

        /**
         * 锁的命名空间
         */
        private String namespace;

        /**
         * 分布式锁的父节点
         */
        private String node;

        /**
         * 获取锁的超时时间,单位ms
         */
        private long timeout;

    }

}
