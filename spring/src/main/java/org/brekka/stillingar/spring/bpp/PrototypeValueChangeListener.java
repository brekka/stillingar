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

package org.brekka.stillingar.spring.bpp;

import org.brekka.stillingar.core.ValueDefinition;

/**
 * Listener that is invoked when a value has been determined to be changed. Must be used in combination with a
 * {@link ValueDefinition} which# specified the context for the value being listened for changes to.
 * 
 * @author Andrew Taylor
 * 
 * @param <T> the type of the value being listened for
 */
public interface PrototypeValueChangeListener<T> {
    /**
     * Called to indicate that a change has occurred to the configured reference that this listener is monitoring.
     * 
     * @param newValue
     *            the updated value that the implementation to apply to the reference.
     * @param oldValue
     *            the previous value (if any).
     * @param target
     *            the target object to apply the value change to.           
     *            
     */
    void onChange(T newValue, T oldValue, Object target);
}
