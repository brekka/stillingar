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

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;

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

    /**
     * @param primarySource
     *            The main source of 'fresh' configuration
     * @param secondarySource
     *            The source secondary configuration to fall back to if there is none in the primary.
     * @throws IllegalArgumentException if both configuration sources are null.           
     */
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
        if (valueType == null) {
            throw new IllegalArgumentException("A value type must be specified");
        }
        return primarySource.isAvailable(valueType) || secondarySource.isAvailable(valueType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    public boolean isAvailable(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("An expression must be specified");
        }
        return primarySource.isAvailable(expression) || secondarySource.isAvailable(expression);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    public <T> T retrieve(Class<T> valueType) {
        if (valueType == null) {
            throw new IllegalArgumentException("A value type must be specified");
        }
        if (primarySource.isAvailable(valueType)) {
            return primarySource.retrieve(valueType);
        }
        if (secondarySource.isAvailable(valueType)) {
            return secondarySource.retrieve(valueType);
        }
        throw new ConfigurationException(String.format(
                "No value found of type '%s' in any of the available configuration sources", valueType.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    public <T> T retrieve(String expression, Class<T> valueType) {
        if (expression == null) {
            throw new IllegalArgumentException("An expression must be specified");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("A value type must be specified");
        }
        if (primarySource.isAvailable(expression)) {
            return primarySource.retrieve(expression, valueType);
        }
        if (secondarySource.isAvailable(expression)) {
            return secondarySource.retrieve(expression, valueType);
        }
        throw new ConfigurationException(String.format("Expression '%s' did not evaluate to a value "
                + "in any of the available configuration sources. Expected return type '%s'.", 
                expression, valueType.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    public <T> List<T> retrieveList(Class<T> valueType) {
        if (valueType == null) {
            throw new IllegalArgumentException("A value type must be specified");
        }
        if (primarySource.isAvailable(valueType)) {
            return primarySource.retrieveList(valueType);
        }
        if (secondarySource.isAvailable(valueType)) {
            return secondarySource.retrieveList(valueType);
        }
        throw new ConfigurationException(String.format(
                "No list value found of type '%s' in any of the available configuration sources", valueType.getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        if (expression == null) {
            throw new IllegalArgumentException("An expression must be specified");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("A value type must be specified");
        }
        if (primarySource.isAvailable(expression)) {
            return primarySource.retrieveList(expression, valueType);
        }
        if (secondarySource.isAvailable(expression)) {
            return secondarySource.retrieveList(expression, valueType);
        }
        throw new ConfigurationException(String.format(
                "Expression '%s' did not evaluate to any values within any of the available " +
                "configuration sources. Expected to return list of type '%s'.", expression, valueType.getName()));
    }
}
