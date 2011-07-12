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

import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlObject;

/**
 * @author Andrew Taylor
 */
public class ByteArrayConverter extends AbstractTypeConverter<byte[]> {

    private static final byte[] EMPTY = new byte[0];
    
    
    @SuppressWarnings("unchecked")
    public Class<byte[]> targetType() {
        return (Class<byte[]>) EMPTY.getClass();
    }
    
    
    public byte[] convert(XmlObject xmlValue) {
        byte[] value;
        if (xmlValue instanceof XmlBase64Binary) {
            value = ((XmlBase64Binary) xmlValue).getByteArrayValue();
        } else {
            throw noConversionAvailable(xmlValue);
        }
        return value;
    }
}
