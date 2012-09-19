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

import org.brekka.stillingar.core.ValueDefinition;

/**
 * Encapsulates a future action to perform on a given {@link ValueDefinition}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ValueChangeAction {
    /**
     * The value definition to update
     */
    private final ValueDefinition<?,?> valueDefinition;

    /**
     * The new value that will ultimately be used to update the {@link ValueDefinition}
     */
    private final Object newValue;

    /**
     * @param valueDefinition
     *            The value definition to update
     * @param newValue
     *            The new value that will ultimately be used to update the {@link ValueDefinition}
     */
    public ValueChangeAction(ValueDefinition<?,?> valueDefinition, Object newValue) {
        this.valueDefinition = valueDefinition;
        this.newValue = newValue;
    }

    /**
     * The value definition to update
     * 
     * @return the value definition
     */
    public ValueDefinition<?,?> getValueDefinition() {
        return valueDefinition;
    }

    /**
     * The new value that will ultimately be used to update the {@link ValueDefinition}
     * 
     * @return the new value
     */
    public Object getNewValue() {
        return newValue;
    }
}