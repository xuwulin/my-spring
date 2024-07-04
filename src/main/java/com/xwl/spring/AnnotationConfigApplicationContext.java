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
