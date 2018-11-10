package com.study.ioc;

import com.study.ioc.context.ApplicationContext;
import com.study.ioc.context.impl.ClassPathApplicationContext;
import com.study.ioc.context.impl.XmlBeanDefinitionReader;

import java.io.InputStream;

public class ApplicationContextFactory {

    public static ApplicationContext loadApplicationContext(InputStream stream) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(stream);
        return new ClassPathApplicationContext(reader);
    }
}
