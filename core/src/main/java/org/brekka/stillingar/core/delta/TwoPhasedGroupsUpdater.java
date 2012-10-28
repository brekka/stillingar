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
import java.util.Collection;
import java.util.List;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;

/**
 * Carries out updates to a list of groups in two phases. The first phase identifies the changes that need to occur for
 * all groups, the the second phase actually carries out the changes.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TwoPhasedGroupsUpdater {

    /**
     * The list of all value groups including the standalone group above.
     */
    private final Collection<ValueDefinitionGroup> valueGroups;

    /**
     * The configuration source to update from
     */
    private final ConfigurationSource configurationSource;

    /**
     * Enacts changes on values and groups.
     */
    private final DeltaOperations deltaOperations;

    /**
     * @param valueGroups
     *            The list of all value groups including the standalone group above.
     * @param configurationSource
     *            The configuration source to update from
     */
    public TwoPhasedGroupsUpdater(Collection<ValueDefinitionGroup> valueGroups, ConfigurationSource configurationSource,
            DeltaOperations deltaOperations) {
        this.valueGroups = valueGroups;
        this.configurationSource = configurationSource;
        this.deltaOperations = deltaOperations;
    }

    /**
     * The initial phase that will perform the purely read-only operation of locating the values for each of the value
     * definitions. This is the most likely source of errors where expressions may be wrong. An example of this is where
     * an expression is looking for an optional element that does not exist in any available configuration.
     * 
     * A problem in phase will leave the system in a completely consistent state, so the administrator can amend the
     * configuration and try again.
     * 
     * Phase one will attempt to resolve all values, collecting any errors into a list which will be thrown with the
     * {@link ChangeConfigurationException}. This is deliberate to avoid the need to fix configuration errors
     * iteratively (you can see them all at the same time).
     * 
     * @return the list of group actions to perform in phase two.
     * @throws ChangeConfigurationException
     *             if any problems are encountered resolving the values to update the {@link ValueDefinition}s with.
     */
    public List<GroupChangeAction> phaseOneUpdate() throws ChangeConfigurationException {
        Collection<ValueDefinitionGroup> valueGroups = this.valueGroups;
        List<GroupChangeAction> updateActionList = new ArrayList<GroupChangeAction>(valueGroups.size());
        List<GroupConfigurationException> groupErrors = new ArrayList<GroupConfigurationException>();

        for (ValueDefinitionGroup valueDefinitionGroup : valueGroups) {
            try {
                GroupChangeAction groupUpdateAction = deltaOperations.prepareGroupChange(valueDefinitionGroup,
                        configurationSource);
                updateActionList.add(groupUpdateAction);
            } catch (GroupConfigurationException e) {
                groupErrors.add(e);
            }
        }
        if (!groupErrors.isEmpty()) {
            throw new ChangeConfigurationException(String.format(
                    "Refresh phase one encountered %d errors out of %d groups", groupErrors.size(),
                    this.valueGroups.size()), groupErrors);
        }
        return updateActionList;
    }

    /**
     * In phase two, the actual task of writing the new values to the {@link ValueDefinition}s will take place. This is
     * the more sensitive part of the operation as an error here will potentially leave a part of the system partially
     * updated. If an error does occur in a {@link ValueDefinitionGroup}, this phase will continue to update any
     * remaining groups, writing the errors encountered to the error list that will be included with the
     * {@link ChangeConfigurationException}.
     * 
     * @param updateActionList the list of update actions to enact.
     * @throws ChangeConfigurationException
     *             if any problems are encountered carrying out the value updates.
     */
    public void phaseTwoUpdate(List<GroupChangeAction> updateActionList) throws ChangeConfigurationException {
        List<GroupConfigurationException> groupErrors = new ArrayList<GroupConfigurationException>();
        for (GroupChangeAction groupUpdateAction : updateActionList) {
            try {
                this.deltaOperations.enactGroupChange(groupUpdateAction, this.configurationSource);
            } catch (GroupConfigurationException e) {
                groupErrors.add(e);
            }
        }
        if (!groupErrors.isEmpty()) {
            throw new ChangeConfigurationException(String.format(
                    "Refresh phase two encountered %d errors out of %d actions", groupErrors.size(),
                    updateActionList.size()), groupErrors);
        }
    }
}
