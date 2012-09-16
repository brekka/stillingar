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

package org.brekka.stillingar.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * FallbackConfigurationSource Test
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class FallbackConfigurationSourceTest {

    private FallbackConfigurationSource source;
    
    @Mock
    private ConfigurationSource primary;
    
    @Mock
    private ConfigurationSource secondary;
    
    @Before
    public void setup() {
        source = new FallbackConfigurationSource(primary, secondary);
    }
    
    @Test
    public void testPrimaryOnly() {
        source = new FallbackConfigurationSource(primary, null);
        assertSame(primary, source.getPrimarySource());
        assertSame(FallbackConfigurationSource.NONE, source.getSecondarySource());
    }
    
    @Test
    public void testSecondaryOnly() {
        source = new FallbackConfigurationSource(null, secondary);
        assertSame(FallbackConfigurationSource.NONE, source.getPrimarySource());
        assertSame(secondary, source.getSecondarySource());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNoSources() {
        new FallbackConfigurationSource(null, null).getClass();
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#getPrimarySource()}.
     */
    @Test
    public void testGetPrimarySource() {
        assertSame(primary, source.getPrimarySource());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#getSecondarySource()}.
     */
    @Test
    public void testGetSecondarySource() {
        assertSame(secondary, source.getSecondarySource());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIsAvailableClassNoType() {
        source.isAvailable((Class<?>) null);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test
    public void testIsAvailableClassUsePrimary() {
        Class<URI> type = URI.class;
        when(primary.isAvailable(type)).thenReturn(Boolean.TRUE);
        assertTrue(source.isAvailable(type));
        verify(primary).isAvailable(type);
        verifyZeroInteractions(secondary);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test
    public void testIsAvailableClassUseSecondary() {
        Class<URI> type = URI.class;
        when(primary.isAvailable(type)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(type)).thenReturn(Boolean.TRUE);
        assertTrue(source.isAvailable(type));
        verify(primary).isAvailable(type);
        verify(secondary).isAvailable(type);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.Class)}.
     */
    @Test
    public void testIsAvailableClassNone() {
        Class<URI> type = URI.class;
        when(primary.isAvailable(type)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(type)).thenReturn(Boolean.FALSE);
        assertFalse(source.isAvailable(type));
        verify(primary).isAvailable(type);
        verify(secondary).isAvailable(type);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIsAvailableStringNoType() {
        source.isAvailable((String) null);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsAvailableStringUsePrimary() {
        String expression = "/c:Test";
        when(primary.isAvailable(expression)).thenReturn(Boolean.TRUE);
        assertTrue(source.isAvailable(expression));
        verify(primary).isAvailable(expression);
        verifyZeroInteractions(secondary);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsAvailableStringUseSecondary() {
        String expression = "/c:Test";
        when(primary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(expression)).thenReturn(Boolean.TRUE);
        assertTrue(source.isAvailable(expression));
        verify(primary).isAvailable(expression);
        verify(secondary).isAvailable(expression);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#isAvailable(java.lang.String)}.
     */
    @Test
    public void testIsAvailableStringNone() {
        String expression = "/c:Test";
        when(primary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        assertFalse(source.isAvailable(expression));
        verify(primary).isAvailable(expression);
        verify(secondary).isAvailable(expression);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRetrieveClassNoType() throws Exception {
        source.retrieve(null);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test
    public void testRetrieveClassFromPrimary() throws Exception {
        Class<URI> type = URI.class;
        URI value = new URI("http://example.org");
        when(primary.isAvailable(type)).thenReturn(Boolean.TRUE);
        when(primary.retrieve(type)).thenReturn(value);
        assertSame(value, source.retrieve(type));
        verify(primary).isAvailable(type);
        verify(primary).retrieve(type);
        verifyZeroInteractions(secondary);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test
    public void testRetrieveClassFromSecondary() throws Exception {
        Class<URI> type = URI.class;
        URI value = new URI("http://example.org");
        when(primary.isAvailable(type)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(type)).thenReturn(Boolean.TRUE);
        when(secondary.retrieve(type)).thenReturn(value);
        assertSame(value, source.retrieve(type));
        verify(primary).isAvailable(type);
        verify(secondary).isAvailable(type);
        verify(secondary).retrieve(type);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveClassNone() throws Exception {
        Class<URI> type = URI.class;
        URI value = new URI("http://example.org");
        when(primary.isAvailable(type)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(type)).thenReturn(Boolean.FALSE);
        assertSame(value, source.retrieve(type));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRetrieveNoExpression() throws Exception {
        source.retrieve(null, URI.class);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRetrieveNoType() throws Exception {
        source.retrieve("/c:Test", null);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveStringPrimary() throws Exception {
        Class<URI> type = URI.class;
        String expression = "/c:Test";
        URI value = new URI("http://example.org");
        when(primary.isAvailable(expression)).thenReturn(Boolean.TRUE);
        when(primary.retrieve(expression, type)).thenReturn(value);
        assertSame(value, source.retrieve(expression, type));
        verify(primary).isAvailable(expression);
        verify(primary).retrieve(expression, type);
        verifyZeroInteractions(secondary);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveStringSecondary() throws Exception {
        Class<URI> type = URI.class;
        String expression = "/c:Test";
        URI value = new URI("http://example.org");
        when(primary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(expression)).thenReturn(Boolean.TRUE);
        when(secondary.retrieve(expression, type)).thenReturn(value);
        assertSame(value, source.retrieve(expression, type));
        verify(primary).isAvailable(expression);
        verify(secondary).isAvailable(expression);
        verify(secondary).retrieve(expression, type);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveStringNone() throws Exception {
        Class<URI> type = URI.class;
        String expression = "/c:Test";
        URI value = new URI("http://example.org");
        when(primary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        assertSame(value, source.retrieve(expression, type));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRetrieveListClassNoType() throws Exception {
        source.retrieveList(null);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test
    public void testRetrieveListClassFromPrimary() throws Exception {
        Class<URI> type = URI.class;
        List<URI> value = Arrays.asList(new URI("http://example.org"));
        when(primary.isAvailable(type)).thenReturn(Boolean.TRUE);
        when(primary.retrieveList(type)).thenReturn(value);
        assertSame(value, source.retrieveList(type));
        verify(primary).isAvailable(type);
        verify(primary).retrieveList(type);
        verifyZeroInteractions(secondary);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test
    public void testRetrieveListClassFromSecondary() throws Exception {
        Class<URI> type = URI.class;
        List<URI> value = Arrays.asList(new URI("http://example.org"));
        when(primary.isAvailable(type)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(type)).thenReturn(Boolean.TRUE);
        when(secondary.retrieveList(type)).thenReturn(value);
        assertSame(value, source.retrieveList(type));
        verify(primary).isAvailable(type);
        verify(secondary).isAvailable(type);
        verify(secondary).retrieveList(type);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveListClassNone() throws Exception {
        Class<URI> type = URI.class;
        URI value = new URI("http://example.org");
        when(primary.isAvailable(type)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(type)).thenReturn(Boolean.FALSE);
        assertSame(value, source.retrieveList(type));
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRetrieveListNoExpression() throws Exception {
        source.retrieveList(null, URI.class);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRetrieveListNoType() throws Exception {
        source.retrieveList("/c:Test", null);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveListStringPrimary() throws Exception {
        Class<URI> type = URI.class;
        String expression = "/c:Test";
        List<URI> value = Arrays.asList(new URI("http://example.org"));
        when(primary.isAvailable(expression)).thenReturn(Boolean.TRUE);
        when(primary.retrieveList(expression, type)).thenReturn(value);
        assertSame(value, source.retrieveList(expression, type));
        verify(primary).isAvailable(expression);
        verify(primary).retrieveList(expression, type);
        verifyZeroInteractions(secondary);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testRetrieveListStringSecondary() throws Exception {
        Class<URI> type = URI.class;
        String expression = "/c:Test";
        List<URI> value = Arrays.asList(new URI("http://example.org"));
        when(primary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(expression)).thenReturn(Boolean.TRUE);
        when(secondary.retrieveList(expression, type)).thenReturn(value);
        assertSame(value, source.retrieveList(expression, type));
        verify(primary).isAvailable(expression);
        verify(secondary).isAvailable(expression);
        verify(secondary).retrieveList(expression, type);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.FallbackConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
     */
    @Test(expected=ConfigurationException.class)
    public void testRetrieveListStringNone() throws Exception {
        Class<URI> type = URI.class;
        String expression = "/c:Test";
        URI value = new URI("http://example.org");
        when(primary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        when(secondary.isAvailable(expression)).thenReturn(Boolean.FALSE);
        assertSame(value, source.retrieveList(expression, type));
    }

}
