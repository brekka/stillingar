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

package org.brekka.stillingar.core;

import java.util.List;

/**
 * TODO Description of AbstractDelegatingConfigurationSource
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractDelegatingConfigurationSource<CS extends ConfigurationSource> implements ConfigurationSource {

    
    private CS delegate;

    /**
     * @param delegate the delegate to set
     */
    protected final void setDelegate(CS delegate) {
        this.delegate = delegate;
    }
    
    /**
     * @return the delegate
     */
    public final CS getDelegate() {
        return delegate;
    }
    
    /**
     * @param expression
     * @return
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    public boolean isAvailable(String expression) {
        return delegate.isAvailable(expression);
    }

    /**
     * @param valueType
     * @return
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    public boolean isAvailable(Class<?> valueType) {
        return delegate.isAvailable(valueType);
    }

    /**
     * @param expression
     * @param valueType
     * @return
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    public <T> T retrieve(String expression, Class<T> valueType) {
        return delegate.retrieve(expression, valueType);
    }

    /**
     * @param valueType
     * @return
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    public <T> T retrieve(Class<T> valueType) {
        return delegate.retrieve(valueType);
    }

    /**
     * @param expression
     * @param valueType
     * @return
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        return delegate.retrieveList(expression, valueType);
    }

    /**
     * @param valueType
     * @return
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    public <T> List<T> retrieveList(Class<T> valueType) {
        return delegate.retrieveList(valueType);
    }
}
