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

import java.nio.channels.IllegalSelectorException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ValueConfigurationException;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.SingleValueDefinition;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.core.ValueListDefinition;
import org.brekka.stillingar.core.GroupConfigurationException.Phase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * DeltaOperations Test
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeltaOperationsTest {
    
    private DeltaOperations deltaOperations;
    
    @Mock
    private ConfigurationSource configurationSource;
    
    @Before
    public void setup() {
        deltaOperations = new DeltaOperations();
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareValueChange(org.brekka.stillingar.core.ValueDefinition, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareValueChangeSingleExpression() {
        String value = "Test";
        String expression = "/c:Test";
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> vd = new SingleValueDefinition<String>(String.class, expression, valueChangeListener);
        
        when(configurationSource.isAvailable(eq(expression))).thenReturn(Boolean.TRUE);
        
        when(configurationSource.retrieve(eq(expression), eq(String.class))).thenReturn(value);
        
        ValueChangeAction valueChangeAction = deltaOperations.prepareValueChange(vd, configurationSource);
        
        verify(configurationSource).retrieve(eq(expression), eq(String.class));
        
        assertEquals(value, valueChangeAction.getNewValue());
        assertSame(vd, valueChangeAction.getValueDefinition());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareValueChange(org.brekka.stillingar.core.ValueDefinition, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareValueChangeSingleType() {
        String value = "Test";
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> vd = new SingleValueDefinition<String>(String.class, valueChangeListener);
        
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieve(eq(String.class))).thenReturn(value);
        
        ValueChangeAction valueChangeAction = deltaOperations.prepareValueChange(vd, configurationSource);
        
        verify(configurationSource).retrieve(eq(String.class));
        
        assertEquals(value, valueChangeAction.getNewValue());
        assertSame(vd, valueChangeAction.getValueDefinition());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareValueChange(org.brekka.stillingar.core.ValueDefinition, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareValueChangeListExpression() {
        List<String> valueList = Arrays.asList("Test1", "Test2", "Test3");
        String expression = "/c:Test";
        ValueChangeListener<List<String>> valueChangeListener = mock(ValueChangeListener.class);
        ValueListDefinition<String> vd = new ValueListDefinition<String>(String.class, expression, valueChangeListener);
        
        when(configurationSource.isAvailable(eq(expression))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieveList(eq(expression), eq(String.class))).thenReturn(valueList);
        
        ValueChangeAction valueChangeAction = deltaOperations.prepareValueChange(vd, configurationSource);
        
        verify(configurationSource).retrieveList(eq(expression), eq(String.class));
        
        assertEquals(valueList, valueChangeAction.getNewValue());
        assertSame(vd, valueChangeAction.getValueDefinition());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareValueChange(org.brekka.stillingar.core.ValueDefinition, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareValueChangeListType() {
        List<String> valueList = Arrays.asList("Test1", "Test2", "Test3");
        ValueChangeListener<List<String>> valueChangeListener = mock(ValueChangeListener.class);
        ValueListDefinition<String> vd = new ValueListDefinition<String>(String.class, valueChangeListener);
        
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieveList(eq(String.class))).thenReturn(valueList);
        
        ValueChangeAction valueChangeAction = deltaOperations.prepareValueChange(vd, configurationSource);
        
        verify(configurationSource).retrieveList(eq(String.class));
        
        assertEquals(valueList, valueChangeAction.getNewValue());
        assertSame(vd, valueChangeAction.getValueDefinition());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareValueChange(org.brekka.stillingar.core.ValueDefinition, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareValueChangeListTypeWithError() {
        ValueChangeListener<List<String>> valueChangeListener = mock(ValueChangeListener.class);
        ValueListDefinition<String> vd = new ValueListDefinition<String>(String.class, valueChangeListener);
        
        ConfigurationException error = new ConfigurationException("Error");
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieveList(eq(String.class))).thenThrow(error);
        
        try {
            deltaOperations.prepareValueChange(vd, configurationSource);
            fail("Exception expected");
        } catch (ConfigurationException e) {
            assertSame(error, e);
        }
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareGroupChange(org.brekka.stillingar.core.ValueDefinitionGroup, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareGroupChange() {
        String value = "Test";
        String expression = "/c:Test";
        List<String> valueList = Arrays.asList("Test1", "Test2", "Test3");
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        SingleValueDefinition<?> vdSingleType = new SingleValueDefinition<String>(String.class, mock(ValueChangeListener.class));
        SingleValueDefinition<?> vdSingleExpression = new SingleValueDefinition<String>(String.class, expression, mock(ValueChangeListener.class));
        ValueListDefinition<?> vdListType = new ValueListDefinition<String>(String.class, mock(ValueChangeListener.class));
        ValueListDefinition<?> vdListExpression = new ValueListDefinition<String>(String.class, expression, mock(ValueChangeListener.class));
        List<ValueDefinition<?,?>> values = Arrays.<ValueDefinition<?,?>>asList(vdSingleType, vdSingleExpression, vdListType, vdListExpression);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("Test", values, groupChangeListener);
        
        when(configurationSource.isAvailable(eq(expression))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieve(eq(expression), eq(String.class))).thenReturn(value);
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieve(eq(String.class))).thenReturn(value);
        when(configurationSource.isAvailable(eq(expression))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieveList(eq(expression), eq(String.class))).thenReturn(valueList);
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieveList(eq(String.class))).thenReturn(valueList);
        
        GroupChangeAction groupChangeAction = deltaOperations.prepareGroupChange(valueDefinitionGroup, configurationSource);
        
        verify(configurationSource).retrieve(eq(expression), eq(String.class));
        verify(configurationSource).retrieve(eq(String.class));
        verify(configurationSource).retrieveList(eq(expression), eq(String.class));
        verify(configurationSource).retrieveList(eq(String.class));
        
        assertSame(valueDefinitionGroup, groupChangeAction.getGroup());
        List<ValueChangeAction> actionList = groupChangeAction.getActionList();
        
        assertSame(value, actionList.get(0).getNewValue());
        assertSame(vdSingleType, actionList.get(0).getValueDefinition());
        
        assertSame(value, actionList.get(1).getNewValue());
        assertSame(vdSingleExpression, actionList.get(1).getValueDefinition());
        
        assertSame(valueList, actionList.get(2).getNewValue());
        assertSame(vdListType, actionList.get(2).getValueDefinition());
        
        assertSame(valueList, actionList.get(3).getNewValue());
        assertSame(vdListExpression, actionList.get(3).getValueDefinition());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#prepareGroupChange(org.brekka.stillingar.core.ValueDefinitionGroup, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testPrepareGroupChangeWithValueError() {
        ValueDefinition<?,?> vdSingleType = new SingleValueDefinition<String>(String.class, mock(ValueChangeListener.class));
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("Test", Arrays.<ValueDefinition<?,?>>asList(vdSingleType), groupChangeListener);
        ConfigurationException configurationException = new ConfigurationException("Message");
        when(configurationSource.isAvailable(eq(String.class))).thenThrow(configurationException);
        
        try {
            deltaOperations.prepareGroupChange(valueDefinitionGroup, configurationSource);
            fail("Expected exception");
        } catch (GroupConfigurationException e) {
            assertEquals("Test", e.getGroupName());
            assertEquals(Phase.VALUE_DISCOVERY, e.getPhase());
            assertSame(configurationException, e.getErrorList().get(0));
        }
    }
    
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#enactValueChange(org.brekka.stillingar.core.delta.ValueChangeAction)}.
     */
    @Test
    public void testEnactValueChange() {
        String value = "Test";
        String expression = "/c:Test";
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        ValueDefinition<String,?> vd = new SingleValueDefinition<String>(String.class, expression, valueChangeListener);
        ValueChangeAction vca = new ValueChangeAction(vd, value);
        
        deltaOperations.enactValueChange(vca);
        
        verify(valueChangeListener).onChange(eq(value), isNull(String.class));
    }
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#enactValueChange(org.brekka.stillingar.core.delta.ValueChangeAction)}.
     */
    @Test(expected=ValueConfigurationException.class)
    public void testEnactValueChangeWithError() {
        String value = "Test";
        String expression = "/c:Test";
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        ValueDefinition<String,?> vd = new SingleValueDefinition<String>(String.class, expression, valueChangeListener);
        ValueChangeAction vca = new ValueChangeAction(vd, value);
        
        doThrow(new IllegalSelectorException()).when(valueChangeListener).onChange(eq(value), isNull(String.class));
        
        deltaOperations.enactValueChange(vca);
        verify(valueChangeListener).onChange(eq(value), isNull(String.class));
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#enactGroupChange(org.brekka.stillingar.core.delta.GroupChangeAction, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testEnactGroupChange() {
        String value = "Test";
        String expression = "/c:Test";
        List<String> valueList = Arrays.asList("Test1", "Test2", "Test3");
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        SingleValueDefinition<String> vdSingleType = new SingleValueDefinition<String>(String.class, mock(ValueChangeListener.class));
        SingleValueDefinition<String> vdSingleExpression = new SingleValueDefinition<String>(String.class, expression, mock(ValueChangeListener.class));
        ValueListDefinition<String> vdListType = new ValueListDefinition<String>(String.class, mock(ValueChangeListener.class));
        ValueListDefinition<String> vdListExpression = new ValueListDefinition<String>(String.class, expression, mock(ValueChangeListener.class));
        
        ValueChangeAction vcaSingleType = new ValueChangeAction(vdSingleType, value);
        ValueChangeAction vcaSingleExpression = new ValueChangeAction(vdSingleExpression, value);
        ValueChangeAction vcaListType = new ValueChangeAction(vdListType, valueList);
        ValueChangeAction vcaListExpression = new ValueChangeAction(vdListExpression, valueList);
        List<ValueDefinition<?,?>> values = Arrays.<ValueDefinition<?,?>>asList(vdSingleType, vdSingleExpression, vdListType, vdListExpression);
        List<ValueChangeAction> vcaList = Arrays.asList(vcaSingleType, vcaSingleExpression, vcaListType, vcaListExpression);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("Test", values, groupChangeListener);
        GroupChangeAction gca = new GroupChangeAction(valueDefinitionGroup, vcaList);
        
        deltaOperations.enactGroupChange(gca, configurationSource);
        verify(vdSingleType.getChangeListener()).onChange(eq(value), isNull(String.class));
        verify(vdSingleExpression.getChangeListener()).onChange(eq(value), isNull(String.class));
        verify(vdListType.getChangeListener()).onChange(eq(valueList), isNull(List.class));
        verify(vdListExpression.getChangeListener()).onChange(eq(valueList), isNull(List.class));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#enactGroupChange(org.brekka.stillingar.core.delta.GroupChangeAction, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testEnactGroupChangeAssignmentError() {
        List<String> valueList = Arrays.asList("Test1", "Test2", "Test3");
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        ValueChangeListener<List<String>> valueChangeListener = mock(ValueChangeListener.class);
        ValueListDefinition<String> vdListType = new ValueListDefinition<String>(String.class, valueChangeListener);
        
        ValueChangeAction vcaListType = new ValueChangeAction(vdListType, valueList);
        List<ValueDefinition<?,?>> values = Arrays.<ValueDefinition<?,?>>asList(vdListType);
        List<ValueChangeAction> vcaList = Arrays.asList(vcaListType);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("Test", values, groupChangeListener);
        GroupChangeAction gca = new GroupChangeAction(valueDefinitionGroup, vcaList);
        
        IllegalStateException illegalStateException = new IllegalStateException();
        doThrow(illegalStateException).when(valueChangeListener).onChange(eq(valueList), isNull(List.class));
        
        try {
            deltaOperations.enactGroupChange(gca, configurationSource);
        } catch (GroupConfigurationException e) {
            assertEquals("Test", e.getGroupName());
            assertEquals(Phase.VALUE_ASSIGNMENT, e.getPhase());
            assertSame(illegalStateException, e.getErrorList().get(0).getCause());
        }
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.DeltaOperations#enactGroupChange(org.brekka.stillingar.core.delta.GroupChangeAction, org.brekka.stillingar.api.ConfigurationSource)}.
     */
    @Test
    public void testEnactGroupChangeListenerError() {
        List<String> valueList = Arrays.asList("Test1", "Test2", "Test3");
        
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        ValueChangeListener<List<String>> valueChangeListener = mock(ValueChangeListener.class);
        ValueListDefinition<String> vdListType = new ValueListDefinition<String>(String.class, valueChangeListener);
        
        ValueChangeAction vcaListType = new ValueChangeAction(vdListType, valueList);
        List<ValueDefinition<?,?>> values = Arrays.<ValueDefinition<?,?>>asList(vdListType);
        List<ValueChangeAction> vcaList = Arrays.asList(vcaListType);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("Test", values, groupChangeListener, this);
        GroupChangeAction gca = new GroupChangeAction(valueDefinitionGroup, vcaList);
        
        IllegalStateException illegalStateException = new IllegalStateException();
        doThrow(illegalStateException).when(groupChangeListener).onChange(eq(configurationSource));
        
        try {
            deltaOperations.enactGroupChange(gca, configurationSource);
        } catch (GroupConfigurationException e) {
            assertEquals("Test", e.getGroupName());
            assertEquals(Phase.LISTENER_INVOCATION, e.getPhase());
            assertSame(illegalStateException, e.getCause());
        }
    }

}
