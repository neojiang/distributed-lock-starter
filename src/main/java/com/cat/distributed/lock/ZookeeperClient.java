package com.cat.distributed.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author: neojiang
 * @date: 2019/09/25
 * @description: Zookeeper客户端
 **/
@Slf4j
@Component
public class ZookeeperClient implements InitializingBean {

    @Autowired
    private Environment environment;

    @Autowired
    private CuratorProperties properties;

    /**
     * 分布式锁的路径
     */
    private String path;

    /**
     * zookeeper客户端
     */
    private CuratorFramework client = null;

    public ZookeeperClient(CuratorFramework client) {
        this.client = client;
    }

    /**
     * 获取zookeeper客户端
     * @return CuratorFramework
     */
    public CuratorFramework getClient() {
        return client;
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    /**
     * 获取分布式锁的路径
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * 初始化操作,创建命名空间节点,父节点
     * 创建zk节点
     * 命名空间(总节点)
     *     |- 父节点(项目节点)
     *           |- 子节点(锁的节点,临时节点)
     */
    public void init() {

        // 设置-分布式锁的路径
        path = "/" + getNode();
        client = client.usingNamespace(getNameSpace());
        try {
            if (client.checkExists().forPath(path) == null) {
                client
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
        } catch (Exception e) {
            log.error("The client connected to the zookeeper service failed. Please try again.");
            e.printStackTrace();
        }
    }

    /**
     * 获取分布式锁的命名空间
     * @return
     */
    private String getNameSpace() {
        if (!StringUtils.isEmpty(properties.getLock().getNamespace())) {
            return properties.getLock().getNamespace();
        } else {
            return Constants.NAMESPACE;
        }
    }

    /**
     * 获取分布式锁的父节点
     * @return
     */
    private String getNode(){
        if (!StringUtils.isEmpty(properties.getLock().getNode())) {
            return properties.getLock().getNode();
        } else if (!StringUtils.isEmpty(environment.getProperty("spring.application.name"))) {
            return environment.getProperty("spring.application.name");
        } else {
            return Constants.NODE;
        }
    }
}
