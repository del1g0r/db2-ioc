package com.study.ioc.context;

import com.study.ioc.context.entity.BeanDefinition;

import java.util.List;

public interface BeanFactoryPostProcessor {

    void postProcessBeanFactory(List<BeanDefinition> definitions);
}
