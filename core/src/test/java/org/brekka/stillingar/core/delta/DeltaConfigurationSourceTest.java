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

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.SingleValueDefinition;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
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
public class DeltaConfigurationSourceTest {
    
    private static final String THE_VALUE = "Test";
    private static final String CHANGED_VALUE = "Test2";
    
    private DeltaConfigurationSource configurationSource;
    
    @Mock
    private ConfigurationSource defaultConfigurationSource;
    
    @Mock
    private DeltaValueInterceptor deltaValueInterceptor;

    @Before
    public void setup() throws Exception {
        configurationSource = new DeltaConfigurationSource(defaultConfigurationSource);
        configurationSource.setDeltaValueInterceptor(deltaValueInterceptor);
        configurationSource.setDeltaOperations(new DeltaOperations());
        configurationSource.refresh(null);
        when(defaultConfigurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(String.class))).thenReturn(THE_VALUE);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#register(org.brekka.stillingar.core.ValueDefinition, boolean)}.
     */
    @Test
    public void testRegisterValueDefinitionNoFire() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, false);
        
        verify(defaultConfigurationSource).isAvailable(eq(String.class));
        verify(defaultConfigurationSource).retrieve(eq(String.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, valueChangeListener);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(String.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#register(org.brekka.stillingar.core.ValueDefinition, boolean)}.
     */
    @Test
    public void testRegisterValueDefinitionWithFire() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, true);
        
        verify(defaultConfigurationSource).isAvailable(eq(String.class));
        verify(defaultConfigurationSource).retrieve(eq(String.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        verify(valueChangeListener).onChange(eq(THE_VALUE), isNull(String.class));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, valueChangeListener);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(String.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#register(org.brekka.stillingar.core.ValueDefinitionGroup, boolean)}.
     */
    @Test
    public void testRegisterValueDefinitionGroupNoFire() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinitionGroup, false);
        
        verify(defaultConfigurationSource).isAvailable(eq(String.class));
        verify(defaultConfigurationSource).retrieve(eq(String.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, groupChangeListener, valueChangeListener);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(String.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
        verify(groupChangeListener).onChange(isA(ConfigurationSource.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#register(org.brekka.stillingar.core.ValueDefinitionGroup, boolean)}.
     */
    @Test
    public void testRegisterValueDefinitionGroupWithFire() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinitionGroup, true);
        
        // Trigger a refresh
        when(defaultConfigurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(defaultConfigurationSource.retrieve(eq(String.class))).thenReturn(CHANGED_VALUE);
        when(deltaValueInterceptor.created(eq(CHANGED_VALUE))).thenReturn(CHANGED_VALUE);
        
        configurationSource.refresh(defaultConfigurationSource);
        
        verify(defaultConfigurationSource, times(2)).isAvailable(eq(String.class));
        verify(defaultConfigurationSource, times(2)).retrieve(eq(String.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        verify(valueChangeListener).onChange(eq(THE_VALUE), isNull(String.class));
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
        verify(deltaValueInterceptor).created(eq(CHANGED_VALUE));
        verify(valueChangeListener).onChange(eq(CHANGED_VALUE), eq(THE_VALUE));
        verify(groupChangeListener, times(2)).onChange(isA(ConfigurationSource.class));
        
        verifyNoMoreInteractions(defaultConfigurationSource, deltaValueInterceptor, groupChangeListener, valueChangeListener);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#unregister(org.brekka.stillingar.core.ValueDefinition)}.
     */
    @Test
    public void testUnregisterValueDefinition() {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, false);
        
        configurationSource.unregister(valueDefinition);
        
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#unregister(org.brekka.stillingar.core.ValueDefinitionGroup)}.
     */
    @Test
    public void testUnregisterValueDefinitionGroup() {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinitionGroup, false);
        
        verify(defaultConfigurationSource).isAvailable(eq(String.class));
        verify(defaultConfigurationSource).retrieve(eq(String.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        configurationSource.unregister(valueDefinitionGroup);
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaConfigurationSource#shutdown()}.
     */
    @Test
    public void testShutdown() {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        when(deltaValueInterceptor.created(eq(THE_VALUE))).thenReturn(THE_VALUE);
        configurationSource.register(valueDefinition, false);
        
        verify(defaultConfigurationSource).isAvailable(eq(String.class));
        verify(defaultConfigurationSource).retrieve(eq(String.class));
        verify(deltaValueInterceptor).created(eq(THE_VALUE));
        
        configurationSource.shutdown();
        
        verify(deltaValueInterceptor).released(eq(THE_VALUE));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetDeltaOperationsNull() {
        configurationSource.setDeltaOperations(null);
    }

}
