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

import java.lang.ref.WeakReference;

import org.brekka.stillingar.api.Replacement;
import org.brekka.stillingar.api.annotations.ConfigurationListener;
import org.brekka.stillingar.core.Expirable;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.ValueChangeListener;

/**
 * Listener for a {@link ConfigurationListener} method parameter that simply captures the value for subsequent lookup by
 * the {@link GroupChangeListener}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class MethodParameterListener implements ValueChangeListener<Object>, ParameterValueResolver, Expirable {

    private final boolean replacement;

    private final Object defaultPrimative;

    /**
     * The value set by {@link #onChange(Object)}
     */
    private WeakReference<?> value;
    
    public MethodParameterListener(boolean replacement, Object defaultPrimative) {
        this.replacement = replacement;
        this.defaultPrimative = defaultPrimative;
    }

    /**
     * When the value changes
     */
    @Override
    public void onChange(Object newValue, Object oldValue) {
        Object val = newValue;
        if (val == null) {
            // If the value type is a primitive, this will contain the default value to use.
            val = defaultPrimative;
        }
        if (replacement) {
            val = new Replacement<Object>(newValue, oldValue);
        }
        this.value = new WeakReference<Object>(val);
    }

    @Override
    public boolean isExpired() {
        return value.isEnqueued();
    }

    /**
     * Retrieve the value
     */
    @Override
    public Object getValue() {
        return value.get();
    }
}
