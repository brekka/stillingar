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

package org.brekka.stillingar.example;

import static org.junit.Assert.assertEquals;

import org.brekka.stillingar.example.support.MessageOfTheDay;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Verify that the polling mechanism for reloading works.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@ContextConfiguration
@DirtiesContext
public class PollingReloadTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private MessageOfTheDay messageOfTheDay;
    
    @BeforeClass
    public static void setOverrideProperty() {
        // Simulate JDK < 7
        System.setProperty("stillingar.reload-watcher.disabled", "true");
        writeConfig("Reload check");
    }
    
    @AfterClass
    public static void unSetProperty() {
        System.setProperty("stillingar.reload-watcher.disabled", "false");
    }
    
	@Test
	public void testPolling() throws Exception {
	    assertEquals("Reload check", messageOfTheDay.getMessage());
	    Thread.sleep(2000);
	    for (int i = 0; i < 3; i++) {
	        String msg = "Message has been updated " + i;
	        writeConfig(msg);
	        Thread.sleep(3000);
	        assertEquals(msg, messageOfTheDay.getMessage());
        }
	}
	
    private static void writeConfig(String message) {
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration newConfiguration = doc.addNewConfiguration();
        newConfiguration.setMOTD(message);
        TestSupport.write(doc);
    }
}
