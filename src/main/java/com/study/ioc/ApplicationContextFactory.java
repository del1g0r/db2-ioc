package com.study.ioc;

import com.study.ioc.context.ApplicationContext;
import com.study.ioc.context.impl.ClassPathApplicationContext;
import com.study.ioc.context.impl.XmlBeanDefinitionReader;

public class ApplicationContextFactory {

    public static ApplicationContext loadApplicationContext(String path) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(path);
        return new ClassPathApplicationContext(reader);
    }
}
