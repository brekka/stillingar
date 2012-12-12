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

package org.brekka.stillingar.spring.bpp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationService;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.core.ValueListDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * ConfigurationBeanPostProcessor Test
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationBeanPostProcessorTest {
    
    
    @Mock
    private ConfigurationService configurationService;
    
    @Mock
    private BeanFactory beanFactory;

    private ConfigurationBeanPostProcessor beanPostProcessor;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        beanPostProcessor = new ConfigurationBeanPostProcessor("test", configurationService);
        beanPostProcessor.setBeanFactory(beanFactory);
    }

    @Test
    public void testSingleton() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<URI> uriList = Arrays.asList(new URI("http://brekka.org/"));
        List<Date> dateList = Arrays.asList(new Date());
        List<Locale> localeList = Arrays.asList(Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        when(beanFactory.isSingleton(eq("bob"))).thenReturn(true);
        when(beanFactory.getBean(eq(Calendar.class))).thenReturn(calendar);
        when(beanFactory.getBean(eq("value9"), eq(String.class))).thenReturn("v9");
        
        when(configurationService.retrieve(eq("/c:value1"), eq(String.class))).thenReturn("v1");
        when(configurationService.retrieve(eq("/c:value2"), eq(Integer.class))).thenReturn(12);
        when(configurationService.retrieve(eq("/c:value3"), eq(Long.class))).thenReturn(123456L);
        when(configurationService.retrieve(eq(UUID.class))).thenReturn(uuid);
        when(configurationService.retrieveList(eq("/c:value5"), eq(Date.class))).thenReturn(dateList);
        when(configurationService.retrieveList(eq(URI.class))).thenReturn(uriList);
        when(configurationService.retrieveList(eq("/c:value7"), eq(Locale.class))).thenReturn(localeList);
        
        ConfiguredTestBean bean = new ConfiguredTestBean();
        Object retVal = beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
        
        ArgumentCaptor<ValueDefinitionGroup> vdg = ArgumentCaptor.forClass(ValueDefinitionGroup.class);
      
        verify(configurationService).register(vdg.capture(), eq(true));
        
        ValueDefinitionGroup valueDefinitionGroup = vdg.getValue();
        assertEquals("bob", valueDefinitionGroup.getName());
        assertNotNull(valueDefinitionGroup.getChangeListener());
        
        List<ValueDefinition<?,?>> values = new ArrayList<ValueDefinition<?,?>>(valueDefinitionGroup.getValues());
        Collections.sort(values, new Comparator<ValueDefinition<?,?>>() {
            @Override
            public int compare(ValueDefinition<?, ?> o1, ValueDefinition<?, ?> o2) {
                if (o1.getExpression() != null && o2.getExpression() != null) {
                    return o1.getExpression().compareTo(o2.getExpression());
                } else if (o1.getExpression() == null) {
                    return 1;
                } else if (o2.getExpression() == null) {
                    return -1;
                }
                return o1.getType() == UUID.class ? -1 : 1;
            }
        });
        
        verifyValue("/c:value1", values.get(0), "v1", String.class, false);
        verifyValue("/c:value2", values.get(1), 12, Integer.class, false);
        verifyValue("/c:value3", values.get(2), 123456L, Long.class, false);
        verifyValue("/c:value5", values.get(3), dateList, Date.class, true);
        verifyValue("/c:value7", values.get(4), localeList, Locale.class, true);
        verifyValue(null, values.get(5), uuid, UUID.class, false);
        verifyValue(null, values.get(6), uriList, URI.class, true);
        
        valueDefinitionGroup.getChangeListener().onChange(configurationService);
        
        assertEquals("v1", bean.getValue1());
        assertEquals(Integer.valueOf(12), bean.getValue2());
        assertEquals(Long.valueOf(123456L), bean.getValue3());
        assertEquals(uuid, bean.getValue4());
        assertEquals(dateList, bean.getValue5());
        assertEquals(uriList, bean.getValue6());
        assertEquals(localeList, bean.getValue7());
        assertEquals(calendar, bean.getValue8());
        assertEquals("v9", bean.getValue9());
        
        assertSame(bean, retVal);
        
        beanPostProcessor.destroy();
        
        verify(configurationService).unregister(eq(valueDefinitionGroup));
    }
    

    @Test
    public void testDynamic() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<URI> uriList = Arrays.asList(new URI("http://brekka.org/"));
        List<Date> dateList = Arrays.asList(new Date());
        List<Locale> localeList = Arrays.asList(Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        when(beanFactory.isSingleton(eq("bob"))).thenReturn(false);
        when(beanFactory.getBean(eq(Calendar.class))).thenReturn(calendar);
        when(beanFactory.getBean(eq("value9"), eq(String.class))).thenReturn("v9");
        
        when(configurationService.retrieve(eq("/c:value1"), eq(String.class))).thenReturn("v1");
        when(configurationService.retrieve(eq("/c:value2"), eq(Integer.class))).thenReturn(12);
        when(configurationService.retrieve(eq("/c:value3"), eq(Long.class))).thenReturn(123456L);
        when(configurationService.retrieve(eq(UUID.class))).thenReturn(uuid);
        when(configurationService.retrieveList(eq("/c:value5"), eq(Date.class))).thenReturn(dateList);
        when(configurationService.retrieveList(eq(URI.class))).thenReturn(uriList);
        when(configurationService.retrieveList(eq("/c:value7"), eq(Locale.class))).thenReturn(localeList);
        
        ConfiguredTestBean bean = new ConfiguredTestBean();
        Object retVal = beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
        
        assertEquals("v1", bean.getValue1());
        assertEquals(Integer.valueOf(12), bean.getValue2());
        assertEquals(Long.valueOf(123456L), bean.getValue3());
        assertEquals(uuid, bean.getValue4());
        assertEquals(dateList, bean.getValue5());
        assertEquals(uriList, bean.getValue6());
        assertEquals(localeList, bean.getValue7());
        assertEquals(calendar, bean.getValue8());
        assertEquals("v9", bean.getValue9());
        
        assertSame(bean, retVal);
        
        beanPostProcessor.destroy();
    }
    
    @Test
    public void testInvalidListenerBeanNotFound() throws Exception {
        ConfiguredTestBean bean = new ConfiguredTestBean();
        
        when(beanFactory.getBean(eq(Calendar.class))).thenThrow(new NoSuchBeanDefinitionException("No bean"));
        
        try {
            beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
            fail();
        } catch (ConfigurationException e) {
            assertEquals("Listener method 'init' parameter 3 is not marked as Configured and no bean definition " +
            		"could be found in the container with the type 'java.util.Calendar'.", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidListenerQualifiedBeanNotFound() throws Exception {
        ConfiguredTestBean bean = new ConfiguredTestBean();
        
        when(beanFactory.getBean(eq("value9"), eq(String.class))).thenThrow(new NoSuchBeanDefinitionException("No bean"));
        
        try {
            beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
            fail();
        } catch (ConfigurationException e) {
            assertEquals("Listener method 'init' parameter 4 is not marked as Configured and no bean definition could be found in " +
            		"the container with the qualifier 'value9' and type 'java.lang.String'.", e.getMessage());
        }
    }
    
    @Test
    public void testDifferentMarkerIgnoreBean() {
        beanPostProcessor.setMarkerAnnotation(org.springframework.stereotype.Component.class);
        // Should be ignored
        ConfiguredTestBean bean = new ConfiguredTestBean();
        when(beanFactory.isSingleton(eq("bob"))).thenThrow(new IllegalStateException("Fail"));
        
        beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
        // Will just go unprocessed
    }
    
    @Test
    public void testInvalidSetterNoParamsBean() {
        // Should be ignored
        InvalidSetterBean bean = new InvalidSetterBean();
        
        try {
            beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
            fail();
        } catch (ConfigurationException e) {
            assertEquals("The method 'public void org.brekka.stillingar.spring.bpp.InvalidSetterBean.setNoParam()' does not appear to be a setter. " +
            		"A bean setter method should take only a single parameter.", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidSetterMultiParamsBean() {
        // Should be ignored
        MultiParamSetterBean bean = new MultiParamSetterBean();
        
        try {
            beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
            fail();
        } catch (ConfigurationException e) {
            assertEquals("The method 'public void org.brekka.stillingar.spring.bpp.MultiParamSetterBean.setNoParam(java.lang.Object,java.lang.Object)' " +
            		"does not appear to be a setter. A bean setter method should take only a single parameter.", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidMultiListenersInBean() {
        // Should be ignored
        MultiListenerBean bean = new MultiListenerBean();
        
        try {
            beanPostProcessor.postProcessBeforeInitialization(bean, "bob");
            fail();
        } catch (ConfigurationException e) {
            assertEquals("Unable to create a configuration listener for the method 'init2' on the bean 'bob' " +
            		"(type 'org.brekka.stillingar.spring.bpp.MultiListenerBean') as it already contains a " +
            		"configuration listener on the method 'init1'", e.getMessage());
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void verifyValue(String expression, ValueDefinition vd, Object value, Class<?> type, boolean list) {
        assertEquals(expression, vd.getExpression());
        assertEquals(type, vd.getType());
        assertNotNull(vd.getChangeListener());
        assertEquals(list, vd instanceof ValueListDefinition);
        ValueChangeListener changeListener = vd.getChangeListener();
        changeListener.onChange(value, null);
    }
    
}
