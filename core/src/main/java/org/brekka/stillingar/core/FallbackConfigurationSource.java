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
 * A base configuration source which supports a default source as a fallback if the expression/type cannot be resolved
 * from the primary source.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class FallbackConfigurationSource implements ConfigurationSource {

    /**
     * Should be returned by {@link #getSecondarySource()} if there is no default available.
     */
    public static final ConfigurationSource NONE = new ConfigurationSource() {
        public <T> List<T> retrieveList(Class<T> valueType) {
            return null;
        }

        public <T> List<T> retrieveList(String expression, Class<T> valueType) {
            return null;
        }

        public <T> T retrieve(Class<T> valueType) {
            return null;
        }

        public <T> T retrieve(String expression, Class<T> valueType) {
            return null;
        }

        public boolean isAvailable(Class<?> valueType) {
            return false;
        }

        public boolean isAvailable(String expression) {
            return false;
        }
    };

    /**
     * The main source of 'fresh' configuration
     */
    private final ConfigurationSource primarySource;

    /**
     * The source secondary configuration to fall back to if there is none in the primary.
     */
    private final ConfigurationSource secondarySource;


    public FallbackConfigurationSource(ConfigurationSource primarySource, ConfigurationSource secondarySource) {
        this.primarySource = (primarySource != null ? primarySource : NONE);
        this.secondarySource = (secondarySource != null ? secondarySource : NONE);
    }

    /**
     * Retrieve the primary source (never null).
     * 
     * @return the active source
     */
    public final ConfigurationSource getPrimarySource() {
        return primarySource;
    }

    /**
     * Retrieve the secondary configuration source. If none was specified it will return {@value #NONE}.
     * 
     * @return the default source or {@value #NONE} if not available.
     */
    public final ConfigurationSource getSecondarySource() {
        return secondarySource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    public boolean isAvailable(Class<?> valueType) {
        return primarySource.isAvailable(valueType) || secondarySource.isAvailable(valueType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    public boolean isAvailable(String expression) {
        return primarySource.isAvailable(expression) || secondarySource.isAvailable(expression);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    public <T> T retrieve(Class<T> valueType) {
        ConfigurationSource activeSource = primarySource;
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieve(valueType);
        }
        return secondarySource.retrieve(valueType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    public <T> T retrieve(String expression, Class<T> valueType) {
        ConfigurationSource activeSource = primarySource;
        if (activeSource.isAvailable(expression)) {
            return activeSource.retrieve(expression, valueType);
        }
        return secondarySource.retrieve(expression, valueType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    public <T> List<T> retrieveList(Class<T> valueType) {
        ConfigurationSource activeSource = primarySource;
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieveList(valueType);
        }
        return secondarySource.retrieveList(valueType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        ConfigurationSource activeSource = primarySource;
        if (activeSource.isAvailable(valueType)) {
            return activeSource.retrieveList(expression, valueType);
        }
        return secondarySource.retrieveList(expression, valueType);
    }
}
