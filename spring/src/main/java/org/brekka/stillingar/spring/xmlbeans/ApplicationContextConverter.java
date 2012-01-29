package org.brekka.stillingar.spring.xmlbeans;

import javax.xml.transform.TransformerFactory;

import org.apache.xmlbeans.XmlObject;
import org.brekka.stillingar.xmlbeans.conversion.AbstractTypeConverter;
import org.brekka.stillingar.xmlbeans.conversion.DocumentConverter;
import org.brekka.stillingar.xmlbeans.conversion.ElementConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ApplicationContextConverter extends AbstractTypeConverter<ApplicationContext> implements ApplicationContextAware {

    private final TransformerFactory factory;
    
    private ApplicationContext applicationContext;
    
    public ApplicationContextConverter() {
        this(TransformerFactory.newInstance());
    }
    
    public ApplicationContextConverter(TransformerFactory factory) {
        this.factory = factory;
    }
    

    @Override
    public ApplicationContext convert(XmlObject xmlValue) {
        Element element = ElementConverter.xmlObjectToElement(xmlValue);
        Document document = DocumentConverter.elementToDocument(element, factory);
        GenericApplicationContext context;
        if (applicationContext == null) {
            context = new GenericApplicationContext();
        } else {
            context = new GenericApplicationContext(applicationContext);
        }
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        reader.registerBeanDefinitions(document, null);
        context.refresh();
        return context;
    }
    
    @Override
    public Class<ApplicationContext> targetType() {
        return ApplicationContext.class;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
