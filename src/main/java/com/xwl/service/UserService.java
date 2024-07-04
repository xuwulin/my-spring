package com.xwl.service;

import com.xwl.spring.annotation.Autowired;
import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.aware.BeanNameAware;
import com.xwl.spring.init.InitializingBean;

@Component("userService")
@Scope("singleton")
public class UserService implements BeanNameAware, InitializingBean, UserInterface {

    private String beanName;

    @Autowired
    private OrderService orderService;

    @Override
    public void test(){
        System.out.println("beanName = " + beanName);
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("执行 afterPropertiesSet() 方法 （初始化）");
    }

}
