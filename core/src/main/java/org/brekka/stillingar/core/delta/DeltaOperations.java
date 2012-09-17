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

import java.util.ArrayList;
import java.util.List;

import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueConfigurationException;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.core.GroupConfigurationException.Phase;

/**
 * Operations that prepare/change the state of {@link ValueDefinition} and {@link ValueDefinitionGroup}s.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DeltaOperations {

    /**
     * Resolve the latest value for the {@link ValueDefinition} from the {@link ConfigurationSource} and encapsulate it
     * in a {@link ValueChangeAction} to be enacted in a subsequent operation.
     * 
     * @param valueDefinition
     *            the value definition to prepare an update for
     * @param configurationSource
     *            the source from which to resolve the value.
     * @return the change action
     * @throws ConfigurationException
     *             if any problem occurs retrieving the value.
     */
    public ValueChangeAction prepareValueChange(ValueDefinition<?> valueDefinition,
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

    /**
     * Carry out the change to the value by invoking the {@link ValueChangeListener} with the value resolved by
     * {@link #prepareValueChange(ValueDefinition, ConfigurationSource)}.
     * 
     * @param valueChangeAction
     *            the action to perform
     * @throws ValueConfigurationException
     *             if the listener throws a {@link RuntimeException}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void enactValueChange(ValueChangeAction valueChangeAction) {
        ValueDefinition<?> valueDefinition = valueChangeAction.getValueDefinition();
        Object newValue = valueChangeAction.getNewValue();
        ValueChangeListener listener = valueDefinition.getListener();
        try {
            listener.onChange(newValue);
        } catch (RuntimeException e) {
            throw new ValueConfigurationException("value assignment", valueDefinition, e);
        }
    }

    /**
     * Prepares a {@link ValueChangeAction} for each {@link ValueDefinition} in the specified group.
     * 
     * @param valueDefinitionGroup
     *            the group to prepare value changes for
     * @param configurationSource
     *            the source from which to resolve the values of this group
     * @return the group change action
     * @throws GroupConfigurationException
     *             if there is a problem retrieving values for one or more of the {@link ValueDefinition}s in the group
     */
    public GroupChangeAction prepareGroupChange(ValueDefinitionGroup valueDefinitionGroup,
            ConfigurationSource configurationSource) {
        List<ValueDefinition<?>> valueDefinitionList = valueDefinitionGroup.getValues();
        List<ValueChangeAction> updateActions = new ArrayList<ValueChangeAction>(valueDefinitionList.size());
        List<ConfigurationException> valueResolveErrors = new ArrayList<ConfigurationException>();
        try {
            for (ValueDefinition<?> valueDefinition : valueDefinitionList) {
                ValueChangeAction valueChangeAction = prepareValueChange(valueDefinition, configurationSource);
                updateActions.add(valueChangeAction);
            }
        } catch (ConfigurationException e) {
            valueResolveErrors.add(e);
        }

        if (!valueResolveErrors.isEmpty()) {
            throw new GroupConfigurationException(valueDefinitionGroup.getName(), Phase.VALUE_DISCOVERY,
                    valueResolveErrors);
        }
        return new GroupChangeAction(valueDefinitionGroup, updateActions);
    }

    /**
     * Enact all of the value definition changes, then call the group listener (if there is one).
     * 
     * @param groupUpdateAction
     *            the group action to enact value changes on.
     * @param configurationSource
     *            will be passed to the {@link GroupChangeListener#onChange(ConfigurationSource)} method giving it a
     *            chance to lookup other values at runtime.
     * @throws GroupConfigurationException if any problem occurs with the value updates or listener invocation.
     */
    public void enactGroupChange(GroupChangeAction groupUpdateAction, ConfigurationSource configurationSource) {
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
             * Make the value updates by calling the listeners. Make an attempt to update all values, in case
             */
            try {
                for (ValueChangeAction valueUpdateAction : actionList) {
                    enactValueChange(valueUpdateAction);
                }
            } catch (ConfigurationException e) {
                valueUpdateErrors.add(e);
            }

            if (!valueUpdateErrors.isEmpty()) {
                throw new GroupConfigurationException(valueDefinitionGroup.getName(), Phase.VALUE_ASSIGNMENT,
                        valueUpdateErrors);
            }

            /*
             * We are clear to notify of the change
             */
            if (valueDefinitionGroup.getChangeListener() != null) {
                try {
                    valueDefinitionGroup.getChangeListener().onChange(configurationSource);
                } catch (RuntimeException e) {
                    throw new GroupConfigurationException(valueDefinitionGroup.getName(), Phase.LISTENER_INVOCATION, e);
                }
            }
        }
    }
}
