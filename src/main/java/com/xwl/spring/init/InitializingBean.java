package com.xwl.spring.init;

/**
 * 初始化接口
 */
public interface InitializingBean {
    /**
     * 属性设置
     */
    void afterPropertiesSet();
}
