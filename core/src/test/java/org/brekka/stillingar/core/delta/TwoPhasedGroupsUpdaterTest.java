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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.SingleValueDefinition;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueConfigurationException;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.core.GroupConfigurationException.Phase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * TODO Description of TwoPhasedGroupsUpdaterTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class TwoPhasedGroupsUpdaterTest {
    
    private TwoPhasedGroupsUpdater twoPhasedGroupsUpdater;
    
    @Mock
    private ConfigurationSource configurationSource;

    @Mock
    private DeltaOperations deltaOperations = new DeltaOperations();
    
    private Collection<ValueDefinitionGroup> valueGroups = new ArrayList<ValueDefinitionGroup>();
    
    @Before
    public void setup() {
        twoPhasedGroupsUpdater = new TwoPhasedGroupsUpdater(valueGroups, configurationSource, deltaOperations);
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.TwoPhasedGroupsUpdater#phaseOneUpdate()}.
     */
    @Test
    public void testPhaseOneUpdateNormal() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        valueGroups.add(valueDefinitionGroup);
        
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieve(eq(String.class))).thenReturn("Value");
        when(deltaOperations.prepareValueChange(eq(valueDefinition), same(configurationSource))).thenCallRealMethod();
        when(deltaOperations.prepareGroupChange(eq(valueDefinitionGroup), same(configurationSource))).thenCallRealMethod();
        List<GroupChangeAction> gca = twoPhasedGroupsUpdater.phaseOneUpdate();
        GroupChangeAction groupChangeAction = gca.get(0);
        assertSame(valueDefinitionGroup, groupChangeAction.getGroup());
        List<ValueChangeAction> actionList = groupChangeAction.getActionList();
        ValueChangeAction valueChangeAction = actionList.get(0);
        assertSame(valueDefinition, valueChangeAction.getValueDefinition());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.TwoPhasedGroupsUpdater#phaseOneUpdate()}.
     */
    @Test
    public void testPhaseOneUpdateGroupThrows() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        valueGroups.add(valueDefinitionGroup);
        
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieve(eq(String.class))).thenReturn("Value");
//        when(deltaOperations.prepareValueChange(eq(valueDefinition), same(configurationSource))).thenCallRealMethod();
        GroupConfigurationException gce = new GroupConfigurationException("TestGroup", Phase.VALUE_DISCOVERY, Arrays.<ConfigurationException>asList());
        when(deltaOperations.prepareGroupChange(eq(valueDefinitionGroup), same(configurationSource))).thenThrow(gce);
        try {
            twoPhasedGroupsUpdater.phaseOneUpdate();
            fail("Expected ChangeConfigurationException");
        } catch (ChangeConfigurationException e) {
            List<GroupConfigurationException> groupErrors = e.getGroupErrors();
            GroupConfigurationException groupConfigurationException = groupErrors.get(0);
            assertSame(gce, groupConfigurationException);
        }
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.TwoPhasedGroupsUpdater#phaseOneUpdate()}.
     */
    @Test
    public void testPhaseOneUpdateValueThrows() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        valueGroups.add(valueDefinitionGroup);
        
        ValueConfigurationException vce = new ValueConfigurationException("Reason", valueDefinition);
        when(configurationSource.isAvailable(eq(String.class))).thenReturn(Boolean.TRUE);
        when(configurationSource.retrieve(eq(String.class))).thenReturn("Value");
        when(deltaOperations.prepareGroupChange(eq(valueDefinitionGroup), same(configurationSource))).thenCallRealMethod();
        when(deltaOperations.prepareValueChange(eq(valueDefinition), same(configurationSource))).thenThrow(vce);
        try {
            twoPhasedGroupsUpdater.phaseOneUpdate();
            fail("Expected ChangeConfigurationException");
        } catch (ChangeConfigurationException e) {
            List<GroupConfigurationException> groupErrors = e.getGroupErrors();
            GroupConfigurationException groupConfigurationException = groupErrors.get(0);
            List<ConfigurationException> errorList = groupConfigurationException.getErrorList();
            assertSame(vce, errorList.get(0));
        }
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.delta.TwoPhasedGroupsUpdater#phaseTwoUpdate(java.util.List)}.
     */
    @Test
    public void testPhaseTwoUpdate() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        
        ValueChangeAction valueChangeAction = new ValueChangeAction(valueDefinition, "Test");
        GroupChangeAction action = new GroupChangeAction(valueDefinitionGroup, Arrays.asList(valueChangeAction));
        List<GroupChangeAction> actions = Arrays.asList(action);
        doCallRealMethod().when(deltaOperations).enactValueChange(same(valueChangeAction));
        doCallRealMethod().when(deltaOperations).enactGroupChange(same(action), same(configurationSource));
        when(deltaOperations.prepareGroupChange(eq(valueDefinitionGroup), same(configurationSource))).thenCallRealMethod();
        
        twoPhasedGroupsUpdater.phaseTwoUpdate(actions);
        
        verify(valueChangeListener).onChange(eq("Test"), isNull(String.class));
        verify(groupChangeListener).onChange(eq(configurationSource));
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.delta.TwoPhasedGroupsUpdater#phaseTwoUpdate(java.util.List)}.
     */
    @Test
    public void testPhaseTwoUpdateWithError() throws Exception {
        ValueChangeListener<String> valueChangeListener = mock(ValueChangeListener.class);
        SingleValueDefinition<String> valueDefinition = new SingleValueDefinition<String>(String.class, valueChangeListener);
        GroupChangeListener groupChangeListener = mock(GroupChangeListener.class);
        List<ValueDefinition<?, ?>> valueList = Arrays.<ValueDefinition<?, ?>>asList(valueDefinition);
        ValueDefinitionGroup valueDefinitionGroup = new ValueDefinitionGroup("TestGroup", valueList, groupChangeListener);
        
        
        ValueChangeAction valueChangeAction = new ValueChangeAction(valueDefinition, "Test");
        GroupChangeAction action = new GroupChangeAction(valueDefinitionGroup, Arrays.asList(valueChangeAction));
        List<GroupChangeAction> actions = Arrays.asList(action);
        
        GroupConfigurationException gce = new GroupConfigurationException("TestGroup", Phase.VALUE_ASSIGNMENT, Arrays.<ConfigurationException>asList());
        
        doThrow(gce).when(deltaOperations).enactGroupChange(same(action), same(configurationSource));
        
        try {
            twoPhasedGroupsUpdater.phaseTwoUpdate(actions);
            fail("Expect ChangeConfigurationException");
        } catch (ChangeConfigurationException e) {
            List<GroupConfigurationException> groupErrors = e.getGroupErrors();
            GroupConfigurationException groupConfigurationException = groupErrors.get(0);
            assertSame(gce, groupConfigurationException);
        }
        
    }


}
