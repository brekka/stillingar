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

import org.apache.commons.lang.SystemUtils;
import org.brekka.stillingar.example.support.MessageOfTheDay;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Verify that the JDK7 based monitor works. Request JDK 7 (naturally).
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@ContextConfiguration
@DirtiesContext
public class WatcherReloadTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private MessageOfTheDay messageOfTheDay;
    
    static {
        writeConfig("Reload check");
    }
    
	@Test
	public void testWatch() throws Exception {
	    org.junit.Assume.assumeTrue(SystemUtils.IS_JAVA_1_7);
	    assertEquals("Reload check", messageOfTheDay.getMessage());
	    
	    for (int i = 0; i < 3; i++) {
	        String msg = "Message has been updated " + i;
	        writeConfig(msg);
	        Thread.sleep(2000);
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
