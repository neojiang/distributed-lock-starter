package com.cat.distributed.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author: neojiang
 * @date: 2019/09/25
 * @description: 分布式锁注解@DistributedLock的AOP
 **/
@Slf4j
@Aspect
@Component
public class DistributedLockAspact {

    @Autowired
    private ZookeeperClient client;

    @Autowired
    private CuratorProperties properties;

    @Pointcut("@annotation(com.cat.distributed.lock.DistributedLock)")
    public void lockPointCut() {
    }

    @Around("lockPointCut()")
    public Object around(ProceedingJoinPoint point) throws Exception {

        // 返回值
        Object result = null;
        // 分布式锁
        InterProcessMutex lock = null;
        // 锁的信息
        LockInfo info = getLockInfo(point);
        // 锁的路径
        String node = client.getPath() + "/" + info.getName();

        try {
            lock = new InterProcessMutex(client.getClient(), node);
            if (lock.acquire(info.getTimeout(), TimeUnit.MILLISECONDS)) {
                log.info("lock [{}] get success.", node);
                result = point.proceed();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            try {
                lock.release();
                log.info("lock [{}] release success.", node);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                log.info("lock [{}] release failed.", node);
                throw e;
            }
        }
    }

    /**
     * 获取锁的名称
     */
    private LockInfo getLockInfo(JoinPoint point) {
        LockInfo info = new LockInfo();
        try {
            // 获取注解
            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = point.getTarget().getClass().getMethod(ms.getName(), ms.getParameterTypes());
            DistributedLock curatorLock = method.getAnnotation(DistributedLock.class);

            setTimeout(info, curatorLock);
            setLockName(point, method, curatorLock, info);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 设置获取锁的超时时间
     *
     * @param info
     * @param curatorLock
     */
    private void setTimeout(LockInfo info, DistributedLock curatorLock) {
        if (curatorLock.timeout() > 0) {
            info.setTimeout(curatorLock.timeout());
        } else if (properties.getLock().getTimeout() > 0) {
            info.setTimeout(properties.getLock().getTimeout());
        } else {
            info.setTimeout(Constants.TIMEOUT);
        }
    }

    /**
     * 设置锁的名称 = 类的全路径名称.方法:设置的锁的名称
     * @param point         连接点
     * @param method        方法
     * @param curatorLock   注解
     * @param info          锁的信息
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setLockName(JoinPoint point, Method method, DistributedLock curatorLock, LockInfo info) throws NoSuchFieldException, IllegalAccessException {

        String classPath = point.getTarget().getClass().getName();

        if (!"null".equals(curatorLock.filed()) && !"void".equals(curatorLock.target().toString())) {
            String target = curatorLock.target().getName();
            Object[] args = point.getArgs();
            for (int i = 0; i < args.length; i++) {
                Object o = args[i];
                if (target.equals(o.getClass().getName())) {
                    Field field = o.getClass().getDeclaredField(curatorLock.filed());
                    field.setAccessible(true);
                    info.setName(classPath + "." + method.getName() + ":" + field.get(o).toString());
                    return;
                }
            }
        } else if (curatorLock.index() > 0) {
            Object index = point.getArgs()[curatorLock.index() - 1];
            info.setName(classPath + "." + method.getName() + ":" + index.toString());
        } else {
            info.setName(classPath + "." + method.getName() + ":" + curatorLock.value());
        }

    }
}
