package com.study.ioc.context.entity;

import java.util.Map;

public class BeanDefinition {

    private String id;
    private String className;
    private Map<String, String> valDependencies;
    private Map<String, String> refDependencies;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getValDependencies() {
        return valDependencies;
    }

    public void setValDependencies(Map<String, String> valDependencies) {
        this.valDependencies = valDependencies;
    }

    public Map<String, String> getRefDependencies() {
        return refDependencies;
    }

    public void setRefDependencies(Map<String, String> refDependencies) {
        this.refDependencies = refDependencies;
    }
}
