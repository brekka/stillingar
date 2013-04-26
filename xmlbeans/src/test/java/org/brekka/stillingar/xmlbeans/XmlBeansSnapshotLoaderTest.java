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

import static org.junit.Assert.assertNotNull;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
import org.brekka.stillingar.xmlbeans.conversion.StringConverter;
import org.junit.Test;

/**
 * Test of XmlBeansConfigurationSourceLoader
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class XmlBeansSnapshotLoaderTest {

    
    @Test
    public void testXmlBeansSnapshotLoader() {
        new XmlBeansConfigurationSourceLoader().toString();
    }

    @Test
    public void testXmlBeansSnapshotLoaderConversionManager() {
        ConversionManager manager = new ConversionManager(XmlBeansConfigurationSourceLoader.prepareConverters());
        manager.addConverter(new StringConverter());
        new XmlBeansConfigurationSourceLoader(manager).toString();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testXmlBeansSnapshotLoaderNullConversionManager() {
        new XmlBeansConfigurationSourceLoader((ConversionManager) null).toString();
    }
    
    @Test
    public void testSetXPathNamepaces() {
        DefaultNamespaceContext namespaceContext = new DefaultNamespaceContext("bob", "http://brekka.org/xml");
        new XmlBeansConfigurationSourceLoader(namespaceContext).toString();
    }
    
    @Test
    public void testParseValid() throws Exception {
        XmlBeansConfigurationSourceLoader xmlBeansSnapshotLoader = new XmlBeansConfigurationSourceLoader();
        ConfigurationSource configurationSource = xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("TestConfiguration.xml"), null);
        assertNotNull(configurationSource);
    }
    
    @Test
    public void testParseInvalidNoValidation() throws Exception {
        XmlBeansConfigurationSourceLoader xmlBeansSnapshotLoader = new XmlBeansConfigurationSourceLoader();
        xmlBeansSnapshotLoader.setValidate(false);
        ConfigurationSource configurationSource = xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("JunkConfiguration.xml"), null);
        assertNotNull(configurationSource);
    }
    
    @Test(expected=ConfigurationException.class)
    public void testParseInvalidValidationOn() throws Exception {
        XmlBeansConfigurationSourceLoader xmlBeansSnapshotLoader = new XmlBeansConfigurationSourceLoader();
        xmlBeansSnapshotLoader.setValidate(true);
        xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("JunkConfiguration.xml"), null);
    }
    
    @Test(expected=ConfigurationException.class)
    public void testParseNonXml() throws Exception {
        XmlBeansConfigurationSourceLoader xmlBeansSnapshotLoader = new XmlBeansConfigurationSourceLoader();
        xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("NotXmlConfiguration.txt"), null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testParseNullStream() throws Exception {
        XmlBeansConfigurationSourceLoader xmlBeansSnapshotLoader = new XmlBeansConfigurationSourceLoader();
        xmlBeansSnapshotLoader.parse(null, null);
    }

}
