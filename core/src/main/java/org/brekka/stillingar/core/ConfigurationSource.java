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
 * Retrieve configuration values based on type and/or an expression. Supports both single value and list retrieval.
 * 
 * @author Andrew Taylor
 */
public interface ConfigurationSource {

    /**
     * Retrieve the value identified by <code>expression</code> which must be of (or convertible to) type
     * <code>valueType</code>. Null can be returned, but only if the expression explicitly resolves to a null value. If
     * the expression fails to resolve, a {@link ConfigurationException} will be thrown.
     * 
     * @param expression
     *            the expression used to identify the value to return.
     * @param valueType
     *            the expected value type to be returned.
     * @return the value resolved for the given expression which could be null.
     * @throws ConfigurationException
     *             if the expression is invalid; or does not resolve to any value; or the value could not be converted
     *             to the requested type; or if more than one value was resolved for the expression.
     */
    <T> T retrieve(String expression, Class<T> valueType);

    /**
     * Identify a single value with the type <code>valueType</code>. This method should only be used on configuration
     * sources that support complex types where there is the potential for only a single instance of the given type to
     * exist. Should multiple value instances of the given type exist, a {@link ConfigurationException} will be thrown.
     * Likewise if no instance is found, a {@link ConfigurationException} will be thrown.
     * 
     * @param valueType
     *            the type of the value to be returned which must be unique within the configuration represented by this
     *            source.
     * @return the value.
     * @throws ConfigurationException
     *             if multiple instances of <code>valueType</code> exist within the configuration, or no instance was
     *             found.
     */
    <T> T retrieve(Class<T> valueType);

    /**
     * Retrieve the list of values identified by <code>expression</code> where each value must be of (or convertible to)
     * type <code>valueType</code>. If the expression cannot resolve any values, then an empty list will be returned.
     * 
     * @param expression
     *            the expression used to identify the list of values to return.
     * @param valueType
     *            the expected value type for each element of the list to be returned.
     * @return the list of values resolved for the given expression, never null.
     * @throws ConfigurationException
     *             if the expression is invalid or one or more values in the list could not be converted to the
     *             requested type.
     */
    <T> List<T> retrieveList(String expression, Class<T> valueType);

    /**
     * Retrieve all values within this configuration source that are of the type <code>valueType</code>. This method
     * is only useful on configuration sources that support complex types. If no values are found with the given type,
     * then an empty list will be returned. 
     * 
     * @param valueType the type of the values being searched for.
     * @return the list of values that have the given type <code>valueType</code>.
     */
    <T> List<T> retrieveList(Class<T> valueType);
}
