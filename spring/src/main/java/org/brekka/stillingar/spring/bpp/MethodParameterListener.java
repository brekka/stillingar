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

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.ValueChangeListener;

/**
 * Listener for a {@link ConfigurationListener} method parameter that simply captures the value for subsequent lookup by
 * the {@link GroupChangeListener}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class MethodParameterListener<T> implements ValueChangeListener<T>, ParameterValueResolver {

    /**
     * The value by {@link #onChange(Object)}
     */
    private T value;

    public void onChange(T newValue) {
        this.value = newValue;
    }

    /**
     * Retrieve the value
     */
    public T getValue() {
        return value;
    }
}