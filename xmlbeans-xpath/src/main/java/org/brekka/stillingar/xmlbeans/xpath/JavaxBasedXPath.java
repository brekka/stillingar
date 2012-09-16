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

package org.brekka.stillingar.xmlbeans.xpath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.xmlbeans.impl.store.PathDelegate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XmlBeans XPath implementation based on the standard XPath functionality found in Java 5+. It is provided as an
 * alternative to the org.apache.xmlbeans/xmlbeans-xpath module that is based on Saxon.
 * 
 * Based on the code from org.apache.xmlbeans.impl.xpath.saxon.XBeansXPath.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 * @see org.apache.xmlbeans.impl.xpath.saxon.XBeansXPath
 */
public class JavaxBasedXPath implements PathDelegate.SelectPathInterface {

    /**
     * The namespace context
     */
    private final NamespaceContext namespaceMap;
    
    /**
     * The path expression
     */
    private final String path;
    
    /**
     * A context variable (?)
     */
    private final String contextVar;
    
    /**
     * Construct given an XPath expression string.
     * 
     * @param path
     *            The XPath expression
     * @param contextVar
     *            The name of the context variable
     * @param namespaceMap
     *            a map of prefix/uri bindings for NS support
     * @param defaultNS
     *            the uri for the default element NS, if any
     */
    public JavaxBasedXPath(String path, String contextVar, Map<String, String> namespaceMap, String defaultNS) {
        this.path = path;
        this.contextVar = contextVar;
        this.namespaceMap = new MapNamespaceContext(namespaceMap, defaultNS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.xmlbeans.impl.store.PathDelegate.SelectPathInterface#selectPath(java.lang.Object)
     */
    @Override
    public List<Node> selectPath(Object node) {
        List<Node> results = new ArrayList<Node>();
        Node contextNode = (Node) node;
        XPathFactory xFactory = XPathFactory.newInstance();
        xFactory.setXPathVariableResolver(new XPathVariableResolver() {
            @Override
            public Object resolveVariable(QName variableName) {
                return contextVar;
            }
        });
        XPath xpath = xFactory.newXPath();
        if (namespaceMap != null) {
            xpath.setNamespaceContext(namespaceMap);
        }
        try {
            XPathExpression expr = xpath.compile(path);
            NodeList nodeList = (NodeList) expr.evaluate(contextNode, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                results.add(nodeList.item(i));
            }
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid XPath expression '%s'", path), e);
        }
        return results;
    }

}
