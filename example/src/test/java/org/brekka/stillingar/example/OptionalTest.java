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

package org.brekka.stillingar.example;

import static org.junit.Assert.*;

import org.brekka.stillingar.example.support.OptionallyConfiguredBean;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration.Testing;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * @author Andrew Taylor
 */
@ContextConfiguration
@DirtiesContext
public class OptionalTest extends AbstractJUnit4SpringContextTests {
    static {
        writeConfig();
    }
    @Autowired
    private OptionallyConfiguredBean bean;
    
    @Test
    public void test() throws Exception {
        assertEquals("NoChange", bean.getHost());
        assertEquals(-1, bean.getShortValue());
        assertEquals(-1f, bean.getFloatValue(), 0.01);
        assertEquals(42L, bean.getLongValue());
        assertNull(bean.getStringValue());
        assertEquals("MOTD", bean.getMotd());
        assertEquals(0, bean.getIntValue()); // Listener called, changed value to default 0
        writeConfig();
    }
    
    private static void writeConfig() {
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration newConfiguration = doc.addNewConfiguration();
        newConfiguration.setMOTD("MOTD");
        Testing testing = newConfiguration.addNewTesting();
        testing.setLong(42L);
        TestSupport.write(doc);
    }
}
