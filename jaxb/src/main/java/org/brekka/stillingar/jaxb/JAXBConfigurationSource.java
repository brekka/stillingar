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

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.brekka.stillingar.core.ConfigurationSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * TODO Description of JAXBConfigurationSource
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBConfigurationSource implements ConfigurationSource {

    private final Document document;
    
    private final Object object;
    
    private final NamespaceContext xPathNamespaceContext;
    
    /**
     * @param object
     */
    public JAXBConfigurationSource(Document document, Object object, NamespaceContext xPathNamespaceContext) {
        this.document = document;
        this.object = object;
        this.xPathNamespaceContext = xPathNamespaceContext;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    @Override
    public boolean isAvailable(String expression) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    @Override
    public boolean isAvailable(Class<?> valueType) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T retrieve(String expression, Class<T> valueType) {
        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xpath = xFactory.newXPath();
        xpath.setNamespaceContext(xPathNamespaceContext);
        try {
            XPathExpression expr = xpath.compile(expression);
            Object result = expr.evaluate(document, XPathConstants.NODE);
            if (result instanceof Node) {
                Node node = (Node) result;
            }
            System.out.println(result);
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    @Override
    public <T> T retrieve(Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

}
