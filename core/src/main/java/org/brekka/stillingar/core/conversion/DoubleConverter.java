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
public class DoubleConverter extends AbstractTypeConverter<Double> {

    @Override
    public final Class<Double> targetType() {
        return Double.class;
    }
    
    @Override
    public final Class<?> primitiveType() {
        return Double.TYPE;
    }
    
    @Override
    public Double convert(Object obj) {
        Double value;
        if (obj instanceof Double) {
            value = (Double) obj;
        } else if (obj instanceof String) {
            String strValue = (String) obj;
            value = Double.valueOf(strValue);
        } else if (obj instanceof Number) {
            Number number = (Number) obj;
            value = number.doubleValue();
        } else {
            value = super.convert(obj);
        }
        return value;
    }
}
