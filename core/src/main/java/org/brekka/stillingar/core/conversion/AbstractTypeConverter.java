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

import static java.lang.String.format;

/**
 * Support class for type converters.
 * 
 * @author Andrew Taylor
 */
public abstract class AbstractTypeConverter<To> implements TypeConverter<To> {

    /**
     * Default, return null
     */
    @Override
    public Class<?> primitiveType() {
        return null;
    }

    /**
     * Simply throws an {@link IllegalArgumentException} with details of the incoming object type and the expected
     * conversion type.
     * 
     * @param value
     *            the value for which no conversion could be performed for the given type.
     * @throws IllegalAccessException indicating the types involved.
     */
    @Override
    public To convert(Object value) {
        throw new IllegalArgumentException(format("No conversion possible from '%s' to '%s'", value.getClass()
                .getName(), targetType().getName()));
    }
}
