package com.study.ioc.context;

public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String id) throws RuntimeException;

    Object postProcessAfterInitialization(Object bean, String id) throws RuntimeException;
}
