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

import java.math.BigInteger;

import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;

/**
 * @author Andrew Taylor
 */
public class LongConverter extends AbstractTypeConverter<Long> {

    
    public Class<Long> targetType() {
        return Long.class;
    }
    
    @Override
    public Class<?> primitiveType() {
        return Long.TYPE;
    }
    
    public Long convert(XmlObject xmlValue) {
        Long value;
        if (xmlValue instanceof XmlLong) {
            value = Long.valueOf(((XmlLong) xmlValue).getLongValue());
        } else if (xmlValue instanceof XmlInteger) {
            XmlInteger integer = (XmlInteger) xmlValue;
            BigInteger bigIntegerValue = integer.getBigIntegerValue();
            value = Long.valueOf(bigIntegerValue.longValue());
        } else {
            throw noConversionAvailable(xmlValue);
        }
        return value;
    }

 
    
}
