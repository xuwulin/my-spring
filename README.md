# 模拟实现Spring

准备工作：

新建一个项目，新建两个包分别为：com.xwl.spring用于存放模拟Spring的组件，com.xwl.service用于存放自定义组件以及测试类

所有和Spring相关的组件命名均与Spring保持一致

## 1、定义基础组件

### 1.1、BeanDefinition

```java
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

```

### 1.2、@ComponentScan

```java
package com.xwl.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 包扫描注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScan {
    /**
     * 扫描的包路径
     *
     * @return
     */
    String value() default "";
}

```

### 1.3、@Component

```java
package com.xwl.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 组件标识注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    /**
     * 指定组件的名称
     *
     * @return
     */
    String value() default "";
}

```

### 1.4、@Scope

```java
package com.xwl.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用域标识注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    /**
     * 作用域标识，默认是单例即singleton
     *
     * @return
     */
    String value() default "";
}

```



## 2、扫描

定义注解配置容器：AnnotationConfigApplicationContext.java

```java
package com.xwl.spring;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.ComponentScan;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.bean.BeanDefinition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置的容器
 */
public class AnnotationConfigApplicationContext {
    /**
     * 配置类
     */
    private Class configClass;

    /**
     * 存放bean定义信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 存放单例bean
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public AnnotationConfigApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描：beanDefinition ==> beanDefinitionMap
        // 判断configClass上是否配置了@ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            // 获取扫描路径：com.xwl.test 是包名，并不是真正的路径
            String path = componentScanAnnotation.value();
            // 替换点，com/xwl/test，相对路径
            path = path.replace(".", "/");
            // 注意：扫描的是生成的.class文件，而不是.java文件

            // 获取classLoader
            ClassLoader classLoader = AnnotationConfigApplicationContext.class.getClassLoader();
            // 根据相对路径获取资源：/D:/WorkspaceIdea/source-code/my-spring/target/classes/com/xwl/service
            URL resource = classLoader.getResource(path);
            // 获取目录或文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    // 获取文件的绝对路径:D:\WorkspaceIdea\source-code\my-spring\target\classes\com\xwl\service\AppConfig.class
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 截取fileName：com\xwl\service\AppConfig
                        String className = fileName.substring(fileName.indexOf("com"), fileName.lastIndexOf(".class"));
                        // 替换反斜杠，得到全类名com.xwl.service.AppConfig
                        className = className.replace("\\", ".");
                    }
                }
            }
        }
    }
}

```

## 3、生成BeanDefinition

```java
package com.xwl.spring;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.ComponentScan;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.bean.BeanDefinition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置的容器
 */
public class AnnotationConfigApplicationContext {
    /**
     * 配置类
     */
    private Class configClass;

    /**
     * 存放bean定义信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public AnnotationConfigApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描：beanDefinition ==> beanDefinitionMap
        // 判断configClass上是否配置了@ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            // 获取扫描路径：com.xwl.test 是包名，并不是真正的路径
            String path = componentScanAnnotation.value();
            // 替换点，com/xwl/test，相对路径
            path = path.replace(".", "/");
            // 注意：扫描的是生成的.class文件，而不是.java文件

            // 获取classLoader
            ClassLoader classLoader = AnnotationConfigApplicationContext.class.getClassLoader();
            // 根据相对路径获取资源：/D:/WorkspaceIdea/source-code/my-spring/target/classes/com/xwl/service
            URL resource = classLoader.getResource(path);
            // 获取目录或文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    // 获取文件的绝对路径:D:\WorkspaceIdea\source-code\my-spring\target\classes\com\xwl\service\AppConfig.class
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 截取fileName：com\xwl\service\AppConfig
                        String className = fileName.substring(fileName.indexOf("com"), fileName.lastIndexOf(".class"));
                        // 替换反斜杠，得到全类名com.xwl.service.AppConfig
                        className = className.replace("\\", ".");

                        try {
                            // 使用类加载器加载类
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {

                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();

                                // BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setBeanClass(clazz);

                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                // 放入map
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

```

## 4、创建bean

AnnotationConfigApplicationContext.java

