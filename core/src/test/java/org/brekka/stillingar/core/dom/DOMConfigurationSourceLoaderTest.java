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

package org.brekka.stillingar.core.dom;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.namespace.NamespaceContext;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.properties.PropertiesConfigurationSourceLoader;
import org.junit.Test;

/**
 * DOMConfigurationSourceLoaderTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DOMConfigurationSourceLoaderTest {

    /**
     * Test method for {@link org.brekka.stillingar.core.dom.DOMConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)}.
     */
    @Test
    public void testParse() throws IOException {
        DOMConfigurationSourceLoader loader = new DOMConfigurationSourceLoader();
        ConfigurationSource configurationSource = loader.parse(getClass().getResourceAsStream("config.xml"), null);
        assertEquals("50000.73", configurationSource.retrieve("//MaxAmount", String.class));
    }

}
