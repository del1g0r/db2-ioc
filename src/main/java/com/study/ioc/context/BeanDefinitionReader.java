package com.study.ioc.context;

import com.study.ioc.context.entity.BeanDefinition;

import java.util.List;

public interface BeanDefinitionReader {

    List<BeanDefinition> readBeanDefinitions();
}
