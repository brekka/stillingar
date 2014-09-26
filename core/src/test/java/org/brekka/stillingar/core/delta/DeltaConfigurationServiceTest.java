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

package org.brekka.stillingar.core.delta;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.SingleValueDefinition;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.core.support.ConfigBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * DeltaConfigurationSource Test
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeltaConfigurationServiceTest {
    
    private static final ConfigBean THE_VALUE = new ConfigBean();
    private static final ConfigBean CHANGED_VALUE = new ConfigBean();
    
    private DeltaConfigurationService configurationSource;
    
    @Mock
    private ConfigurationSource defaultConfigurationSource;
    
    @Mock
    private DeltaValueInterceptor deltaValueInterceptor;

    @Before
    public void setup() throws Exception {
        configurationSource = new DeltaConfigurationService(defaultConfigurationSource);
        configurationSource.setDeltaValueInterceptor(deltaValueInterceptor);
        configurationSource.setDeltaOperations(new DeltaOperations());
        configurationSource.refresh(null);
        when(defaultConfigurationSource.isAvailable(eq(ConfigBean.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(ConfigBean.class))).thenReturn(THE_VALUE);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#register(org.brekka.stillingar.core.ValueDefinition, boolean)}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRegisterValueDefinitionNoFire() throws Exception {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, false);
        
        verify(defaultConfigurationSource, times(2)).isAvailable(eq(ConfigBean.class));
        verify(defaultConfigurationSource).retrieve(eq(ConfigBean.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, valueChangeListener);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(ConfigBean.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(ConfigBean.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#register(org.brekka.stillingar.core.ValueDefinition, boolean)}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRegisterValueDefinitionWithFire() throws Exception {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, true);
        
        verify(defaultConfigurationSource, times(2)).isAvailable(eq(ConfigBean.class));
        verify(defaultConfigurationSource).retrieve(eq(ConfigBean.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        verify(valueChangeListener).onChange(eq(THE_VALUE), isNull(ConfigBean.class));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, valueChangeListener);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(ConfigBean.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(ConfigBean.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#register(org.brekka.stillingar.core.ValueDefinitionGroup, boolean)}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRegisterValueDefinitionGroupNoFire() throws Exception {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinitionGroup, false);
        
        verify(defaultConfigurationSource, times(2)).isAvailable(eq(ConfigBean.class));
        verify(defaultConfigurationSource).retrieve(eq(ConfigBean.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, groupChangeListener, valueChangeListener);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(ConfigBean.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(ConfigBean.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
        verify(groupChangeListener).onChange(isA(ConfigurationSource.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#register(org.brekka.stillingar.core.ValueDefinitionGroup, boolean)}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRegisterValueDefinitionGroupWithFire() throws Exception {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinitionGroup, true);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(ConfigBean.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(ConfigBean.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(defaultConfigurationSource, times(4)).isAvailable(eq(ConfigBean.class));
        verify(defaultConfigurationSource, times(2)).retrieve(eq(ConfigBean.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        verify(valueChangeListener).onChange(eq(THE_VALUE), isNull(ConfigBean.class));
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
        verify(groupChangeListener, times(2)).onChange(isA(ConfigurationSource.class));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, groupChangeListener, valueChangeListener);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#unregister(org.brekka.stillingar.core.ValueDefinition)}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testUnregisterValueDefinition() {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, false);
        
        configurationSource.unregister(valueDefinition);
        
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#unregister(org.brekka.stillingar.core.ValueDefinitionGroup)}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testUnregisterValueDefinitionGroup() {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinitionGroup, false);
        
        verify(defaultConfigurationSource, times(2)).isAvailable(eq(ConfigBean.class));
        verify(defaultConfigurationSource).retrieve(eq(ConfigBean.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        configurationSource.unregister(valueDefinitionGroup);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationService#shutdown()}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testShutdown() {
        ValueChangeListener<ConfigBean> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<ConfigBean> valueDefinition = new SingleValueDefinition<ConfigBean>(ConfigBean.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, false);
        
        verify(defaultConfigurationSource, times(2)).isAvailable(eq(ConfigBean.class));
        verify(defaultConfigurationSource).retrieve(eq(ConfigBean.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        configurationSource.shutdown();
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetDeltaOperationsNull() {
        configurationSource.setDeltaOperations(null);
    }

}
