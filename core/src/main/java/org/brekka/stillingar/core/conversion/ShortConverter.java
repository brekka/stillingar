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
 * @author Andrew Taylor
 */
public class ShortConverter extends AbstractTypeConverter<Short> {

    
    public final Class<Short> targetType() {
        return Short.class;
    }
    
    @Override
    public final Class<?> primitiveType() {
        return Short.TYPE;
    }
    
    public Short convert(Object obj) {
        Short value;
        if (obj instanceof Short) {
            value = (Short) obj;
        } else if (obj instanceof String) {
            String strValue = (String) obj;
            value = Short.valueOf(strValue);
        } else if (obj instanceof Number) {
            Number number = (Number) obj;
            value = number.shortValue();
        } else {
            throw noConversionAvailable(obj);
        }
        return value;
    }

 
    
}
