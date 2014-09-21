/*
 * Copyright 2014 the original author or authors.
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

package org.brekka.stillingar.jackson;

import static org.junit.Assert.assertNotNull;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.jackson.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for JacksonConfigurationSourceLoader
 *
 * @author Andrew Taylor
 */
public class JacksonConfigurationSourceLoaderTest {
    
    private ObjectMapper om;
    
    @Before
    public void setup() {
        om = new ObjectMapper();
    }

    @Test(expected=NullPointerException.class)
    public void testInitNone() {
        new JacksonConfigurationSourceLoader(null, null).toString();
    }
    
    @Test
    public void testInitNoRootClass() {
        new JacksonConfigurationSourceLoader(om, null).toString();
    }
    
    @Test
    public void testInitWithRootClass() {
        new JacksonConfigurationSourceLoader(om, TestConfig.class).toString();
    }
    
    @Test
    public void testInitRootClassConvMan() {
        ConversionManager manager = new ConversionManager(JacksonConfigurationSourceLoader.prepareConverters());
        new JacksonConfigurationSourceLoader(om, TestConfig.class, manager).toString();
    }
    
    @Test
    public void testInitNoRootClassConvMan() {
        ConversionManager manager = new ConversionManager(JacksonConfigurationSourceLoader.prepareConverters());
        new JacksonConfigurationSourceLoader(om, null, manager).toString();
    }

    @Test
    public void testParseValid() throws Exception {
        JacksonConfigurationSourceLoader snapshotLoader =  new JacksonConfigurationSourceLoader(om, TestConfig.class);
        ConfigurationSource configurationSource = snapshotLoader.parse(
                getClass().getResourceAsStream("TestConfiguration.json"), null);
        assertNotNull(configurationSource);
    }
    
    @Test(expected=ConfigurationException.class)
    public void testParseNonJson() throws Exception {
        JacksonConfigurationSourceLoader snapshotLoader =  new JacksonConfigurationSourceLoader(om, TestConfig.class);
        snapshotLoader.parse(getClass().getResourceAsStream("NonJsonTestConfiguration.txt"), null);
    }
    
    @Test(expected=NullPointerException.class)
    public void testParseNullStream() throws Exception {
        JacksonConfigurationSourceLoader snapshotLoader =  new JacksonConfigurationSourceLoader(om, TestConfig.class);
        snapshotLoader.parse(null, null);
    }

}
