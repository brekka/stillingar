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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.brekka.stillingar.test.jaxb.Configuration.CompanyX;
import org.brekka.stillingar.test.jaxb.Configuration.CompanyY;
import org.brekka.stillingar.test.jaxb.Configuration.Services.Rules.Fraud;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * TODO Description of JAXBConfigurationSourceTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBConfigurationSourceTest {


    private JAXBConfigurationSource configurationSource;
    
    @Before
    public void setup() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(getClass().getResourceAsStream("TestConfiguration.xml"));
        JAXBContext jc = JAXBContext.newInstance("org.brekka.stillingar.test.jaxb");
        Unmarshaller u = jc.createUnmarshaller();
        Object object = u.unmarshal(document);
        NamespaceContext namespaceContext = new NamespaceContext() {
            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return Arrays.asList("c").iterator();
            }
            
            @Override
            public String getPrefix(String namespaceURI) {
                return "c";
            }
            
            @Override
            public String getNamespaceURI(String prefix) {
                return "http://brekka.org/xml/stillingar/test/v1";
            }
        };
        configurationSource = new JAXBConfigurationSource(document, object, namespaceContext);
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test
    public void testIsAvailableClassTrue() {
        assertTrue(configurationSource.isAvailable(CompanyY.class));
    }
    
    @Test
    public void testIsAvailableClassFalse() {
        assertFalse(configurationSource.isAvailable(CompanyX.class));
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsAvailableXPathTrue() {
        assertTrue(configurationSource.isAvailable("//c:CompanyY"));
    }
    
    @Test
    public void testIsAvailableXPathFalse() {
        assertFalse(configurationSource.isAvailable("//c:CompanyX"));
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test
    public void testRetrieveClass() {
        CompanyY companyY = configurationSource.retrieve(CompanyY.class);
        assertNotNull(companyY);
        assertEquals("http://example.org/CompanyY", companyY.getWarehouseWebService().getURL());
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveXPath() {
        CompanyY companyY = configurationSource.retrieve("//c:CompanyY", CompanyY.class);
        assertNotNull(companyY);
        assertEquals("http://example.org/CompanyY", companyY.getWarehouseWebService().getURL());
    }
    
    @Test
    public void testRetrieveXPathIndex() {
        String keyword = configurationSource.retrieve("//c:Fraud/c:Keyword[2]", String.class);
        assertEquals("KeywordB", keyword);
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test
    public void testRetrieveListClass() {
        List<Fraud> list = configurationSource.retrieveList(Fraud.class);
        assertNotNull(list);
        assertTrue(list.size() == 1);
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveListXPath() {
        List<String> list = configurationSource.retrieveList("//c:Fraud/c:Keyword", String.class);
        assertNotNull(list);
        assertEquals(Arrays.asList("KeywordA", "KeywordB", "KeywordC"), list);
    }

}
