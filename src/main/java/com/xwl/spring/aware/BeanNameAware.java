package com.xwl.spring.aware;

/**
 * 回调接口，由容器调用，用于设置beanName
 */
public interface BeanNameAware {
    /**
     * 设置beanName
     *
     * @param beanName beanName
     */
    void setBeanName(String beanName);

}
