package com.cat.distributed.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author: neojiang
 * @date: 2019/09/25
 * @description: Curator自动配置类
 **/
@Configuration
@Import({ZookeeperClient.class, DistributedLockAspact.class})
@EnableConfigurationProperties(CuratorProperties.class)
@ConditionalOnProperty(name = "spring.curator.enabled", matchIfMissing = true)
public class CuratorAutoConfigure {

    @Autowired
    private CuratorProperties properties;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                properties.getService(),
                properties.getSessionTimeout(),
                properties.getConnectionTimeout(),
                new RetryNTimes(properties.getRetryCount(), properties.getRetryTime()));
    }

}
