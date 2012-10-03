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

import java.nio.charset.Charset;

import org.brekka.stillingar.core.ConfigurationSource;
import org.junit.Test;

/**
 * PropertiesConfigurationSourceLoader Test
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PropertiesConfigurationSourceLoaderTest {

    /**
     * Test method for {@link org.brekka.stillingar.core.properties.PropertiesConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)}.
     */
    @Test
    public void testParseUTF8() throws Exception {
        PropertiesConfigurationSourceLoader loader = new PropertiesConfigurationSourceLoader();
        ConfigurationSource configurationSource = loader.parse(getClass().getResourceAsStream("config_UTF-8.properties"), Charset.forName("UTF-8"));
        assertEquals("rækjusalat", configurationSource.retrieve("key1", String.class));
    }
    
    @Test
    public void testParseISO88591() throws Exception {
        PropertiesConfigurationSourceLoader loader = new PropertiesConfigurationSourceLoader();
        ConfigurationSource configurationSource = loader.parse(getClass().getResourceAsStream("config_ISO-8859-1.properties"), Charset.forName("ISO-8859-1"));
        assertEquals("rækjusalat", configurationSource.retrieve("key1", String.class));
    }
    
    @Test
    public void testParseDefault() throws Exception {
        PropertiesConfigurationSourceLoader loader = new PropertiesConfigurationSourceLoader();
        ConfigurationSource configurationSource = loader.parse(getClass().getResourceAsStream("config_ISO-8859-1.properties"), null);
        assertEquals("rækjusalat", configurationSource.retrieve("key1", String.class));
    }

}
