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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Documentation
 * 
 * @author Andrew Taylor
 */
public class ConversionManager {

    private final Map<Class<?>, TypeConverter<?>> converters;
    
    public ConversionManager(Collection<TypeConverter<?>> converters) {
        this.converters = prepare(converters);
    }
    
    @SuppressWarnings("unchecked")
    public <To> TypeConverter<To> getConverterForTarget(Class<To> targetType) {
        if (targetType == null) {
            return null;
        }
        return (TypeConverter<To>) this.converters.get(targetType);
    }
    
    public boolean hasConverter(Class<?> targetType) {
        if (targetType.isEnum()) {
            return getConverterForTarget(Enum.class) != null;
        }
        return getConverterForTarget(targetType) != null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T convert(Object value, Class<T> valueType) {
        if (value == null) {
            return null;
        } else if (valueType.isAssignableFrom(value.getClass())) {
            return valueType.cast(value);
        }
        if (valueType.isEnum()) {
            Object converterForTarget = getConverterForTarget(Enum.class);
            if (converterForTarget instanceof EnumConverter) {
                EnumConverter converter = (EnumConverter) converterForTarget;
                return (T) converter.convert(value, (Class<Enum<?>>) valueType);
            }
        }
        TypeConverter<T> converterForTarget = getConverterForTarget(valueType);
        if (converterForTarget == null) {
            throw new IllegalArgumentException(format("Unable to find converter"
                    + " to convert value '%s' to requested type '%s'.", value, valueType.getName()));
        }
        return converterForTarget.convert(value);
    }
    
    public synchronized void addConverter(TypeConverter<?> converter) {
        Class<?> targetType = converter.targetType();
        if (targetType != null) {
            // Target type can be null if the converter is not available due to missing libraries
            converters.put(targetType, converter);
        }
    }
    
    protected static Map<Class<?>, TypeConverter<?>> prepare(Collection<TypeConverter<?>> converters) {
        Map<Class<?>, TypeConverter<?>> converterMap = new HashMap<Class<?>, TypeConverter<?>>();
        for (TypeConverter<?> converter : converters) {
            Class<?> targetType = converter.targetType();
            if (targetType == null) {
                // Target type can be null if the converter is not available due to missing libraries
                continue;
            }
            converterMap.put(targetType, converter);
            Class<?> primitiveType = converter.primitiveType();
            if (primitiveType != null) {
                converterMap.put(primitiveType, converter);
            }
        }
        return converterMap;
    }
}
