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

package org.brekka.stillingar.jaxb.conversion;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Andrew Taylor
 */
public class DateConverter extends AbstractTypeConverter<Date> {

    
    public Class<Date> targetType() {
        return Date.class;
    }
    
    
    public Date convert(Object value) {
        Date date;
        if (value instanceof Date) {
            date = (Date) value;
        } else if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar xCal = (XMLGregorianCalendar) value;
            date = xCal.toGregorianCalendar().getTime();
        } else {
            throw noConversionAvailable(value);
        }
        return date;
    }
}
