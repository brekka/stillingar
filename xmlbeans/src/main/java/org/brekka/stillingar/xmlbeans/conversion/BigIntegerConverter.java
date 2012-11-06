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

/**
 * @author Andrew Taylor
 */
public class BigIntegerConverter extends org.brekka.stillingar.core.conversion.BigIntegerConverter {

    
    @Override
    public BigInteger convert(Object xmlValue) {
        BigInteger value;
        if (xmlValue instanceof XmlInteger) {
            value = ((XmlInteger) xmlValue).getBigIntegerValue();
        } else {
            value = super.convert(xmlValue);
        }
        return value;
    }
}
