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

package org.brekka.stillingar.core.conversion;

/**
 * Provides type conversion from XMLBeans into the corresponding regular Java types. Converters should never have to
 * deal with a null input value.
 * 
 * @author Andrew Taylor
 */
public interface TypeConverter<To> {

    /**
     * Convert the XML type into the corresponding regular Java type.
     * 
     * @param value
     *            the value to convert
     * @return the converted value, never null.
     * @throws IllegalArgumentException
     *             if no conversion is possible for the specified type.
     */
    To convert(Object value);

    /**
     * The type that will be returned by this converter. Can return null to indicate the converter should be ignored.
     * 
     * @return the target type (can be null).
     */
    Class<To> targetType();

    /**
     * The corresponding primitive type that can also be handled by the converter. Simply return null if there is no
     * primitive type for this converter.
     * 
     * @return the primitive type or null if none is available.
     */
    Class<?> primitiveType();
}
