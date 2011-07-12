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

package org.brekka.stillingar.xmlbeans.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Taylor
 */
public class ConversionManager {

    protected static final Collection<Class<? extends TypeConverter<?>>> BUILT_IN = new ArrayList<Class<? extends TypeConverter<?>>>();
    static {
        BUILT_IN.add(BigDecimalConverter.class);
        BUILT_IN.add(BigIntegerConverter.class);
        BUILT_IN.add(BooleanConverter.class);
        BUILT_IN.add(ByteConverter.class);
        BUILT_IN.add(ByteArrayConverter.class);
        BUILT_IN.add(CalendarConverter.class);
        BUILT_IN.add(DateConverter.class);
        BUILT_IN.add(DoubleConverter.class);
        BUILT_IN.add(FloatConverter.class);
        BUILT_IN.add(IntegerConverter.class);
        BUILT_IN.add(LongConverter.class);
        BUILT_IN.add(ShortConverter.class);
        BUILT_IN.add(StringConverter.class);
        BUILT_IN.add(URIConverter.class);
    }
    
    private final Map<Class<?>, TypeConverter<?>> converters;
    
    public ConversionManager() {
        this(prepare(BUILT_IN));
    }
    
    protected ConversionManager(Map<Class<?>, TypeConverter<?>> converters) {
        this.converters = converters;
    }
    
    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverterForTarget(Class<T> targetType) {
        if (targetType == null) {
            return null;
        }
        return (TypeConverter<T>) this.converters.get(targetType);
    }
    
    protected static Map<Class<?>, TypeConverter<?>> prepare(Collection<Class<? extends TypeConverter<?>>> converters) {
        Map<Class<?>, TypeConverter<?>> converterMap = new HashMap<Class<?>, TypeConverter<?>>();
        for (Class<? extends TypeConverter<?>> type : converters) {
            try {
                TypeConverter<?> converter = (TypeConverter<?>) type.newInstance();
                converterMap.put(converter.targetType(), converter);
                Class<?> primitiveType = converter.primitiveType();
                if (primitiveType != null) {
                    converterMap.put(primitiveType, converter);
                }
            } catch (InstantiationException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return converterMap;
    }
}
