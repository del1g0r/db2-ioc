package com.study.ioc.context;

import com.study.ioc.context.entity.Bean;
import com.study.ioc.context.entity.BeanDefinition;

import java.util.List;
import java.util.Map;

public interface ApplicationContext {

    Object getBean(String id);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String id, Class<T> clazz);

    void defferedInjectDepenencies(Object object);
}
