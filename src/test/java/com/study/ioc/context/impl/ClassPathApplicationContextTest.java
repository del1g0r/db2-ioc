package com.study.ioc.context.impl;

import com.study.ioc.context.entity.Bean;
import com.study.ioc.context.entity.BeanDefinition;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassPathApplicationContextTest {

    public static class TestClass {

        private boolean boolValue;
        private Integer intValue;
        private float floatValue;
        private String strValue;


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
    }

    @Test
    public void testInjectValueDepenencies() throws Exception {

        Map<String, Bean> beans = new HashMap<>();
        Bean bean = new Bean();
        bean.setId("testClass");
        TestClass testClass = new TestClass();
        bean.setValue(testClass);
        beans.put(bean.getId(), bean);

        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setId("testClass");
        Map<String, String> valDependencies = new HashMap<>();
        valDependencies.put("boolValue", "true");
        valDependencies.put("intValue", "2");
        valDependencies.put("floatValue", "3");
        valDependencies.put("strValue", "hello");
        beanDefinition.setValDependencies(valDependencies);
        beanDefinitions.add(beanDefinition);

        ClassPathApplicationContext context = new ClassPathApplicationContext();
        context.injectValueDepenencies(beanDefinitions, beans);

        assertTrue(testClass.isBoolValue());
        assertEquals(2, testClass.getIntValue().intValue());
        assertEquals(3, testClass.getFloatValue(), 0);
        assertEquals("hello", testClass.getStrValue());
    }
}


