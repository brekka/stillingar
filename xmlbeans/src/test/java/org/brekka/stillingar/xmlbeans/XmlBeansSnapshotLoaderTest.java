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

import java.util.Collections;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.xmlbeans.conversion.StringConverter;
import org.junit.Test;

/**
 * Test of XmlBeansSnapshotLoader
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class XmlBeansSnapshotLoaderTest {

    
    @Test
    public void testXmlBeansSnapshotLoader() {
        new XmlBeansSnapshotLoader().toString();
    }

    @Test
    public void testXmlBeansSnapshotLoaderConversionManager() {
        ConversionManager manager = new ConversionManager(XmlBeansSnapshotLoader.CONVERTERS);
        manager.addConverter(new StringConverter());
        new XmlBeansSnapshotLoader(manager).toString();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testXmlBeansSnapshotLoaderNullConversionManager() {
        new XmlBeansSnapshotLoader(null).toString();
    }
    
    @Test
    public void testSetXPathNamepaces() {
        new XmlBeansSnapshotLoader().setXpathNamespaces(Collections.<String,String>emptyMap());
    }
    @Test(expected=IllegalArgumentException.class)
    public void testSetNullXPathNamepaces() {
        new XmlBeansSnapshotLoader().setXpathNamespaces(null);
    }

    @Test
    public void testParseValid() throws Exception {
        XmlBeansSnapshotLoader xmlBeansSnapshotLoader = new XmlBeansSnapshotLoader();
        ConfigurationSource configurationSource = xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("TestConfiguration.xml"), null);
        assertNotNull(configurationSource);
    }
    
    @Test
    public void testParseInvalidNoValidation() throws Exception {
        XmlBeansSnapshotLoader xmlBeansSnapshotLoader = new XmlBeansSnapshotLoader();
        xmlBeansSnapshotLoader.setValidate(false);
        ConfigurationSource configurationSource = xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("JunkConfiguration.xml"), null);
        assertNotNull(configurationSource);
    }
    
    @Test(expected=ConfigurationException.class)
    public void testParseInvalidValidationOn() throws Exception {
        XmlBeansSnapshotLoader xmlBeansSnapshotLoader = new XmlBeansSnapshotLoader();
        xmlBeansSnapshotLoader.setValidate(true);
        xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("JunkConfiguration.xml"), null);
    }
    
    @Test(expected=ConfigurationException.class)
    public void testParseNonXml() throws Exception {
        XmlBeansSnapshotLoader xmlBeansSnapshotLoader = new XmlBeansSnapshotLoader();
        xmlBeansSnapshotLoader.parse(getClass().getResourceAsStream("NotXmlConfiguration.txt"), null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testParseNullStream() throws Exception {
        XmlBeansSnapshotLoader xmlBeansSnapshotLoader = new XmlBeansSnapshotLoader();
        xmlBeansSnapshotLoader.parse(null, null);
    }

}
