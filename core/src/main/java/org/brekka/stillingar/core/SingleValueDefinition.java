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
 * Encapsulates the details of a configuration value that should be listened for, and the action that should be taken
 * when it is updated.
 * 
 * @author Andrew Taylor
 */
public final class SingleValueDefinition<T> extends ValueDefinition<T, ValueChangeListener<T>> {

    /**
     * @param type
     *            The type of the value that is used to define what is expected to be returned. In the absence of an
     *            expression it can also be used to determine what value will be returned. When actual return type is a
     *            {@link List}, this value will be the type of value within the list.
     * @param expression
     *            The expression that should be used to determine what value is to be returned.
     * @param listener
     *            The listener that will be called in response to this value being changed.
     */
    public SingleValueDefinition(Class<T> type, String expression, ValueChangeListener<T> listener) {
        super(type, expression, listener);
    }

    /**
     * @param type
     *            The type of the value that is used to define what is expected to be returned. In the absence of an
     *            expression it can also be used to determine what value will be returned. When actual return type is a
     *            {@link List}, this value will be the type of value within the list.
     * @param listener
     *            The listener that will be called in response to this value being changed.
     */
    public SingleValueDefinition(Class<T> type, ValueChangeListener<T> listener) {
        this(type, null, listener);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ValueDefinition#getListener()
     */
    @SuppressWarnings("unchecked")
    @Override
    public ValueChangeListener<T> getListener() {
        return (ValueChangeListener<T>) super.getListener();
    }
}
