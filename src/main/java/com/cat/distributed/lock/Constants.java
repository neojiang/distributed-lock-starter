package com.cat.distributed.lock;

/**
 * @author: neojiang
 * @date: 2019/09/25
 * @description: 全局常量
 **/
public class Constants {

    /**
     * 分布式锁的命名空间
     */
    public static final String NAMESPACE = "DISTRIBUTED_LOCK_NAMESPACE";

    /**
     * 分布式锁的父节点
     */
    public static final String NODE = "DEFAULT_LOCK_NODE";

    /**
     * 获取分布式锁的超时时间
     */
    public static final long TIMEOUT = 2000L;

    /**
     * zookeeper地址
     */
    public static final String SERVICE = "127.0.0.1:2181";

    /**
     * 重试次数
     */
    public static final int RETRY_COUNT = 5;

    /**
     * 重试间隔时间,单位ms
     */
    public static final int RETRY_TIME = 5000;

    /**
     * 会话超时时间,单位ms
     */
    public static final int SESSION_TIMEOUT = 5000;

    /**
     * 连接超时时间,单位ms
     */
    public static final int CONNECTION_TIMEOUT = 5000;
}
