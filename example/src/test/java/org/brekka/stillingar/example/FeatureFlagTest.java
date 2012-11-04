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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration.Testing;
import org.brekka.xml.stillingar.example.v1.FeatureFlagType;
import org.junit.Test;
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
public class FeatureFlagTest extends AbstractJUnit4SpringContextTests {

    @Configured("//c:FeatureFlag[@key='turbo']")
    private boolean turboEnabled;
    
    @Configured("//c:FeatureFlag[@key='stripe']")
    private boolean stripeEnabled;
    
    @Configured("//c:FeatureFlag[@key='wax']")
    private boolean waxEnabled;
    
    
    private static Testing testing;
    
    static {
        testing = writeConfig();
    }
    
	@Test
	public void testFields() throws Exception {
	    assertTrue(turboEnabled);
	    assertFalse(stripeEnabled);
	    assertTrue(waxEnabled);
	}



    private static Testing writeConfig() {
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration configuration = doc.addNewConfiguration();
        setFlag("turbo", true, configuration);
        setFlag("stripe", false, configuration);
        setFlag("wax", true, configuration);
        TestSupport.write(doc);
        return testing;
    }
    
    private static void setFlag(String key, boolean value, Configuration configuration) {
        FeatureFlagType turbo = configuration.addNewFeatureFlag();
        turbo.setBooleanValue(value);
        turbo.setKey(key);
    }
}
