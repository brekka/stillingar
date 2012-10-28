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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
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
public class JAXBConfigurationSource implements ConfigurationSource {

    /**
     * The DOM representation of the XML. XPath expressions will be applied against this
     */
    private final Document document;

    /**
     * JAXB object representation of the XML
     */
    private final Object object;

    /**
     * Namespace context to use in XPath operations (can be null).
     */
    private final NamespaceContext xPathNamespaceContext;

    /**
     * @param object
     */
    public JAXBConfigurationSource(Document document, Object object, NamespaceContext xPathNamespaceContext) {
        this.document = document;
        this.object = object;
        this.xPathNamespaceContext = xPathNamespaceContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    @Override
    public boolean isAvailable(String expression) {
        Object result = doXPath(expression, XPathConstants.NODESET);
        if (result instanceof NodeList) {
            NodeList nodeList = (NodeList) result;
            return nodeList.getLength() > 0;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T retrieve(String expression, Class<T> valueType) {
        T retVal;
        Object result = doXPath(expression, XPathConstants.NODE);
        if (result instanceof Node) {
            Node node = (Node) result;
            if (valueType.isAssignableFrom(result.getClass())) {
                retVal = (T) node;
            } else {
                Object resolvedObject = resolveObject(node);
                if (resolvedObject == null) {
                    retVal = null;
                } else if (valueType.isAssignableFrom(resolvedObject.getClass())) {
                    retVal = (T) resolvedObject;
                } else if (List.class.isAssignableFrom(resolvedObject.getClass())) {
                    retVal = resolveValueFromList(node, (List<T>) resolvedObject);
                } else {
                    throw new ConfigurationException(String.format(
                            "Expected '%s', found '%s'", valueType.getName(),
                            resolvedObject.getClass().getName()));
                }
            }
        } else {
            throw new ConfigurationException(String.format(
                    "Not a single node '%s', found '%s'", expression, result));
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        List<T> retVal;
        if (Node.class.isAssignableFrom(valueType)) {
            // Regular DOM
            Object result = doXPath(expression, XPathConstants.NODESET);
            if (result instanceof NodeList) {
                NodeList nodeList = (NodeList) result;
                retVal = new ArrayList<T>(nodeList.getLength());
                for (int i = 0; i < nodeList.getLength(); i++) {
                    retVal.add((T) nodeList.item(i));
                }
            } else {
                throw new ConfigurationException(String.format(
                        "Not a list of nodes '%s', found '%s'", expression, result));
            }
        } else {
            // Use JAXB
            Object result = doXPath(expression, XPathConstants.NODE);
            if (result instanceof Node) {
                Node node = (Node) result;
                Object resolvedObject = resolveObject(node);
                if (resolvedObject == null) {
                    retVal = null;
                } else if (List.class.isAssignableFrom(resolvedObject.getClass())) {
                    retVal = (List<T>) resolvedObject;
                } else {
                    throw new ConfigurationException(String.format(
                            "Expected list, found '%s'", valueType.getName(),
                            resolvedObject.getClass().getName()));
                }
            } else {
                throw new ConfigurationException(String.format(
                        "Not a list of nodes '%s', found '%s'", expression, result));
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    @Override
    public boolean isAvailable(Class<?> valueType) {
        return find(valueType);
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    @Override
    public <T> T retrieve(Class<T> valueType) {
        T retVal;
        List<T> values = new ArrayList<T>();
        collect(object, valueType, values);
        if (values.size() == 0) {
            retVal = null;
        } else if (values.size() == 1) {
            retVal = values.get(0);
        } else {
            throw new ConfigurationException(String.format("Found %d instances of '%s'", values.size(),
                    valueType.getName()));
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(Class<T> valueType) {
        List<T> values = new ArrayList<T>();
        collect(object, valueType, values);
        return values;
    }
    

    protected Object doXPath(String expression, QName returnType) {
        Object retVal;
        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xpath = xFactory.newXPath();
        if (xPathNamespaceContext != null) {
            xpath.setNamespaceContext(xPathNamespaceContext);
        }
        try {
            XPathExpression expr = xpath.compile(expression);
            retVal = expr.evaluate(document, returnType);
        } catch (XPathExpressionException e) {
            throw new ConfigurationException(String.format(
                    "Invalid XPath expression '%s'", expression, e));
        }
        return retVal;
    }
    
    protected boolean find(Class<?> lookingFor) {
        Map<Object, Void> seen = new IdentityHashMap<Object, Void>();
        return collect(object, lookingFor, seen, null);
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

        Field[] fieldArr = parentObj.getClass().getDeclaredFields();
        for (Field field : fieldArr) {
            XmlElement xmlElement = field.getAnnotation(XmlElement.class);
            if (node.getLocalName().equals(xmlElement.name())) {
                return extractFieldValue(parentObj, field);
            }
        }
        return null;
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
            throw new ConfigurationException(String.format("Unable to reliably identify the corresponding" +
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
        
        throw new ConfigurationException(String.format("Failed to find the correct indexed node " +
        		"(DOM had %d children, JAXB had %d candidates)", childNodes.getLength(), list.size()));
    }


    protected static <T> boolean collect(Object current, Class<T> lookingFor, List<T> values) {
        Map<Object, Void> seen = new IdentityHashMap<Object, Void>();
        return collect(current, lookingFor, seen, values);
    }

    @SuppressWarnings("unchecked")
    protected static <T> boolean collect(Object current, Class<T> lookingFor, Map<Object, Void> seen, List<T> values) {
        if (current == null) {
            return false;
        }
        if (seen.containsKey(current)) {
            return false;
        }
        Class<? extends Object> currentClass = current.getClass();
        if (currentClass == lookingFor) {
            if (values != null) {
                values.add((T) current);
            }
            return true;
        }
        seen.put(current, null);

        if (currentClass.getAnnotation(XmlType.class) == null) {
            return false;
        }

        Field[] declaredFields = current.getClass().getDeclaredFields();
        boolean found = false;
        for (Field field : declaredFields) {
            if (field.getAnnotation(XmlElement.class) == null) {
                continue;
            }
            Object fieldValue = extractFieldValue(current, field);
            boolean result = collect(fieldValue, lookingFor, seen, values);
            if (result 
                    && values == null) {
                return true;
            }
            found |= result;
        }
        return found;
    }

    /**
     * @param current
     * @param field
     * @return
     */
    protected static Object extractFieldValue(Object current, Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object next;
        try {
            next = field.get(current);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(String.format("Unable to access the value of field '%s' of object " +
            		"with type '%s'", field.getName(), current.getClass().getName()), e);
        }
        return next;
    }

}
