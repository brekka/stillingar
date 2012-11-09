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

import java.math.BigInteger;

/**
 * @author Andrew Taylor
 */
public class BigIntegerConverter extends AbstractTypeConverter<BigInteger> {

    
    public final Class<BigInteger> targetType() {
        return BigInteger.class;
    }
    
    
    public BigInteger convert(Object obj) {
        BigInteger value;
        if (obj instanceof BigInteger) {
            value = (BigInteger) obj;
        } else if (obj instanceof String) {
            String strValue = (String) obj;
            value = new BigInteger(strValue);
        } else if (obj instanceof Number) {
            Number number = (Number) obj;
            value = BigInteger.valueOf(number.longValue());
        } else {
            value = super.convert(obj);
        }
        return value;
    }
}
