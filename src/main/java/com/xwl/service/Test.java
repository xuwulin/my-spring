package com.xwl.service;

import com.xwl.spring.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("orderService"));

//        OrderService orderService = (OrderService) applicationContext.getBean("orderService");
//        orderService.test();

        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();
    }
}
