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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.ConfigurationService;
import org.brekka.stillingar.core.DelegatingConfigurationSource;
import org.brekka.stillingar.core.Expirable;
import org.brekka.stillingar.core.FallbackConfigurationSource;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;

/**
 * A configuration source that is 'change aware' supporting the registration of value definitions and value definition
 * groups that will have their change listeners updated when the underlying configuration source changes.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DeltaConfigurationService 
      extends DelegatingConfigurationSource<FallbackConfigurationSource> 
   implements ConfigurationService {

    /**
     * The group that will contain all of the {@link ValueDefinition}s that were registered via
     * {@link #register(ValueDefinition, boolean)}.
     */
    private final ValueDefinitionGroup standaloneGroup;

    /**
     * The list of all value groups including the standalone group above.
     */
    private final Set<ValueDefinitionGroup> valueGroups = new LinkedHashSet<ValueDefinitionGroup>();

    /**
     * Operations for changing value definitions/groups
     */
    private DeltaOperations deltaOperations = new DeltaOperations();
    
    /**
     * A map of all values that are currently applied to the various value definitions.
     */
    private Map<ValueDefinition<?, ?>, WeakReference<?>> lastValueMap;
    
    /**
     * Interceptor for value changes.
     */
    private DeltaValueInterceptor deltaValueInterceptor;

    
    public DeltaConfigurationService(ConfigurationSource defaultConfigurationSource) {
        super(new FallbackConfigurationSource(null, defaultConfigurationSource));
        // Use a LinkedHashSet to quick add/removal and iteration in order of addition.
        this.standaloneGroup = new ValueDefinitionGroup("_standalone", new LinkedHashSet<ValueDefinition<?, ?>>(),
                null, null);
        this.valueGroups.add(standaloneGroup);
        this.lastValueMap = Collections.emptyMap();
    }

    /**
     * Register a value definition
     */
    @Override
    public synchronized void register(ValueDefinition<?, ?> valueDefinition, boolean fireImmediately) {
        ValueChangeAction valueChangeAction = deltaOperations.prepareValueChange(valueDefinition, this);
        Object newValue = interceptCreatedValue(valueChangeAction.getNewValue());
        if (fireImmediately) {
            deltaOperations.enactValueChange(new ValueChangeAction(valueChangeAction.getValueDefinition(), newValue));
        }
        lastValueMap.put(valueDefinition, new WeakReference<Object>(valueChangeAction.getNewValue()));
        standaloneGroup.getValues().add(valueDefinition);
    }

    /**
     * Register a value group definition
     */
    @Override
    public synchronized void register(ValueDefinitionGroup valueDefinitionGroup, boolean fireImmediately) {
        GroupChangeAction groupUpdateAction = deltaOperations.prepareGroupChange(valueDefinitionGroup, this);
        groupUpdateAction = interceptGroupRefresh(groupUpdateAction, lastValueMap);
        if (fireImmediately) {
            deltaOperations.enactGroupChange(groupUpdateAction, this);
        }
        valueGroups.add(valueDefinitionGroup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.ChangeAwareConfigurationSource#unregister(org.brekka.stillingar.core.ValueDefinition)
     */
    @Override
    public synchronized void unregister(ValueDefinition<?, ?> valueDefinition) {
        Collection<ValueDefinition<?, ?>> values = standaloneGroup.getValues();
        values.remove(valueDefinition);
        releaseValue(valueDefinition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.ChangeAwareConfigurationSource#unregister(org.brekka.stillingar.core.ValueDefinitionGroup
     * )
     */
    @Override
    public synchronized void unregister(ValueDefinitionGroup valueGroup) {
        if (valueGroup == standaloneGroup) {
            throw new IllegalStateException("May not remove the standalone group");
        }
        valueGroups.remove(valueGroup);
        for (ValueDefinition<?,?> valueDefinition : valueGroup.getValues()) {
            releaseValue(valueDefinition);
        }
    }
    
    /**
     * Shutdown this {@link ConfigurationSource}, releasing all values.
     */
    public synchronized void shutdown() {
        Collection<WeakReference<?>> values = lastValueMap.values();
        for (WeakReference<?> weakReference : values) {
            Object object = weakReference.get();
            if (object != null) {
                interceptReleasedValue(object);
            }
        }
        this.lastValueMap = Collections.emptyMap();
        this.valueGroups.clear();
        this.standaloneGroup.getValues().clear();
    }


    /**
     * Updates the primary configuration source and notify all registered listeners of the change. Must be called at
     * least once prior to any of the {@link #register} methods being called.
     * 
     * @throws ChangeConfigurationException
     *             if problems are encountered during the first or second phases.
     */
    protected synchronized void refresh(ConfigurationSource latest) throws ChangeConfigurationException {
        Map<ValueDefinition<?, ?>, WeakReference<?>> newValueMap = new LinkedHashMap<ValueDefinition<?, ?>, WeakReference<?>>();
        FallbackConfigurationSource newSource = new FallbackConfigurationSource(latest, getDelegate()
                .getSecondarySource());

        checkAndRemoveExpired();

        TwoPhasedGroupsUpdater updater = new TwoPhasedGroupsUpdater(valueGroups, newSource, deltaOperations);

        // Phase One
        List<GroupChangeAction> updateActionList = updater.phaseOneUpdate();
        
        updateActionList = interceptRefresh(updateActionList, newValueMap);

        // Phase Two
        updater.phaseTwoUpdate(updateActionList);

        // No exception, means success
        setDelegate(newSource);
        this.lastValueMap = newValueMap;
    }


    /**
     * @param updateActionList
     * @return
     */
    protected List<GroupChangeAction> interceptRefresh(List<GroupChangeAction> updateActionList, 
            Map<ValueDefinition<?, ?>, WeakReference<?>> newValueMap) {
        List<GroupChangeAction> groupChangeActions = new ArrayList<GroupChangeAction>();
        for (GroupChangeAction groupChangeAction : updateActionList) {
            groupChangeActions.add(interceptGroupRefresh(groupChangeAction, newValueMap));
        }
        return groupChangeActions;
    }

    /**
     * @param groupChangeAction
     * @return
     */
    protected GroupChangeAction interceptGroupRefresh(GroupChangeAction groupChangeAction, 
            Map<ValueDefinition<?, ?>, WeakReference<?>> newValueMap) {
        List<ValueChangeAction> currentActionList = groupChangeAction.getActionList();
        List<ValueChangeAction> updatedActionList = new ArrayList<ValueChangeAction>(currentActionList.size());
        ValueDefinitionGroup group = groupChangeAction.getGroup();
        for (ValueChangeAction valueChangeAction : groupChangeAction.getActionList()) {
            ValueDefinition<?, ?> valueDefinition = valueChangeAction.getValueDefinition();
            
            // Release old value
            WeakReference<?> originalValueRef = lastValueMap.get(valueDefinition);
            Object oldValue = null;
            if (originalValueRef != null) {
                oldValue = originalValueRef.get();
                if (oldValue != null) {
                    interceptReleasedValue(oldValue);
                }
            }
            
            // Prepare new value
            Object newValue = valueChangeAction.getNewValue();
            newValue = interceptCreatedValue(newValue);
            newValueMap.put(valueDefinition, new WeakReference<Object>(newValue));
            updatedActionList.add(new ValueChangeAction(valueDefinition, newValue, oldValue));
        }
        return new GroupChangeAction(group, updatedActionList);
    }

    /**
     * Check whether any of the value/group definitions have expired and remove them.
     */
    protected void checkAndRemoveExpired() {
        Iterator<ValueDefinitionGroup> valueGroupIterator = valueGroups.iterator();
        while (valueGroupIterator.hasNext()) {
            ValueDefinitionGroup valueDefinitionGroup = valueGroupIterator.next();
            if (isExpired(valueDefinitionGroup.getChangeListener())) {
                valueGroupIterator.remove();
            }
        }

        Collection<ValueDefinition<?, ?>> standaloneValues = standaloneGroup.getValues();
        Iterator<ValueDefinition<?, ?>> standaloneValueIterator = standaloneValues.iterator();
        while (standaloneValueIterator.hasNext()) {
            ValueDefinition<?, ?> valueDefinition = standaloneValueIterator.next();
            if (isExpired(valueDefinition.getChangeListener())) {
                standaloneValueIterator.remove();
            }
        }
    }

    /**
     * @param valueDefinition
     */
    protected void releaseValue(ValueDefinition<?, ?> valueDefinition) {
        WeakReference<?> removedValueRef = lastValueMap.remove(valueDefinition);
        if (removedValueRef != null) {
            Object removedValue = removedValueRef.get();
            interceptReleasedValue(removedValue);
        }
    }


    /**
     * @param newValue
     * @param valueDefinition
     */
    protected Object interceptCreatedValue(Object newValue) {
        if (deltaValueInterceptor == null) {
            return newValue;
        }
        return deltaValueInterceptor.created(newValue);
    }
    
    /**
     * @param valueDefinition
     */
    protected void interceptReleasedValue(Object value) {
        if (deltaValueInterceptor == null) {
            return;
        }
        deltaValueInterceptor.released(value);
    }
    
    /**
     * @param deltaOperations
     *            the deltaOperations to set
     */
    public void setDeltaOperations(DeltaOperations deltaOperations) {
        if (deltaOperations == null) {
            throw new IllegalArgumentException("Delta operations may not be null");
        }
        this.deltaOperations = deltaOperations;
    }
    
    /**
     * @param deltaValueInterceptor the deltaValueInterceptor to set
     */
    public void setDeltaValueInterceptor(DeltaValueInterceptor deltaValueInterceptor) {
        this.deltaValueInterceptor = deltaValueInterceptor;
    }
    

    /**
     * Checks to see if the specified instance implements {@link Expirable} and if it is, check whether it has
     * expired.
     * 
     * @param val
     *            the value to check
     * @return true if the listener has expired.
     */
    private static boolean isExpired(Object val) {
        if (val instanceof Expirable) {
            return ((Expirable) val).isExpired();
        }
        return false;
    }
}
