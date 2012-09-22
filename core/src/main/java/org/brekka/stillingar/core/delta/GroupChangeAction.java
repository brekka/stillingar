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

import org.brekka.stillingar.core.ValueDefinitionGroup;

/**
 * Encapsulates a future action to perform on a given {@link ValueDefinitionGroup}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class GroupChangeAction {

    /**
     * The group to update
     */
    private final ValueDefinitionGroup group;

    /**
     * List of actions to perform on the values contained within this group.
     */
    private final List<ValueChangeAction> actionList;

    /**
     * @param group
     *            The group to update
     * @param actionList
     *            List of actions to perform on the values contained within this group.
     */
    public GroupChangeAction(ValueDefinitionGroup group, List<ValueChangeAction> actionList) {
        this.group = group;
        this.actionList = actionList;
    }

    /**
     * The group to be updated
     * 
     * @return the group
     */
    public ValueDefinitionGroup getGroup() {
        return group;
    }

    /**
     * List of actions to perform on the values contained within this group.
     * 
     * @return the action list
     */
    public List<ValueChangeAction> getActionList() {
        return new ArrayList<ValueChangeAction>(actionList);
    }

}