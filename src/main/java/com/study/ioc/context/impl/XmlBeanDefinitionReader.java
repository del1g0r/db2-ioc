package com.study.ioc.context.impl;

import com.study.ioc.context.entity.BeanDefinition;
import com.study.ioc.context.BeanDefinitionReader;
import com.study.ioc.context.exception.BeanInstantiationException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XmlBeanDefinitionReader implements BeanDefinitionReader {

    private InputStream stream;

    private static class BeanDefinitionHandler extends DefaultHandler {

        BeanDefinitionHandler() {
            beanDefinitions = new ArrayList<>();
        }

        private BeanDefinition beanDefinition;
        private List<BeanDefinition> beanDefinitions;

        List<BeanDefinition> getBeanDefinitions() {
            return beanDefinitions;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equalsIgnoreCase("bean")) {
                String id = attributes.getValue("id");
                String className = attributes.getValue("class");
                beanDefinition = new BeanDefinition();
                beanDefinition.setId(id);
                beanDefinition.setClassName(className);
                beanDefinition.setValDependencies(new HashMap<>());
                beanDefinition.setRefDependencies(new HashMap<>());
            } else if (qName.equalsIgnoreCase("property")) {
                String name = attributes.getValue("name");
                String value = attributes.getValue("value");
                String ref = attributes.getValue("ref");
                if (name != null) {
                    if (value != null) {
                        beanDefinition.getValDependencies().put(name, value);
                    } else if (ref != null) {
                        beanDefinition.getRefDependencies().put(name, ref);
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase("bean")) {
                beanDefinitions.add(beanDefinition);
            }
        }
    }

    public XmlBeanDefinitionReader(InputStream stream) {
        this.stream = stream;
    }

    public XmlBeanDefinitionReader(String path) {
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Can't create stream for path " + path, e);
        }
    }

    @Override
    public List<BeanDefinition> readBeanDefinitions() {
        try {
            BeanDefinitionHandler handler = new BeanDefinitionHandler();
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(handler);
            InputSource source = new InputSource(stream);
            parser.parse(source);
            return handler.getBeanDefinitions();
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Can't read definitiona", e);
        }
    }
}