```java
package com.xwl.spring;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.ComponentScan;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.bean.BeanDefinition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置的容器
 */
public class AnnotationConfigApplicationContext {
    /**
     * 配置类
     */
    private Class configClass;

    /**
     * 存放bean定义信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 存放单例bean
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public AnnotationConfigApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描：beanDefinition ==> beanDefinitionMap
        // 判断configClass上是否配置了@ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            // 获取扫描路径：com.xwl.test 是包名，并不是真正的路径
            String path = componentScanAnnotation.value();
            // 替换点，com/xwl/test，相对路径
            path = path.replace(".", "/");
            // 注意：扫描的是生成的.class文件，而不是.java文件

            // 获取classLoader
            ClassLoader classLoader = AnnotationConfigApplicationContext.class.getClassLoader();
            // 根据相对路径获取资源：/D:/WorkspaceIdea/source-code/my-spring/target/classes/com/xwl/service
            URL resource = classLoader.getResource(path);
            // 获取目录或文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    // 获取文件的绝对路径:D:\WorkspaceIdea\source-code\my-spring\target\classes\com\xwl\service\AppConfig.class
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 截取fileName：com\xwl\service\AppConfig
                        String className = fileName.substring(fileName.indexOf("com"), fileName.lastIndexOf(".class"));
                        // 替换反斜杠，得到全类名com.xwl.service.AppConfig
                        className = className.replace("\\", ".");

                        try {
                            // 使用类加载器加载类
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {

                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();

                                // BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setBeanClass(clazz);

                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                // 放入map
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 创建
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                // 创建单例bean
                Object bean = createBean(beanName, beanDefinition);
                // 放入单例池
                singletonObjects.put(beanName, bean);
            }
        }

    }

    /**
     * 创建bean
     *
     * @param beanName       bean名称
     * @param beanDefinition bean定义信息
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getBeanClass();
        
        try {
            // 需要创建bean的类必须要有构造方法
            Object instance = clazz.getConstructor().newInstance();
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

```

## 5、获取bean

AnnotationConfigApplicationContext.java

```java
package com.xwl.spring;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.ComponentScan;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.bean.BeanDefinition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置的容器
 */
public class AnnotationConfigApplicationContext {
    /**
     * 配置类
     */
    private Class configClass;

    /**
     * 存放bean定义信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 存放单例bean
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /**
     * 获取bean
     *
     * @param beanName bean名称
     * @return
     */
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if (beanDefinition == null) {
            throw new NullPointerException("没有找到bean：" + beanName);
        } else {
            String scope = beanDefinition.getScope();
            if (scope.equals("singleton")) {
                // 单例
                Object bean = singletonObjects.get(beanName);
                // 判断bean如果为null时，走创建流程
                // 防止依赖注入时，如果没有创建完成，会为null，比如在OrderService中依赖注入了userService，创建bean时orderService先创建，而userService还未创建，此时给orderService中的userService赋值时，userService为null
                if (bean == null) {
//                    Object o = createBean(beanName, beanDefinition);
//                    singletonObjects.put(beanName, o);
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            } else {
                // 多例
                return createBean(beanName, beanDefinition);
            }
        }
    }
}

```

测试：

UserService.java

```java
package com.xwl.service;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.Scope;

@Component("userService")
@Scope("singleton")
public class UserService {
}

```

AppConfig.java

```java
package com.xwl.service;

import com.xwl.spring.annotation.ComponentScan;

@ComponentScan("com.xwl.service")
public class AppConfig {
}

```

Test.java

```java
package com.xwl.service;

import com.xwl.spring.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
    }
}

```

运行结果：

```
com.xwl.service.UserService@1d44bcfa
com.xwl.service.UserService@1d44bcfa
com.xwl.service.UserService@1d44bcfa
```

修改UserService.java的作用域

```java
package com.xwl.service;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.Scope;

@Component("userService")
@Scope("prototype")
public class UserService {
}

```

再次测试，运行结果：

```
com.xwl.service.UserService@1d44bcfa
com.xwl.service.UserService@266474c2
com.xwl.service.UserService@6f94fa3e
```

## 6、依赖注入

AnnotationConfigApplicationContext.java

```java
	/**
     * 创建bean
     *
     * @param beanName       bean名称
     * @param beanDefinition bean定义信息
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getBeanClass();

        try {
            // 需要创建bean的类必须要有构造方法
            // 实例化
            Object instance = clazz.getConstructor().newInstance();

            // 简单实现依赖注入：给属性赋值，但不是所有的属性都需要注入，只是给加了@Autowired注解的属性赋值
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    // 属性赋值（spring的依赖注入是先byType，后byName）
                    field.set(instance, getBean(field.getName()));
                }
            }
            
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
```

