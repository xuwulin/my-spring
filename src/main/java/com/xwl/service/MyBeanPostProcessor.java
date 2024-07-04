package com.xwl.service;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.processor.BeanPostProcessor;

import java.lang.reflect.Proxy;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            System.out.println("初始化前执行");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            System.out.println("初始化后执行");
        }

        // 生成代理对象
        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(MyBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                System.out.println("切面逻辑");
                // 执行原方法
                return method.invoke(bean, args);
            });
            return proxyInstance;
        }
        return bean;
    }
}
