package com.study.ioc.context.impl;

import com.study.ioc.context.*;
import com.study.ioc.context.exception.BeanInstantiationException;
import com.study.ioc.context.entity.Bean;
import com.study.ioc.context.entity.BeanDefinition;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ClassPathApplicationContext implements ApplicationContext {

    private Map<String, Bean> beans;

    public ClassPathApplicationContext() {
    }

    public ClassPathApplicationContext(BeanDefinitionReader reader) {
        List<BeanDefinition> beanDefinitions = reader.readBeanDefinitions();
        processBeanFactoryPostProcessor(beanDefinitions);
        this.beans = postProcessAfterInitialization(
                processPostConstruct(
                        postProcessBeforeInitialization(
                                injectRefDepenencies(beanDefinitions,
                                        injectValueDepenencies(beanDefinitions,
                                                constructBeans(beanDefinitions
                                                )
                                        )
                                )
                        )
                )
        );
    }

    void processBeanFactoryPostProcessor(List<BeanDefinition> beanDefinitions) {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> beanClass = Class.forName(beanDefinition.getClassName());
                if (BeanFactoryPostProcessor.class.isAssignableFrom(beanClass)) {
                    Constructor<?> constructor = beanClass.getConstructor();
                    BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) constructor.newInstance();
                    beanFactoryPostProcessor.postProcessBeanFactory(beanDefinitions);
                }
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
                throw new BeanInstantiationException("Couldn't process bean factory " + beanDefinition.getId());
            }
        }
    }

    Map<String, Bean> postProcessBeforeInitialization(Map<String, Bean> beans) {
        for (Map.Entry<String, Bean> entry : beans.entrySet()) {
            try {
                Class<?> beanClass = entry.getValue().getValue().getClass();
                if (BeanPostProcessor.class.isAssignableFrom(beanClass)) {
                    Constructor<?> constructor = beanClass.getConstructor();
                    BeanPostProcessor beanBeanPostProcessor = (BeanPostProcessor) constructor.newInstance();
                    for (Map.Entry<String, Bean> entry0 : beans.entrySet()) {
                        Bean bean = entry0.getValue();
                        bean.setValue(beanBeanPostProcessor.postProcessBeforeInitialization(bean.getValue(), bean.getId()));
                    }
                }
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new BeanInstantiationException("Couldn't process \"before\" initialization" + entry.getKey());
            }
        }
        return beans;
    }

    Map<String, Bean> postProcessAfterInitialization(Map<String, Bean> beans) {
        for (Map.Entry<String, Bean> entry : beans.entrySet()) {
            try {
                Class<?> beanClass = entry.getValue().getValue().getClass();
                if (BeanPostProcessor.class.isAssignableFrom(beanClass)) {
                    Constructor<?> constructor = beanClass.getConstructor();
                    BeanPostProcessor beanBeanPostProcessor = (BeanPostProcessor) constructor.newInstance();
                    for (Map.Entry<String, Bean> entry0 : beans.entrySet()) {
                        Bean bean = entry0.getValue();
                        bean.setValue(beanBeanPostProcessor.postProcessAfterInitialization(bean.getValue(), bean.getId()));
                    }
                }
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new BeanInstantiationException("Couldn't process \"after\" initialization " + entry.getKey());
            }
        }
        return beans;
    }

    Map<String, Bean> processPostConstruct(Map<String, Bean> beans) {
        for (Map.Entry<String, Bean> entry : beans.entrySet()) {
            try {
                Object object = entry.getValue().getValue();
                for (Method method : object.getClass().getMethods()) {
                    if (method.getAnnotation(PostConstruct.class) != null) {
                        method.invoke(object);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new BeanInstantiationException("Couldn't process \"post\" construct " + entry.getKey());
            }
        }
        return beans;
    }

    @Override
    public Object getBean(String id) {
        Bean bean = beans.get(id);
        if (bean != null) {
            return bean.getValue();
        } else {
            throw new BeanInstantiationException("Unknown bean: " + id);
        }
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Object object = null;
        for (Map.Entry<String, Bean> entry : beans.entrySet()) {
            if (clazz.isAssignableFrom(entry.getValue().getValue().getClass()))
                if (object == null) {
                    object = entry.getValue();
                } else {
                    throw new BeanInstantiationException("Not unique bean for class " + clazz.getName());
                }
        }
        return clazz.cast(object);
    }

    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        Object object = getBean(id);
        if (clazz.isAssignableFrom(object.getClass())) {
            return clazz.cast(object);
        } else {
            throw new BeanInstantiationException("Is not assignable class of bean " + object.getClass().getName() + " for " + clazz.getName());
        }
    }

    Map<String, Bean> constructBeans(List<BeanDefinition> beanDefinitions) {
        Map<String, Bean> beans = new HashMap<>();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> classBean = Class.forName(beanDefinition.getClassName());
                Constructor<?> constructor = classBean.getConstructor();
                Object object = constructor.newInstance();
                Bean bean = new Bean();
                bean.setId(beanDefinition.getId());
                bean.setValue(object);
                beans.put(bean.getId(), bean);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                throw new BeanInstantiationException("Can't construct bean" + beanDefinition.getId(), e);
            }
        }
        return beans;
    }

    Method getMethodByName(String methodName, Class clazz) {
        Method notApplicableMathod = null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                if (method.getParameterCount() == 1) {
                    return method;
                } else {
                    notApplicableMathod = method;
                }
            }
        }
        if (notApplicableMathod != null) {
            throw new BeanInstantiationException("Not suitable method for setter: " + notApplicableMathod.getName());
        } else {
            throw new BeanInstantiationException("Method is not found: " + methodName);
        }
    }

    @SuppressWarnings("unchecked")
    <T> T getValue(String value, Class<T> valueClass) {
        switch (valueClass.getName()) {
            case "int":
            case "java.lang.Integer":
                return (T) Integer.valueOf(value);
            case "long":
            case "java.lang.Long":
                return (T) Long.valueOf(value);
            case "float":
            case "java.lang.Float":
                return (T) Float.valueOf(value);
            case "boolean":
            case "java.lang.Boolean":
                return (T) Boolean.valueOf(value);
            case "java.lang.String":
                return (T) value;
            default:
                throw new BeanInstantiationException("Uncovered type of value: " + valueClass.getName());
        }
    }

    Map<String, Bean> injectValueDepenencies(List<BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                Bean bean = beans.get(beanDefinition.getId());
                Object object = bean.getValue();
                Map<String, String> dependencies = beanDefinition.getValDependencies();
                for (Map.Entry entry : dependencies.entrySet()) {
                    Method method = getMethodByName("set" + entry.getKey(), object.getClass());
                    method.invoke(object, getValue((String) entry.getValue(), method.getParameterTypes()[0]));
                }
            }
            return beans;
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Can't inject value", e);
        }
    }

    Map<String, Bean> injectRefDepenencies(List<BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                Bean bean = beans.get(beanDefinition.getId());
                Object object = bean.getValue();
                Map<String, String> dependencies = beanDefinition.getRefDependencies();
                for (Map.Entry<String, String> entry : dependencies.entrySet()) {
                    Method method = getMethodByName("set" + entry.getKey(), object.getClass());
                    method.invoke(object, beans.get(entry.getValue()).getValue());
                }
            }
            return beans;
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Can't inject reference", e);
        }
    }
}