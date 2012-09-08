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

import java.util.ArrayList;
import java.util.List;

import org.brekka.stillingar.core.GroupConfigurationException.Phase;


/**
 * TODO
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractChangeAwareConfigurationSource 
                extends AbstractDelegatingConfigurationSource<FallbackConfigurationSource>
             implements ChangeAwareConfigurationSource {
    
    /**
     * The group that will contain all of the {@link ValueDefinition}s that were registered via
     * {@link #register(ValueDefinition, boolean)}.
     */
    private ValueDefinitionGroup standaloneGroup;

    /**
     * The list of all value groups including the standalone group above.
     */
    private List<ValueDefinitionGroup> valueGroups = new ArrayList<ValueDefinitionGroup>();
    
    /**
     * 
     * @param defaultConfigurationSource the defaults to use (can be NONE).
     */
    protected AbstractChangeAwareConfigurationSource(ConfigurationSource defaultConfigurationSource) {
        setDelegate(new FallbackConfigurationSource(null, defaultConfigurationSource));
        this.standaloneGroup = new ValueDefinitionGroup("_standalone", 
                new ArrayList<ValueDefinition<?>>(), null, null);
        this.valueGroups.add(standaloneGroup);
    }
    

    public synchronized void register(ValueDefinition<?> valueDefinition, boolean fireImmediately) {
        ValueChangeAction valueChangeAction = prepareValueChange(valueDefinition, this);
        if (fireImmediately) {
            enactValueChange(valueChangeAction);
        }
        standaloneGroup.getValues().add(valueDefinition);
    }

    public synchronized void register(ValueDefinitionGroup valueDefinitionGroup, boolean fireImmediately) {
        GroupChangeAction groupUpdateAction = prepareGroupChange(valueDefinitionGroup, this);
        if (fireImmediately) {
            enactGroupChange(groupUpdateAction, this);
        }
        valueGroups.add(valueDefinitionGroup);
    }
    
    

    
    /**
     * Updates the primary configuration source and notify all registered listeners of the change. Must be
     * called at least once prior to any of the {@link #register} methods being called.
     * 
     * @return the list of errors encountered.
     */
    protected synchronized List<GroupConfigurationException> refresh(ConfigurationSource latest) {
        List<GroupConfigurationException> groupErrors = new ArrayList<GroupConfigurationException>();
        FallbackConfigurationSource newSource = new FallbackConfigurationSource(latest, 
                getDelegate().getSecondarySource());
        List<GroupChangeAction> updateActionList = phaseOneUpdate(this, groupErrors);

        // If there are no errors, move on to phase two
        if (groupErrors.isEmpty()) {
            phaseTwoUpdate(updateActionList, groupErrors, newSource);
        }
        
        // Still no errors, make the snapshot active.
        if (groupErrors.isEmpty()) {
            setDelegate(newSource);
        }
        return groupErrors;
    }
    
    /**
     * 
     * @param snapshot
     * @param groupErrors
     * @return
     */
    protected List<GroupChangeAction> phaseOneUpdate(ConfigurationSource configurationSource, List<GroupConfigurationException> groupErrors) {
        List<ValueDefinitionGroup> valueGroups = this.valueGroups;
        List<GroupChangeAction> updateActionList = new ArrayList<GroupChangeAction>(valueGroups.size());
        
        for (ValueDefinitionGroup valueDefinitionGroup : valueGroups) {
            try {
                GroupChangeAction groupUpdateAction = prepareGroupChange(valueDefinitionGroup, configurationSource);
                updateActionList.add(groupUpdateAction);
            } catch (GroupConfigurationException e) {
                groupErrors.add(e);
            }
        }
        return updateActionList;
    }
    
    protected void phaseTwoUpdate(List<GroupChangeAction> updateActionList, List<GroupConfigurationException> groupErrors,
            ConfigurationSource latest) {
        // No errors encountered, proceed to the next phase
        for (GroupChangeAction groupUpdateAction : updateActionList) {
            try {
                enactGroupChange(groupUpdateAction, latest);
            } catch (GroupConfigurationException e) {
                groupErrors.add(e);
            }
        }
    }
    
    protected GroupChangeAction prepareGroupChange(
            ValueDefinitionGroup valueDefinitionGroup,
            ConfigurationSource configurationSource) {
        List<ValueDefinition<?>> valueDefinitionList = valueDefinitionGroup.getValues();
        List<ValueChangeAction> updateActions = new ArrayList<ValueChangeAction>(valueDefinitionList.size());
        List<ConfigurationException> valueResolveErrors = new ArrayList<ConfigurationException>();
        
        /*
         * Phase 1 - attempt to retrieve all of the updated values.
         * Try to resolve every value so that a developer doesn't have to fix errors one-by-one. 
         * Instead give a summary of all errors encountered.
         */
        try {
            for (ValueDefinition<?> valueDefinition : valueDefinitionList) {
                ValueChangeAction valueChangeAction = prepareValueChange(valueDefinition, configurationSource);
                updateActions.add(valueChangeAction);
            }
        } catch (ConfigurationException e) {
            valueResolveErrors.add(e);
        }
        
        if (!valueResolveErrors.isEmpty()) {
            throw new GroupConfigurationException(valueDefinitionGroup.getName(), 
                    Phase.VALUE_DISCOVERY, valueResolveErrors);
        }
        return new GroupChangeAction(valueDefinitionGroup, updateActions);
    }
    
    protected void enactGroupChange(GroupChangeAction groupUpdateAction, ConfigurationSource latest) {
        ValueDefinitionGroup valueDefinitionGroup = groupUpdateAction.getGroup();
        List<ValueChangeAction> actionList = groupUpdateAction.getActionList();
        List<ConfigurationException> valueUpdateErrors = new ArrayList<ConfigurationException>();
        
        Object semaphore = valueDefinitionGroup.getSemaphore();
        if (semaphore == null) {
            // Semaphore must be set to something
            semaphore = new Object();
        }
        
        /*
         * Lock on the semaphore, providing an opportunity to atomically update a group of variables.
         */
        synchronized (semaphore) {
        
            /*
             * Phase 2 - Make the value updates by calling the listeners.
             * Make an attempt to update all values, in case 
             */
            try {
                for (ValueChangeAction valueUpdateAction : actionList) {
                    enactValueChange(valueUpdateAction);
                }
            } catch (ConfigurationException e) {
                valueUpdateErrors.add(e);
            }
            
            if (!valueUpdateErrors.isEmpty()) {
                throw new GroupConfigurationException(valueDefinitionGroup.getName(), 
                        Phase.VALUE_ASSIGNMENT, valueUpdateErrors);
            }
            
            /*
             * Phase 3 - We are clear to notify of the change
             */
            if (valueDefinitionGroup.getChangeListener() != null) {
                try {
                    valueDefinitionGroup.getChangeListener().onChange(latest);
                } catch (RuntimeException e) {
                    throw new GroupConfigurationException(valueDefinitionGroup.getName(), 
                            Phase.LISTENER_INVOCATION, e);
                }
            }
        }
    }

    protected ValueChangeAction prepareValueChange(ValueDefinition<?> valueDefinition, 
                ConfigurationSource configurationSource) {
        String expression = valueDefinition.getExpression();
        Class<?> type = valueDefinition.getType();
        Object result;
        if (valueDefinition.isList()) {
            if (expression != null) {
                result = configurationSource.retrieveList(expression, type);
            } else {
                result = configurationSource.retrieveList(type);
            }
        } else {
            if (expression != null) {
                result = configurationSource.retrieve(expression, type);
            } else {
                result = configurationSource.retrieve(type);
            }
        }
        return new ValueChangeAction(valueDefinition, result);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void enactValueChange(ValueChangeAction valueChangeAction) {
        ValueDefinition<?> valueDefinition = valueChangeAction.getValueDefinition();
        Object newValue = valueChangeAction.getNewValue();
        ValueChangeListener listener = valueDefinition.getListener();
        try {
            listener.onChange(newValue);
        } catch (RuntimeException e) {
            throw new ValueConfigurationException("value assignment", valueDefinition, e);
        }
    }
    
    protected class GroupChangeAction {
        public ValueDefinitionGroup getGroup() {
            return group;
        }
        public List<ValueChangeAction> getActionList() {
            return actionList;
        }
        private final ValueDefinitionGroup group;
        private final List<ValueChangeAction> actionList;
        public GroupChangeAction(ValueDefinitionGroup group,
                List<ValueChangeAction> actionList) {
            this.group = group;
            this.actionList = actionList;
        }
        
    }
    
    protected class ValueChangeAction {
        private final ValueDefinition<?> valueDefinition;
        private final Object newValue;
        public ValueChangeAction(ValueDefinition<?> valueDefinition,
                Object newValue) {
            this.valueDefinition = valueDefinition;
            this.newValue = newValue;
        }
        public ValueDefinition<?> getValueDefinition() {
            return valueDefinition;
        }
        public Object getNewValue() {
            return newValue;
        }
        
    }
}
