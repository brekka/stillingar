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

import java.io.IOException;

/**
 * @author Andrew Taylor
 */
public class ByteArrayConverter extends AbstractTypeConverter<byte[]> {

    private static final byte[] EMPTY = new byte[0];
    
    /**
     * Cache whether the iHarder base64 library is available.
     */
    private final boolean base64Available;
    
    /**
     * Determines whether JodaTime is available, caching the fact
     */
    public ByteArrayConverter() {
        boolean base64Available = false;
        try {
            Thread.currentThread().getContextClassLoader().loadClass("net.iharder.Base64");
            base64Available = true;
        } catch (ClassNotFoundException e) {
        }
        this.base64Available = base64Available;
    }
    
    
    @SuppressWarnings("unchecked")
    public final Class<byte[]> targetType() {
        return (Class<byte[]>) EMPTY.getClass();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TypeConverter#convert(java.lang.Object)
     */
    @Override
    public byte[] convert(Object obj) {
        byte[] value;
        if (obj instanceof byte[]) {
            value = (byte[]) obj;
        } else if (obj instanceof String) {
            value = parseString((String) obj);
        } else {
            value = super.convert(obj);
        }
        return value;
    }


    /**
     * @param obj
     * @return
     */
    protected byte[] parseString(String valueStr) {
        if (!base64Available) {
            throw new IllegalArgumentException(format(
                    "Base64 conversion unavailable for value of length %d to '%s'." +
                    " Add net.iharder:base64 library to enable default conversion.", 
                    valueStr.length(), targetType().getName()));
        }
        try {
            return net.iharder.Base64.decode(valueStr);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                    "Failed to Base64 decode string of length %d", valueStr.length()), e);
        }
    }
}