测试：

修改UserService.java

```java
package com.xwl.service;

import com.xwl.spring.annotation.Autowired;
import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.Scope;

@Component("userService")
@Scope("singleton")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(orderService);
    }
}

```

Test.java

```java
package com.xwl.service;

import com.xwl.spring.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}
```

运行结果：

```
com.xwl.service.OrderService@66d3c617
```

## 7、Aware回调机制

新增Aware接口：BeanNameAware.java

```java
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

```

修改UserService.java

```java
package com.xwl.service;

import com.xwl.spring.annotation.Autowired;
import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.aware.BeanNameAware;

@Component("userService")
@Scope("singleton")
public class UserService implements BeanNameAware {

    private String beanName;

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println("beanName = " + beanName);
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}

```

修改AnnotationConfigApplicationContext.java

```java
    /**
     * 创建bean
     *
     * @param beanName       bean名称
     * @param beanDefinition bean定义信息
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getBeanClass();

        try {
            // 需要创建bean的类必须要有构造方法
            // 实例化
            Object instance = clazz.getConstructor().newInstance();

            // 依赖注入
            // 简单实现，给属性赋值，但不是所有的属性都需要注入，只是给加了@Autowired注解的属性赋值
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    // 属性赋值（spring的依赖注入是先byType，后byName）
                    field.set(instance, getBean(field.getName()));
                }
            }

            // Aware接口回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
```

## 8、初始化

新增init接口：InitializingBean.java

```java
package com.xwl.spring.init;

public interface InitializingBean {
     /**
     * 属性设置
     */
    void afterPropertiesSet();
}

```

修改UserService.java

```java
package com.xwl.service;

import com.xwl.spring.annotation.Autowired;
import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.aware.BeanNameAware;
import com.xwl.spring.init.InitializingBean;

@Component("userService")
@Scope("singleton")
public class UserService implements BeanNameAware, InitializingBean {

    private String beanName;

    @Autowired
    private OrderService orderService;

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

```

再次运行测试方法，结果：

```
执行 afterPropertiesSet() 方法 （初始化）
beanName = userService
com.xwl.service.OrderService@66d3c617
```

## 9、BeanPostProcessor

新增bean后置处理接口：BeanPostProcessor.java

```java
package com.xwl.spring.processor;

public interface BeanPostProcessor {
    /**
     * 初始化之前执行
     *
     * @param bean
     * @param beanName
     * @return
     */
    Object postProcessBeforeInitialization(Object bean, String beanName);

    /**
     * 初始化之后执行
     *
     * @param bean
     * @param beanName
     * @return
     */
    Object postProcessAfterInitialization(Object bean, String beanName);
}

```

新增MyBeanPostProcessor实现BeanPostProcessor接口：

```java
package com.xwl.service;

import com.xwl.spring.annotation.Component;
import com.xwl.spring.processor.BeanPostProcessor;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            System.out.println("初始化前执行");
        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            System.out.println("初始化后执行");
        }
        return null;
    }
}

```

修改：AnnotationConfigApplicationContext.java

