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

package org.brekka.stillingar.core.properties;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.brekka.stillingar.api.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

/**
 * PropertiesConfigurationSource Test
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PropertiesConfigurationSourceTest {

    private PropertiesConfigurationSource configurationSource;
    
    @Before
    public void setup() {
        Properties p = new Properties();
        p.setProperty("key1", "test1");
        p.setProperty("key2.0", "testA");
        p.setProperty("key2.1", "testB");
        p.setProperty("key2.2", "testC");
        p.setProperty("intKey", "256");
        p.setProperty("longKey", "45678912312");
        p.setProperty("shortKey", "223");
        p.setProperty("floatKey", "85.69");
        p.setProperty("doubleKey", "8589897474565965547558.69");
        p.setProperty("booleanKey", "true");
        configurationSource = new PropertiesConfigurationSource(p);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsAvailableString() {
        assertTrue(configurationSource.isAvailable("key1"));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsNotAvailableString() {
        assertFalse(configurationSource.isAvailable("key1.not"));
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testIsAvailableClassOfQ() {
        configurationSource.isAvailable(String.class);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveClassOfT() {
        configurationSource.retrieve(String.class);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveString() {
        assertEquals("test1", configurationSource.retrieve("key1", String.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveInteger() {
        assertEquals(Integer.valueOf(256), configurationSource.retrieve("intKey", Integer.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveLong() {
        assertEquals(Long.valueOf(45678912312L), configurationSource.retrieve("longKey", Long.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveShort() {
        assertEquals(Short.valueOf((short) 223), configurationSource.retrieve("shortKey", Short.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveFloat() {
        assertEquals(Float.valueOf(85.69f), configurationSource.retrieve("floatKey", Float.class));
    }
    
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveDouble() {
        assertEquals(Double.valueOf(8589897474565965547558.69d), configurationSource.retrieve("doubleKey", Double.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveBoolean() {
        assertEquals(Boolean.TRUE, configurationSource.retrieve("booleanKey", Boolean.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveUnknown() {
        configurationSource.retrieve("floatKey", URL.class);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveNotFound() {
        assertNull(configurationSource.retrieve("noKey", String.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveListClassOfT() {
        configurationSource.retrieveList(String.class);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveList() {
        assertEquals(Arrays.asList("testA", "testB", "testC"), configurationSource.retrieveList("key2", String.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveListDirectHit() {
        assertEquals(Arrays.asList("test1"), configurationSource.retrieveList("key1", String.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveListNotFound() {
        assertEquals(Collections.emptyList(), configurationSource.retrieveList("nolistKey", String.class));
    }

}
