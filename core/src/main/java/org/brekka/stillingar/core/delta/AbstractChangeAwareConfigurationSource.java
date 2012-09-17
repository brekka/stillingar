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

import org.brekka.stillingar.core.AbstractDelegatingConfigurationSource;
import org.brekka.stillingar.core.ChangeAwareConfigurationSource;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.FallbackConfigurationSource;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;

/**
 * A configuration source that is 'change aware' supporting the registration of value definitions and value defintion
 * groups that will have their change listeners updated when the underlying configuration source changes.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractChangeAwareConfigurationSource extends
        AbstractDelegatingConfigurationSource<FallbackConfigurationSource> implements ChangeAwareConfigurationSource {

    /**
     * The group that will contain all of the {@link ValueDefinition}s that were registered via
     * {@link #register(ValueDefinition, boolean)}.
     */
    private final ValueDefinitionGroup standaloneGroup;

    /**
     * The list of all value groups including the standalone group above.
     */
    private final List<ValueDefinitionGroup> valueGroups = new ArrayList<ValueDefinitionGroup>();

    /**
     * Operations for changing value definitions/groups
     */
    private DeltaOperations deltaOperations = new DeltaOperations();

    /**
     * 
     * @param defaultConfigurationSource
     *            the defaults to use (can be NONE).
     */
    protected AbstractChangeAwareConfigurationSource(ConfigurationSource defaultConfigurationSource) {
        setDelegate(new FallbackConfigurationSource(null, defaultConfigurationSource));
        this.standaloneGroup = new ValueDefinitionGroup("_standalone", new ArrayList<ValueDefinition<?>>(), null, null);
        this.valueGroups.add(standaloneGroup);
    }

    /**
     * Register a value definition
     */
    public synchronized void register(ValueDefinition<?> valueDefinition, boolean fireImmediately) {
        if (fireImmediately) {
            ValueChangeAction valueChangeAction = deltaOperations.prepareValueChange(valueDefinition, this);
            deltaOperations.enactValueChange(valueChangeAction);
        }
        standaloneGroup.getValues().add(valueDefinition);
    }

    /**
     * Register a value group definition
     */
    public synchronized void register(ValueDefinitionGroup valueDefinitionGroup, boolean fireImmediately) {
        if (fireImmediately) {
            GroupChangeAction groupUpdateAction = deltaOperations.prepareGroupChange(valueDefinitionGroup, this);
            deltaOperations.enactGroupChange(groupUpdateAction, this);
        }
        valueGroups.add(valueDefinitionGroup);
    }

    /**
     * Updates the primary configuration source and notify all registered listeners of the change. Must be called at
     * least once prior to any of the {@link #register} methods being called.
     * 
     * @throws if
     *             an problems are encountered during the first or second phases.
     */
    protected synchronized void refresh(ConfigurationSource latest) throws ChangeConfigurationException {
        FallbackConfigurationSource newSource = new FallbackConfigurationSource(latest, getDelegate()
                .getSecondarySource());

        TwoPhasedGroupsUpdater updater = new TwoPhasedGroupsUpdater(valueGroups, newSource, deltaOperations);

        // Phase One
        List<GroupChangeAction> updateActionList = updater.phaseOneUpdate();

        // Phase Two
        updater.phaseTwoUpdate(updateActionList);

        // No exception, means success
        setDelegate(newSource);
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
}
