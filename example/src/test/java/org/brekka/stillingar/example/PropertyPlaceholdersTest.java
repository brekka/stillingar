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

import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration.Testing;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Test field based configuration.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@ContextConfiguration
@DirtiesContext
@Configured
public class PropertyPlaceholdersTest extends AbstractJUnit4SpringContextTests {

    
    @Autowired
    @Qualifier("motd")
    private String unchangableMotd;
    
    @Autowired
    @Qualifier("testingStr")
    private String unchangableTestingStr;
    
    @Autowired
    private StandardBean bean;
    
    private static Configuration configuration;
    
    static {
        configuration = writeConfig();
    }
    
	@Test
	public void testProperties() throws Exception {
	    assertEquals(configuration.getMOTD(), unchangableMotd);
	    String expected = configuration.getMOTD() + "-" + configuration.getTesting().getInt();
	    assertEquals(expected, unchangableTestingStr);
	    assertEquals(configuration.getMOTD(), bean.getValue());
	    verify();
	    Thread.sleep(2000);
	    for (int i = 0; i < 3; i++) {
	        configuration = writeConfig();
	        Thread.sleep(2000);
	        verify();
        }
	}
	
	
	
    /**
     * 
     */
    private void verify() throws Exception {
        String motd = applicationContext.getBean("motd", String.class);
        assertEquals(configuration.getMOTD(), motd);
        
        String testingStr = applicationContext.getBean("testingStr", String.class);
        String expected = configuration.getMOTD() + "-" + configuration.getTesting().getInt();
        assertEquals(expected, testingStr);
        assertEquals(configuration.getMOTD(), bean.getValue());
    }



    private static Configuration writeConfig() {
        Random r = new Random();
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        configuration = doc.addNewConfiguration();
        Testing testing = configuration.addNewTesting();
        configuration.setMOTD("Value_" + RandomStringUtils.randomAlphabetic(6));
        testing.setInt(r.nextInt());
        testing.setString("${//c:MOTD}-${//c:Testing/c:Int}");
        
        TestSupport.write(doc);
        return configuration;
    }
}
