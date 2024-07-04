package com.xwl.spring.bean;

/**
 * bean的定义信息
 */
public class BeanDefinition {
    /**
     * bean的类型
     */
    private Class beanClass;

    /**
     * bean的作用域
     */
    private String scope;

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
