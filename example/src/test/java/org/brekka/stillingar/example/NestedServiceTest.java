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
import org.brekka.stillingar.example.support.ThirdPartyConfigBean;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.brekka.xml.stillingar.external.ThirdPartyConfigurationDocument.ThirdPartyConfiguration;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Check that nested spring contexts can access beans from the parent and register namespaces.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@ContextConfiguration
@DirtiesContext
public class NestedServiceTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private MessageOfTheDay messageOfTheDay;
    
    static {
        writeConfig("localhost");
    }
    
    @AfterClass
    public static void unSetProperty() {
        System.setProperty("stillingar.reload-watcher.disabled", "false");
    }
    
	@Test
	public void testGrabThirdPartyConfig() throws Exception {
	    ClassPathXmlApplicationContext subContext = new ClassPathXmlApplicationContext(new String[] {
	            "org/brekka/stillingar/example/NestedServiceTest-subcontext.xml"}, true, applicationContext);
	    
	    ThirdPartyConfigBean bean = subContext.getBean(ThirdPartyConfigBean.class);
	    assertEquals("localhost", bean.getHost());
	    
	    writeConfig("localhost.local");
	    Thread.sleep(1000);
	    
	    assertEquals("localhost.local", bean.getHost());
	}
	
    private static void writeConfig(String host) {
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration newConfiguration = doc.addNewConfiguration();
        newConfiguration.setMOTD("Test");
        ThirdPartyConfiguration thirdPartyConfiguration = newConfiguration.addNewThirdPartyConfiguration();
        thirdPartyConfiguration.setHost(host);
        thirdPartyConfiguration.setPort(1234);
        thirdPartyConfiguration.setUsername("username");
        thirdPartyConfiguration.setPassword("password");
        TestSupport.write(doc);
    }
}
