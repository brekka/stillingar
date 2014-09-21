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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.iharder.Base64;

import org.brekka.stillingar.api.ValueConfigurationException;
import org.brekka.stillingar.jackson.config.TestConfig;
import org.brekka.stillingar.jackson.config.TestConfig.CompanyX;
import org.brekka.stillingar.jackson.config.TestConfig.CompanyY;
import org.brekka.stillingar.jackson.config.TestConfig.FeatureFlag;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * Tests for JacksonConfigurationSource
 *
 * @author Andrew Taylor
 */
public class JacksonConfigurationSourceTest {

    private JacksonConfigurationSource configurationSource;
    
    @Before
    public void setup() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        JacksonConfigurationSourceLoader loader = new JacksonConfigurationSourceLoader(mapper, TestConfig.class);
        configurationSource = (JacksonConfigurationSource) loader.parse(getClass().getResourceAsStream("TestConfiguration.json"), null);
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.JacksonConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test
    public void testIsAvailableClassTrue() {
        assertTrue(configurationSource.isAvailable(CompanyY.class));
    }
    
    @Test
    public void testIsAvailableClassFalse() {
        assertFalse(configurationSource.isAvailable(CompanyX.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.JacksonConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsAvailableXPathTrue() {
        assertTrue(configurationSource.isAvailable("$.companyY"));
    }
    
    @Test
    public void testIsAvailableXPathFalse() {
        assertFalse(configurationSource.isAvailable("$.companyX"));
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.JacksonConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test
    public void testRetrieveClass() {
        CompanyY companyY = configurationSource.retrieve(CompanyY.class);
        assertNotNull(companyY);
        assertEquals("http://example.org/CompanyY", companyY.getWarehouseWebService().getUrl());
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.JacksonConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveXPath() {
        CompanyY companyY = configurationSource.retrieve("$..companyY", CompanyY.class);
        assertNotNull(companyY);
        assertEquals("http://example.org/CompanyY", companyY.getWarehouseWebService().getUrl());
    }
    
    @Test
    public void testRetrieveXPathIndex() {
        String keyword = configurationSource.retrieve("$..fraud.keyword[1]", String.class);
        assertEquals("KeywordB", keyword);
    }
    
    @Test
    public void testRetrieveXPathAttrSelector() {
        Boolean flag = configurationSource.retrieve("$..featureFlag[?(@.key == 'TURBO')].enabled", Boolean.class);
        assertEquals(Boolean.TRUE, flag);
    }
    
    @Test
    public void testRetrieveXPathAttrSelectorElem() {
        String message = configurationSource.retrieve("$..motd[?(@.number == 1)].message", String.class);
        assertEquals("Test message", message);
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.JacksonConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test
    public void testRetrieveListClass() {
        List<FeatureFlag> list = configurationSource.retrieveList(FeatureFlag.class);
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    /**
     * Test method for {@link org.brekka.stillingar.xmlbeans.JacksonConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveListXPath() {
        List<String> list = configurationSource.retrieveList("$..fraud.keyword[*]", String.class);
        assertNotNull(list);
        assertEquals(Arrays.asList("KeywordA", "KeywordB", "KeywordC"), list);
    }
    
    @Test
    public void testRetrieveShort() {
        assertEquals(Short.valueOf((short) 169), configurationSource.retrieve("$..scale", Short.class));
    }
    
    @Test
    public void testRetrieveByte() {
        assertEquals(Byte.valueOf((byte) 126), configurationSource.retrieve("$..flag", Byte.class));
    }
    
    @Test
    public void testRetrieveInt() {
        assertEquals(Integer.valueOf(42), configurationSource.retrieve("$..maxQuantity", Integer.class));
    }
    
    @Test
    public void testRetrieveLong() {
        assertEquals(Long.valueOf(85697458963323L), configurationSource.retrieve("$..length", Long.class));
    }
    
    @Test
    public void testRetrieveFloat() {
        assertEquals(Float.valueOf(0.89f), configurationSource.retrieve("$..triggerFactor", Float.class));
    }
    
    @Test
    public void testRetrieveDecimal() {
        assertEquals(new BigDecimal("50000.73"), configurationSource.retrieve("$..maxAmount", BigDecimal.class));
    }
    
    @Test
    public void testRetrieveInteger() {
        assertEquals(new BigInteger("33543"), configurationSource.retrieve("$..factor", BigInteger.class));
    }
    
    @Test
    public void testRetrieveBoolean() throws Exception {
        assertEquals(Boolean.TRUE, configurationSource.retrieve("$..fraud.enabled", Boolean.class));
    }
    
    @Test // P5Y2M10D
    public void testRetrievePeriod() throws Exception {
        assertEquals(new Period(5, 2, 0, 10, 0, 0, 0, 0), configurationSource.retrieve("$..lockDuration", Period.class));
    }
    
    
    @Test
    public void testRetrieveBytes() throws Exception {
        byte[] expected = Base64.decode("U3RpbGxpbmdhcg==");
        assertTrue(Arrays.equals(expected, configurationSource.retrieve("$..publicKey", byte[].class)));
    }
    
    @Test
    public void testRetrieveDateTime() throws Exception {
        DateTime expected = new DateTime(2012, 12, 31, 12, 0, 0, 0);
        assertEquals(expected, configurationSource.retrieve("$..expires", DateTime.class));
    }
    
    @Test
    public void testRetrieveCalendar() throws Exception {
        DateTime expected = new DateTime(2012, 12, 31, 12, 0, 0, 0);
        assertEquals(expected.toCalendar(null), configurationSource.retrieve("$..expires", Calendar.class));
    }
    
    @Test
    public void testRetrieveDate() throws Exception {
        DateTime expected = new DateTime(2012, 12, 31, 12, 0, 0, 0);
        assertEquals(expected.toDate(), configurationSource.retrieve("$..expires", Date.class));
    }
    
    @Test
    public void testRetrieveURI() throws Exception {
        assertEquals(new URI("http://example.org/CompanyY"), configurationSource.retrieve("$..companyY..url", URI.class));
    }
    
    @Test
    public void testRetrieveUUID() throws Exception {
        assertEquals(UUID.fromString("64829ee9-d265-47bb-8fb4-4ab4ada0cdfc"), configurationSource.retrieve("$.motd.id", UUID.class));
    }
    
    @Test
    public void testRetrieveEnum() {
        assertEquals(TimeUnit.DAYS, configurationSource.retrieve("$..security.timeUnit", TimeUnit.class));
    }
    @Test(expected=ValueConfigurationException.class)
    public void testRetrieveIncorrectType() throws Exception {
        configurationSource.retrieve("$..maxQuantity", URL.class);
    }
    
}
