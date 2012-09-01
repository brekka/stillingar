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
 * A base configuration source which supports a default source as a fallback if the expression/type
 * cannot be resolved from the primary source.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractDefaultedConfigurationSource implements ConfigurationSource {

    /**
     * Should be returned by {@link #getDefaultSource()} if there is no default available.
     */
    protected static final ConfigurationSource NONE = new ConfigurationSource() {
        public <T> List<T> retrieveList(Class<T> valueType) { return null;  }
        public <T> List<T> retrieveList(String expression, Class<T> valueType) { return null; }
        public <T> T retrieve(Class<T> valueType) { return null; }
        public <T> T retrieve(String expression, Class<T> valueType) { return null; }
        public boolean isAvailable(Class<?> valueType) { return false; }
        public boolean isAvailable(String expression) { return false; }
    };
    
    /**
     * The source of defaults
     */
    private final ConfigurationSource defaultSource;
    
    
    public AbstractDefaultedConfigurationSource() {
        this(NONE);
    }
    
    public AbstractDefaultedConfigurationSource(ConfigurationSource defaultSource) {
        this.defaultSource = (defaultSource != null ? defaultSource : NONE);
    }

    /**
     * Retrieve the currently active source.
     * 
     * @return the active source
     */
    protected abstract ConfigurationSource getActiveSource();
    
    /**
     * Retrieve the default configuration source which will normally be loaded from the classpath. If there
     * are no defaults available, return {@value #NONE}.
     * 
     * @return the default source or {@value #NONE} if not available.
     */
    protected final ConfigurationSource getDefaultSource() {
        return defaultSource;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    public boolean isAvailable(Class<?> valueType) {
        return getActiveSource().isAvailable(valueType) 
             || getDefaultSource().isAvailable(valueType);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    public boolean isAvailable(String expression) {
        return getActiveSource().isAvailable(expression) 
             || getDefaultSource().isAvailable(expression);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    public <T> T retrieve(Class<T> valueType) {
        ConfigurationSource activeSource = getActiveSource();
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieve(valueType);
        }
        return getDefaultSource().retrieve(valueType);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    public <T> T retrieve(String expression, Class<T> valueType) {
        ConfigurationSource activeSource = getActiveSource();
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieve(expression, valueType);
        }
        return getDefaultSource().retrieve(expression, valueType);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    public <T> List<T> retrieveList(Class<T> valueType) {
        ConfigurationSource activeSource = getActiveSource();
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieveList(valueType);
        }
        return getDefaultSource().retrieveList(valueType);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        ConfigurationSource activeSource = getActiveSource();
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieveList(expression, valueType);
        }
        return getDefaultSource().retrieveList(expression, valueType);
    }
}
