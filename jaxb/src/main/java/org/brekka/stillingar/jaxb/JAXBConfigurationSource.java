/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.stillingar.jaxb;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.brekka.stillingar.api.ValueConfigurationException;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.dom.DOMConfigurationSource;
import org.brekka.stillingar.core.support.BeanReflectionHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configuration source based on JAXB. Due to the lack of direct XPath support in JAXB, a standard DOM model will also
 * be retained and used as the basis for XPath operations. Some simple logic then finds to corresponding JAXB bean.
 * 
 * It should be noted that searches by type are performed by traversing the JAXB object model graph. 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBConfigurationSource extends DOMConfigurationSource {

    /**
     * JAXB object representation of the XML
     */
    private final Object object;
    private final BeanReflectionHelper reflectionHelper;

    /**
     * @param document
     * @param object
     * @param xPathNamespaceContext
     * @param conversionManager
     */
    public JAXBConfigurationSource(Document document, Object object, NamespaceContext xPathNamespaceContext, ConversionManager conversionManager) {
        super(document, xPathNamespaceContext, conversionManager);
        this.object = object;
        this.reflectionHelper = new JAXBBeanReflectionHelper(object);
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    @Override
    public boolean isAvailable(Class<?> valueType) {
        return reflectionHelper.isAvailable(valueType);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.dom.DOMConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T retrieve(String expression, Class<T> valueType) {
        T retVal;
        if (isJaxb(valueType)) {
            Object obj = doXPath(expression, XPathConstants.NODE, valueType);
            if (obj instanceof Node) {
                Node node = (Node) obj;
                Object resolvedObject = toJaxbObject(node, valueType);
                if (resolvedObject == null) {
                    retVal = null;
                } else if (valueType.isAssignableFrom(resolvedObject.getClass())) {
                    retVal = (T) resolvedObject;
                } else if (List.class.isAssignableFrom(resolvedObject.getClass())) {
                    try {
                        retVal = resolveValueFromList(node, (List<T>) resolvedObject);
                    } catch (IllegalStateException e) {
                        throw new ValueConfigurationException(format(
                                "Failed identify correct element from list", resolvedObject.getClass().getName()
                                ), valueType, expression, e);
                    }
                } else if (valueType.isPrimitive()
                        && !resolvedObject.getClass().isPrimitive()) {
                    retVal = (T) resolvedObject;
                } else {
                    throw new ValueConfigurationException(format(
                            "Unable to handle result type '%s'", resolvedObject.getClass().getName()
                            ), valueType, expression);
                }
            } else {
                throw new ValueConfigurationException(format(
                        "Result is not a single node, it is instead: '%s'", 
                        obj.getClass().getName()), valueType, expression);
            }
        } else {
            retVal = super.retrieve(expression, valueType);
        }
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.dom.DOMConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        List<T> valueList;
        if (isJaxb(valueType)) {
            Object obj = doXPath(expression, XPathConstants.NODE, valueType);
            if (obj instanceof Node) {
                Node node = (Node) obj;
                Object resolvedObject = toJaxbObject(node, valueType);
                if (resolvedObject == null) {
                    valueList = null;
                } else if (List.class.isAssignableFrom(resolvedObject.getClass())) {
                    valueList = (List<T>) resolvedObject;
                } else {
                    throw new ValueConfigurationException(format(
                            "Unable to handle non-list based result type '%s'", 
                            resolvedObject.getClass().getName()
                            ), valueType, expression);
                }
            } else {
                throw new ValueConfigurationException(format(
                        "Result is not a single node, it is instead: '%s'", 
                        obj.getClass().getName()), valueType, expression);
            }
        } else {
            valueList = super.retrieveList(expression, valueType);
        }
        return valueList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    @Override
    public <T> T retrieve(Class<T> valueType) {
        return reflectionHelper.findValueOf(valueType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(Class<T> valueType) {
        return reflectionHelper.findListOf(valueType);
    }
    
    protected boolean isJaxb(Class<?> valueType) {
        return valueType == byte[].class      // Force JAXB to handle byte arrays (limited support in DOM)
                || valueType == Calendar.class // Force JAXB to handle Calendar (limited support in DOM)
                || valueType.getAnnotation(XmlType.class) != null;
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T toJaxbObject(Node node, Class<T> expectedType) {
        // Special handling for document
        if (expectedType == Document.class) {
            return getConversionManager().convert(node, expectedType);
        }
        
        // Resolve the object using JAXB
        Object value = resolveObject(node);
        if (value != null) {
            if (getConversionManager().hasConverter(expectedType)) {
                if (value instanceof List) {
                    List<?> valueList = (List<?>) value;
                    List<Object> changed = new ArrayList<Object>();
                    for (Object obj : valueList) {
                        changed.add(getConversionManager().convert(obj, expectedType));
                    }
                    value = changed;
                } else {
                    value = getConversionManager().convert(value, expectedType);
                }
            }
        }
        return (T) value;
    }
    
    /**
     * @param node
     * @return
     */
    protected Object resolveObject(Node node) {
        Node parentNode = node.getParentNode();
        Object parentObj;
        if (parentNode instanceof Document) {
            return this.object;
        }
        parentObj = resolveObject(parentNode);

        Object value = null;
        Field[] fieldArr = parentObj.getClass().getDeclaredFields();
        for (Field field : fieldArr) {
            XmlElement xmlElement = field.getAnnotation(XmlElement.class);
            if (xmlElement != null) {
                if (node.getLocalName().equals(xmlElement.name())) {
                    value = BeanReflectionHelper.extractFieldValue(parentObj, field);
                }
            }
        }
        return value;
    }
    
    /**
     * @param node
     * @param resolvedObject
     * @return
     */
    protected static <T> T resolveValueFromList(Node node, List<T> list) {
        Node parentNode = node.getParentNode();
        NodeList childNodes = parentNode.getChildNodes();
        List<Node> nodeList = new ArrayList<Node>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(node.getNodeName())  
                    && child.getNamespaceURI().equals(node.getNamespaceURI())) {
                nodeList.add(child);
            }
        }
        
        if (list.size() != nodeList.size()) {
            throw new IllegalStateException(String.format("Unable to reliably identify the corresponding" +
                    " JAXB object as there are %d candidates and only %d DOM nodes. This is most likely" +
                    " a result of the expression containing a more complex selector than just index based lookup.", 
                    list.size(), childNodes.getLength()));
        }
        
        for (int i = 0; i < nodeList.size(); i++) {
            Node child = nodeList.get(i);
            if (child == node) {
                return list.get(i);
            }
        }
        
        throw new IllegalStateException(String.format("Failed to find the correct indexed node " +
        		"(DOM had %d children, JAXB had %d candidates)", childNodes.getLength(), list.size()));
    }
}