```java
package com.xwl.spring;

import com.xwl.spring.annotation.Autowired;
import com.xwl.spring.annotation.Component;
import com.xwl.spring.annotation.ComponentScan;
import com.xwl.spring.annotation.Scope;
import com.xwl.spring.aware.BeanNameAware;
import com.xwl.spring.init.InitializingBean;
import com.xwl.spring.bean.BeanDefinition;
import com.xwl.spring.processor.BeanPostProcessor;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解配置的容器
 */
public class AnnotationConfigApplicationContext {
    /**
     * 配置类
     */
    private Class configClass;

    /**
     * 存放bean定义信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 存放单例bean
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /**
     * bean后置处理器集合
     */
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    public AnnotationConfigApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描：beanDefinition ==> beanDefinitionMap
        // 判断configClass上是否配置了@ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            // 获取扫描路径：com.xwl.test 是包名，并不是真正的路径
            String path = componentScanAnnotation.value();
            // 替换点，com/xwl/test，相对路径
            path = path.replace(".", "/");
            // 注意：扫描的是生成的.class文件，而不是.java文件

            // 获取classLoader
            ClassLoader classLoader = AnnotationConfigApplicationContext.class.getClassLoader();
            // 根据相对路径获取资源：/D:/WorkspaceIdea/source-code/my-spring/target/classes/com/xwl/service
            URL resource = classLoader.getResource(path);
            // 获取目录或文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    // 获取文件的绝对路径:D:\WorkspaceIdea\source-code\my-spring\target\classes\com\xwl\service\AppConfig.class
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 截取fileName：com\xwl\service\AppConfig
                        String className = fileName.substring(fileName.indexOf("com"), fileName.lastIndexOf(".class"));
                        // 替换反斜杠，得到全类名com.xwl.service.AppConfig
                        className = className.replace("\\", ".");

                        try {
                            // 使用类加载器加载类
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {

                                // 判断clazz是否是BeanPostProcessor的实现类
                                // 不能使用instanceof判断，因为是class，不是对象
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    // 创建BeanPostProcessor对象
                                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.newInstance();
                                    // 放入BeanPostProcessor集合
                                    beanPostProcessorList.add(beanPostProcessor);
                                }

                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();
                                if (beanName.equals("")) {
                                    // 获取类名，首字母小写
                                    String simpleName = clazz.getSimpleName();
                                    beanName = Introspector.decapitalize(simpleName);
                                    // 等价写法
//                                    beanName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                                }

                                // BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setBeanClass(clazz);

                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                // 放入map
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        // 创建
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                // 创建单例bean
                Object bean = createBean(beanName, beanDefinition);
                // 放入单例池
                singletonObjects.put(beanName, bean);
            }
        }

    }

    /**
     * 创建bean
     *
     * @param beanName       bean名称
     * @param beanDefinition bean定义信息
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getBeanClass();

        try {
            // 需要创建bean的类必须要有构造方法
            // 实例化
            Object instance = clazz.getConstructor().newInstance();

            // 依赖注入
            // 简单实现，给属性赋值，但不是所有的属性都需要注入，只是给加了@Autowired注解的属性赋值
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    // 属性赋值（spring的依赖注入是先byType，后byName）
                    field.set(instance, getBean(field.getName()));
                }
            }

            // Aware接口回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            // 后置处理前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }

            // 后置处理后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

```

测试结果：

```
初始化前执行
执行 afterPropertiesSet() 方法 （初始化）
初始化后执行
beanName = userService
com.xwl.service.OrderService@63947c6b
```

## 10、AOP

新增UserInterface.java

```java
package com.xwl.service;

public interface UserInterface {
    void test();
}

```

修改UserService.java

```java
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

```

修改MyBeanPostProcessor.java

```java
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
                System.out.println("执行代理方法");
                return method.invoke(bean, args);
            });
            return proxyInstance;
        }
        return bean;
    }
}

```

修改AnnotationConfigApplicationContext.java

```java
/**
     * 创建bean
     *
     * @param beanName       bean名称
     * @param beanDefinition bean定义信息
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getBeanClass();

        try {
            // 需要创建bean的类必须要有构造方法
            // 实例化
            Object instance = clazz.getConstructor().newInstance();

            // 依赖注入
            // 简单实现，给属性赋值，但不是所有的属性都需要注入，只是给加了@Autowired注解的属性赋值
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    // 属性赋值（spring的依赖注入是先byType，后byName）
                    field.set(instance, getBean(field.getName()));
                }
            }

            // Aware接口回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            // 后置处理前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }

            // 后置处理后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                // 返回的是代理对象
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
```

运行结果：

```
初始化前执行
执行 afterPropertiesSet() 方法 （初始化）
初始化后执行
Exception in thread "main" java.lang.ClassCastException: com.sun.proxy.$Proxy5 cannot be cast to com.xwl.service.UserService
	at com.xwl.service.Test.main(Test.java:17)
```

为什么报错？

测试类中：UserService userService = (UserService) applicationContext.getBean("userService");这行报的错

applicationContext.getBean("userService");获取到的是一个代理对象：

![image-20240628182333038](https://images-1318546573.cos.ap-chengdu.myqcloud.com/typora/image-20240628182333038.png)

生成的代理类类型是 UserInterface 

修改Test.java

```java
package com.xwl.service;

import com.xwl.spring.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();
    }
}

```

运行结果：

```
初始化前执行
执行 afterPropertiesSet() 方法 （初始化）
初始化后执行
切面逻辑
beanName = userService
com.xwl.service.OrderService@7b23ec81
```







