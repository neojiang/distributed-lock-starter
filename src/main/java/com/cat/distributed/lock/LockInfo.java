package com.cat.distributed.lock;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author: neojiang
 * @date: 2019/09/05
 * @description: 锁的信息
 **/
@Setter
@Getter
@ToString
public class LockInfo {

    /**
     * 锁的名称
     */
    private String name;

    /**
     * 获取锁的超时时间
     */
    private Long timeout;

}
