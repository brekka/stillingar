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

package org.brekka.stillingar.core.conversion;


/**
 * EnumConverter
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class EnumConverter extends AbstractTypeConverter<Enum<?>> {

    /**
     * The enum type
     */
    private final Class<Enum<?>> targetType;
    
    /**
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EnumConverter() {
        this((Class) Enum.class);
    }

    /**
     * @param targetType
     */
    public EnumConverter(Class<Enum<?>> targetType) {
        this.targetType = targetType;
    }

    @Override
    public final Class<Enum<?>> targetType() {
        return targetType;
    }
    
    @Override
    public Enum<?> convert(Object obj) {
        return convert(obj, targetType);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Enum<?> convert(Object obj, Class<Enum<?>> enumType) {
        Enum<?> value;
        if (obj instanceof Enum<?>) {
            value = (Enum<?>) obj;
        } else if (obj instanceof String) {
            String strValue = (String) obj;
            value = Enum.valueOf((Class) enumType, strValue);
        } else {
            value = super.convert(obj);
        }
        return value;
    }
}
