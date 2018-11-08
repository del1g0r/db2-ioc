package com.study.ioc.context.impl;

import com.study.ioc.context.*;
import com.study.ioc.context.exception.BeanInstantiationException;
import com.study.ioc.context.entity.Bean;
import com.study.ioc.context.entity.BeanDefinition;

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
        this.beans = injectRefDepenencies(beanDefinitions
                , injectValueDepenencies(beanDefinitions
                        , constructBeans(beanDefinitions))
        );
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
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        Iterator iterator = beans.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Bean bean = (Bean) entry.getValue();
            Object object = bean.getValue();
            if (object.getClass().equals(clazz))
                return (T) object;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String id, Class<T> clazz) {
        Object object = getBean(id);
        return (T) object;
    }

    Map<String, Bean> constructBeans(List<BeanDefinition> beanDefinitions) {
        try {
            Map<String, Bean> beans = new HashMap<>();
            for (BeanDefinition beanDefinition : beanDefinitions) {
                Class<?> clazz = Class.forName(beanDefinition.getClassName());
                Constructor<?> constructor = clazz.getConstructor();
                Object object = constructor.newInstance();
                Bean bean = new Bean();
                bean.setId(beanDefinition.getId());
                bean.setValue(object);
                beans.put(bean.getId(), bean);
            }
            return beans;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Can't construct bean", e);
        }
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

    Map<String, Bean> injectRefDepenencies
            (List<BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        try {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                Bean bean = beans.get(beanDefinition.getId());
                Object object = bean.getValue();
                Map<String, String> dependencies = beanDefinition.getRefDependencies();
                for (Map.Entry<String,String> entry : dependencies.entrySet()) {
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