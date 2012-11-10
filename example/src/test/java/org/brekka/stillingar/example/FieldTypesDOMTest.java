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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.brekka.stillingar.example.support.ConfiguredFieldTypes;
import org.brekka.stillingar.example.support.TestSupport;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.FeatureFlagType;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration.Testing;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Test field based configuration (JAXB).
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@ContextConfiguration
@DirtiesContext
public class FieldTypesDOMTest extends AbstractJUnit4SpringContextTests {

    
    @Autowired
    private ConfiguredFieldTypes configuredFieldTypes;
    
    private static Testing testing;
    
    static {
        testing = writeConfig();
        
    }
    
	@Test
	public void testFields() throws Exception {
	    verify();
	    Thread.sleep(2000);
	    for (int i = 0; i < 3; i++) {
	        testing = writeConfig();
	        Thread.sleep(2000);
	        verify();
        }
	}
	
	
	
    /**
     * 
     */
    private void verify() throws Exception {
        ConfiguredFieldTypes t = configuredFieldTypes;
        assertEquals(new URI(testing.getAnyURI()), t.getUri());
        assertEquals(testing.getBoolean(), t.isBooleanPrimitive());
        assertEquals(Byte.valueOf(testing.getByte()), t.getByteValue());
        assertEquals(testing.getByte(), t.getBytePrimitive());
        assertEquals(testing.getDate().getTime(), t.getDateAsCalendar().getTime());
        assertEquals(testing.getDate().getTime(), t.getDateAsDate());
        assertEquals(testing.getDateTime().getTime(), t.getDateTimeAsCalendar().getTime());
        assertEquals(testing.getDateTime().getTime(), t.getDateTimeAsDate());
        assertEquals(testing.getDecimal(), t.getDecimal());
        assertEquals(Double.valueOf(testing.getDouble()), t.getDoubleValue());
        assertEquals(testing.getDouble(), t.getDoublePrimitive(), 1d);
        assertEquals(testing.getFloat(), t.getFloatPrimitive(), 1f);
        assertEquals(Float.valueOf(testing.getFloat()), t.getFloatValue());
        assertEquals(testing.getInt(), t.getIntPrimitive());
        assertEquals(Integer.valueOf(testing.getInt()), t.getIntValue());
        assertEquals(testing.getLanguage(), t.getLanguage().toString());
        assertEquals(testing.getLong(), t.getLongPrimitive());
        assertEquals(Long.valueOf(testing.getLong()), t.getLongValue());
        assertEquals(testing.getShort(), t.getShortPrimitive());
        assertEquals(Short.valueOf(testing.getShort()), t.getShortValue());
        assertEquals(testing.getString(), t.getString());
        assertEquals(String.format("%tT", testing.getTime()), String.format("%tT", t.getTimeAsCalendar().getTime()));
        assertTrue(Arrays.equals(t.getBinary(), testing.getBinary()));
        assertEquals(UUID.fromString(testing.getUUID()), t.getUuid());
        assertNotNull(t.getTestingElement());
        assertNotNull(t.getRoot());
    }



    private static Testing writeConfig() {
        Random r = new Random();
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration newConfiguration = doc.addNewConfiguration();
        FeatureFlagType featureFlag = newConfiguration.addNewFeatureFlag();
        featureFlag.setKey("turbo");
        featureFlag.setBooleanValue(true);
        Testing testing = newConfiguration.addNewTesting();
        testing.setAnyURI("http://brekka.org/" + RandomStringUtils.randomAlphanumeric(10));
        testing.setBoolean(r.nextBoolean());
        testing.setByte((byte) r.nextInt());
        Calendar cal = Calendar.getInstance();
        testing.setDate(cal);
        testing.setDateTime(cal);
        testing.setDecimal(BigDecimal.valueOf(r.nextDouble()));
        testing.setDouble(r.nextDouble());
        testing.setFloat(r.nextFloat());
        testing.setInt(r.nextInt());
        testing.setInteger(BigInteger.valueOf(r.nextLong()));
        testing.setLanguage("en");
        testing.setLong(r.nextLong());
        testing.setShort((short) r.nextInt());
        testing.setString(RandomStringUtils.randomAlphanumeric(24));
        testing.setTime(cal);
        testing.setUUID(UUID.randomUUID().toString());
        byte[] binary = new byte[32];
        r.nextBytes(binary);
        testing.setBinary(binary);
        TestSupport.write(doc);
        return testing;
    }
}
