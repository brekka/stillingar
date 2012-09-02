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

package org.brekka.stillingar.xmlbeans;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.xmlbeans.conversion.ConversionManager;
import org.brekka.xml.stillingar.test.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.CompanyX;
import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.CompanyY;
import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.Services.Rules.Fraud;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Description of XmlBeansConfigurationSourceTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class XmlBeansConfigurationSourceTest {
    
    private XmlBeansConfigurationSource configurationSource;
    
    @Before
    public void setup() throws Exception {
        ConfigurationDocument document = ConfigurationDocument.Factory.parse(getClass().getResourceAsStream("TestConfiguration.xml"));
        Map<String, String> nsMap = new HashMap<String, String>();
        nsMap.put("c", "http://brekka.org/xml/stillingar/test/v1");
        configurationSource = new XmlBeansConfigurationSource(document, nsMap, new ConversionManager());
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
    
//    @Test
//    public void testRetrieveXPathIndex() {
//        String keyword = configurationSource.retrieve("//c:Fraud/c:Keyword[2]", String.class);
//        assertEquals("KeywordB", keyword);
//    }

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
