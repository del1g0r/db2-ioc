package com.study.ioc.context.impl;

import com.study.ioc.context.BeanFactoryPostProcessor;
import com.study.ioc.context.BeanPostProcessor;
import com.study.ioc.context.entity.Bean;
import com.study.ioc.context.entity.BeanDefinition;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassPathApplicationContextTest {

    public static class TestClass {

        private boolean boolValue;
        private Integer intValue;
        private float floatValue;
        private String strValue;
        private boolean flagAfterConstructisExecuted = false;
        private boolean flagNotAfterConstructisExecuted = false;


        public Integer getIntValue() {
            return intValue;
        }

        public void setIntValue(Integer intValue) {
            this.intValue = intValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public void setFloatValue(float floatValue) {
            this.floatValue = floatValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }

        public boolean isBoolValue() {
            return boolValue;
        }

        public void setBoolValue(boolean boolValue) {
            this.boolValue = boolValue;
        }

        public boolean isFlagAfterConstructisExecuted() {
            return flagAfterConstructisExecuted;
        }

        public boolean isNotFlagAfterConstructisExecuted() {
            return flagNotAfterConstructisExecuted;
        }

        @PostConstruct
        public void afterConstruct() {
            flagAfterConstructisExecuted = true;
        }

        public void notAfterConstruct() {
            flagNotAfterConstructisExecuted = true;
        }
    }

    public static class BeanPostProcessorTestImpl implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String id) throws RuntimeException {
            if (id.equals("testClass")) {
                return "Hello world";
            } else {
                return bean;
            }
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String id) throws RuntimeException {
            if (id.equals("testClass")) {
                return "Hello world";
            } else {
                return bean;
            }
        }
    }

    public static class BeanFactoryPostProcessorTestImpl implements BeanFactoryPostProcessor {

        private boolean  flagFactoryExecuted;

        public boolean isFlagFactoryExecuted() {
            return flagFactoryExecuted;
        }

        @Override
        public void postProcessBeanFactory(List<BeanDefinition> definitions) {
            flagFactoryExecuted = true;
        }
    }

    private Map<String, Bean> prepareBeans(Object object, String name) {
        Map<String, Bean> beans = new HashMap<>();
        Bean bean = new Bean();
        bean.setId(name);
        bean.setValue(object);
        beans.put(bean.getId(), bean);
        return beans;
    }

    private List<BeanDefinition> prepareBeanDefinitions(String name) {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setId(name);
        Map<String, String> valDependencies = new HashMap<>();
        valDependencies.put("boolValue", "true");
        valDependencies.put("intValue", "2");
        valDependencies.put("floatValue", "3");
        valDependencies.put("strValue", "hello");
        beanDefinition.setValDependencies(valDependencies);
        beanDefinitions.add(beanDefinition);
        return beanDefinitions;
    }

    @Test
    public void testInjectValueDepenencies() {
        TestClass testClass = new TestClass();
        Map<String, Bean> beans = prepareBeans(testClass, "testClass");
        List<BeanDefinition> beanDefinitions = prepareBeanDefinitions("testClass");

        ClassPathApplicationContext context = new ClassPathApplicationContext();
        context.injectValueDepenencies(beanDefinitions, beans);

        assertTrue(testClass.isBoolValue());
        assertEquals(2, testClass.getIntValue().intValue());
        assertEquals(3, testClass.getFloatValue(), 0);
        assertEquals("hello", testClass.getStrValue());
    }

    @Test
    public void testProcessPostConstruct() {
        TestClass testClass = new TestClass();
        Map<String, Bean> beans = prepareBeans(testClass, "testClass");

        ClassPathApplicationContext context = new ClassPathApplicationContext();
        context.processPostConstruct(beans);

        assertTrue(testClass.isFlagAfterConstructisExecuted());
        assertFalse(testClass.isNotFlagAfterConstructisExecuted());
    }

    @Test
    public void testPostProcessBeforeInitialization() {
        String testClass = "123";
        Map<String, Bean> beans = prepareBeans(testClass, "testClass");
        BeanPostProcessorTestImpl testPostProcessor = new BeanPostProcessorTestImpl();
        Bean bean = new Bean();
        bean.setId("postProcessor");
        bean.setValue(testPostProcessor);
        beans.put(bean.getId(), bean);

        ClassPathApplicationContext context = new ClassPathApplicationContext();
        context.postProcessBeforeInitialization(beans);

        assertEquals("Hello world", beans.get("testClass").getValue());
    }
}


