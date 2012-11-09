/*
 * Copyright 2011 the original author or authors.
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

package org.brekka.stillingar.core.dom;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ValueConfigurationException;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {@link ConfigurationSource} implementation that is backed by a DOM {@link Document} instance.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DOMConfigurationSource implements ConfigurationSource {

    /**
     * The document from which configuration values will be resolved.
     */
    private final Document document;
    
    /**
     * The conversion manager
     */
    private final ConversionManager conversionManager;

    /**
     * Namespace context to use in XPath operations (can be null).
     */
    private final NamespaceContext xPathNamespaceContext;
    
    
    /**
     * @param document
     */
    public DOMConfigurationSource(Document document, NamespaceContext xPathNamespaceContext) {
        this(document, xPathNamespaceContext, new ConversionManager(DOMConfigurationSourceLoader.CONVERTERS));
    }

    /**
     * @param document
     *            The document from which configuration values will be resolved.
     */
    public DOMConfigurationSource(Document document, NamespaceContext xPathNamespaceContext, ConversionManager conversionManager) {
        this.document = document;
        this.conversionManager = conversionManager;
        this.xPathNamespaceContext = xPathNamespaceContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    @Override
    public boolean isAvailable(String expression) {
        Object result = doXPath(expression, XPathConstants.NODESET, null);
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
    @Override
    public <T> T retrieve(String expression, Class<T> valueType) {
        T retVal;
        NodeList results = doXPath(expression, XPathConstants.NODESET, valueType);
        if (results.getLength() == 1) {
            Node node = results.item(0);
            retVal = toObject(node, valueType, expression);
        } else if (results.getLength() == 0) {
            throw new ValueConfigurationException(
                    "No value found matching expression", valueType, expression);
        } else {
            throw new ValueConfigurationException(format(
                    "Expected single result for this expression, found %d", results.getLength()), 
                    valueType, expression);
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        NodeList nodeList = doXPath(expression, XPathConstants.NODESET, valueType);
        List<T> retVal = new ArrayList<T>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            T value = toObject(node, valueType, expression);
            retVal.add(value);
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
        throw new ValueConfigurationException(
                "Not supported using DOM", null, null);
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    @Override
    public <T> T retrieve(Class<T> valueType) {
        throw new ValueConfigurationException(
                "An expression must be specified when using DOM", null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(Class<T> valueType) {
        throw new ValueConfigurationException(
                "An expression must be specified when using DOM", null, null);
    }
    

    protected NodeList doXPath(String expression, QName returnQName, Class<?> returnType) {
        NodeList retVal;
        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xpath = xFactory.newXPath();
        if (xPathNamespaceContext != null) {
            xpath.setNamespaceContext(xPathNamespaceContext);
        }
        try {
            XPathExpression expr = xpath.compile(expression);
            Object result = expr.evaluate(document, returnQName);
            if (result instanceof NodeList) {
                retVal = (NodeList) result;
            } else {
                throw new ValueConfigurationException(format(
                        "Result is not a list of nodes, it is instead: '%s'", 
                        result), returnType, expression);
            }
        } catch (XPathExpressionException e) {
            throw new ValueConfigurationException(
                    "Not a vaild XPath expression",  returnType, expression, e);
        }
        return retVal;
    }
    

    /**
     * @param node
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T> T toObject(Node node, Class<T> valueType, String expression) {
        if (Object.class == valueType) {
            // Not expecting anything in particular
            return (T) node;
        } 
        T retVal;
        Object value = node;
        if (!Node.class.isAssignableFrom(valueType)) {
            // Non-node, extract text content
            value = node.getTextContent();
        }
        if (conversionManager.hasConverter(valueType)) {
            retVal = conversionManager.convert(value, valueType);
        } else {
            throw new ValueConfigurationException(format(
                    "No conversion available from type '%s'", value.getClass()
                    .getName()), valueType, expression);
        }
        return retVal;
    }

    
}
