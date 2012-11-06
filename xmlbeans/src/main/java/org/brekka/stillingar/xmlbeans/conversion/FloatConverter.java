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

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlFloat;

/**
 * @author Andrew Taylor
 */
public class FloatConverter extends org.brekka.stillingar.core.conversion.FloatConverter {

    @Override
    public Float convert(Object xmlValue) {
        Float value;
        if (xmlValue instanceof XmlFloat) {
            value = Float.valueOf(((XmlFloat) xmlValue).getFloatValue());
        } else if (xmlValue instanceof XmlDecimal) {
            XmlDecimal decimal = (XmlDecimal) xmlValue;
            BigDecimal bigDecimalValue = decimal.getBigDecimalValue();
            value = Float.valueOf(bigDecimalValue.floatValue());
        } else {
            value = super.convert(xmlValue);
        }
        return value;
    }

 
    
}
