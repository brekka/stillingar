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

package org.brekka.stillingar.xmlbeans.conversion;

import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx;

/**
 * EnumConverter
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class EnumConverter extends org.brekka.stillingar.core.conversion.EnumConverter {

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.EnumConverter#convert(java.lang.Object, java.lang.Class)
     */
    @Override
    public Enum<?> convert(Object obj, Class<Enum<?>> enumType) {
        if (obj instanceof JavaStringEnumerationHolderEx) {
            JavaStringEnumerationHolderEx xObj = (JavaStringEnumerationHolderEx) obj;
            StringEnumAbstractBase enumValue = xObj.getEnumValue();
            obj = enumValue.toString();
        } else if (obj instanceof XmlAnySimpleType) {
            String strValue = ((XmlAnySimpleType) obj).getStringValue();
            obj = strValue;
        }
        return super.convert(obj, enumType);
    }
}
