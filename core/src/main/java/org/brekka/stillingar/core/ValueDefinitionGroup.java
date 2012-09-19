/*
 * Copyright 2011 the original author or authors.
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

import java.util.List;

/**
 * Encapsulates the details of a 'target' containing multiple values to be updated in an atomic manner, with an optional
 * {@link GroupChangeListener} that should be called once all values are set.
 * 
 * @author Andrew Taylor
 */
public final class ValueDefinitionGroup {

    /**
     * The label for this group. Used in exceptions to provide context. Does not have to be unique, but is more useful
     * if it is.
     */
    private final String name;

    /**
     * The list of values that are part of this group and as such should be updated with it.
     */
    private final List<ValueDefinition<?,?>> values;

    /**
     * Optional listener that will be invoked once all values have been updated.
     */
    private final GroupChangeListener changeListener;

    /**
     * Optional locking semaphore that can be used to ensure exclusive access to the 'target' while it is being updated
     * and the listener invoked.
     */
    private final Object semaphore;

    /**
     * @param name
     *            The label for this group. Used in exceptions to provide context. Does not have to be unique, but is
     *            more useful if it is.
     * @param values
     *            The list of values that are part of this group and as such should be updated with it.
     * @param changeListener
     *            Optional listener that will be invoked once all values have been updated.
     */
    public ValueDefinitionGroup(String name, List<ValueDefinition<?,?>> values, GroupChangeListener changeListener) {
        this(name, values, changeListener, null);
    }

    /**
     * @param name
     *            The label for this group. Used in exceptions to provide context. Does not have to be unique, but is
     *            more useful if it is.
     * @param values
     *            The list of values that are part of this group and as such should be updated with it.
     * @param changeListener
     *            Optional listener that will be invoked once all values have been updated.
     * @param semaphore
     *            Optional locking semaphore that can be used to ensure exclusive access to the 'target' while it is
     *            being updated and the listener invoked.
     */
    public ValueDefinitionGroup(String name, List<ValueDefinition<?,?>> values, GroupChangeListener changeListener,
            Object semaphore) {
        this.name = name;
        this.values = values;
        this.changeListener = changeListener;
        this.semaphore = semaphore;
    }

    /**
     * The label for this group. Used in exceptions to provide context.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The list of values that are part of this group and as such should be updated with it.
     * 
     * @return
     */
    public List<ValueDefinition<?,?>> getValues() {
        return values;
    }

    /**
     * Optional listener that will be invoked once all values have been updated.
     * 
     * @return
     */
    public GroupChangeListener getChangeListener() {
        return changeListener;
    }

    /**
     * Optional locking semaphore that can be used to ensure exclusive access to the 'target' while it is being updated
     * and the listener invoked.
     * 
     * @return
     */
    public Object getSemaphore() {
        return semaphore;
    }
}
