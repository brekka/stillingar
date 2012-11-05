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

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

import org.apache.xmlbeans.XmlCursor;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.stillingar.example.support.ApplicationContextBean;
import org.brekka.stillingar.example.support.MessageOfTheDay;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration.ApplicationContext;
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
@Configured
public class ContextReloadTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private ApplicationContextBean appContextBean;
    
    private static Configuration configuration;
    
    static {
        configuration = writeConfig();
    }
    
	@Test
	public void testContext() throws Exception {
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
    private void verify() {
        MessageOfTheDay messageOfTheDay = appContextBean.getContext().getBean(MessageOfTheDay.class);
        assertEquals(configuration.getMOTD(), messageOfTheDay.getMessage());
    }

    private static Configuration writeConfig() {
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration configuration = doc.addNewConfiguration();
        configuration.setMOTD("Some message");
        ApplicationContext subAppContext = configuration.addNewApplicationContext();
        XmlCursor cursor = subAppContext.newCursor();
        cursor.toEndToken();
        cursor.beginElement(new QName("http://www.springframework.org/schema/beans", "beans", "b"));
        cursor.toEndToken();
        cursor.beginElement(new QName("http://brekka.org/schema/stillingar/v1", "annotation-config", "stil"));
        cursor.toFirstAttribute();
        cursor.insertAttributeWithValue("service-ref", "stillingar-example");
        cursor.toEndToken();
        cursor.toNextToken();
        cursor.beginElement(new QName("http://www.springframework.org/schema/beans", "bean", "b"));
        cursor.toFirstAttribute();
        cursor.insertAttributeWithValue("id", "motd");
        cursor.insertAttributeWithValue("class", MessageOfTheDay.class.getName());
        cursor.dispose();
        TestSupport.write(doc);
        return configuration;
    }
}
