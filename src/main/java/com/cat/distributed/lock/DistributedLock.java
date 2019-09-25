package com.cat.distributed.lock;

import java.lang.annotation.*;

/**
 * @author: neojiang
 * @date: 2019/09/25
 * @description: 分布式锁的注解
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DistributedLock {

    /**
     * 锁的名称
     */
    String value() default "default";

    /**
     * 方法参数的序号
     */
    int index() default 0;

    /**
     * 字段
     */
    String filed() default "null";

    /**
     * 实体
     */
    Class<?> target() default void.class;

    /**
     * 获取锁的超时时间
     */
    long timeout() default 0;

}
