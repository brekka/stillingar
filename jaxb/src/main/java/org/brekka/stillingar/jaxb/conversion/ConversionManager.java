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

package org.brekka.stillingar.jaxb.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Taylor
 */
public class ConversionManager {

    private final Map<Class<?>, TypeConverter<?>> converters;
    
    public ConversionManager() {
        this(Arrays.<TypeConverter<?>>asList(
            new CalendarConverter(),
            new DateConverter(),
            new URIConverter(),
            new DocumentConverter(),
            new UUIDConverter(),
            new LocaleConverter()
        ));
    }
    
    public ConversionManager(Collection<TypeConverter<?>> converters) {
        this.converters = prepare(converters);
    }
    
    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverterForTarget(Class<T> targetType) {
        if (targetType == null) {
            return null;
        }
        return (TypeConverter<T>) this.converters.get(targetType);
    }
    
    public synchronized void addConverter(TypeConverter<?> converter) {
        converters.put(converter.targetType(), converter);
    }
    
    protected static Map<Class<?>, TypeConverter<?>> prepare(Collection<TypeConverter<?>> converters) {
        Map<Class<?>, TypeConverter<?>> converterMap = new HashMap<Class<?>, TypeConverter<?>>();
        for (TypeConverter<?> converter : converters) {
            converterMap.put(converter.targetType(), converter);
            Class<?> primitiveType = converter.primitiveType();
            if (primitiveType != null) {
                converterMap.put(primitiveType, converter);
            }
        }
        return converterMap;
    }
}
