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

import java.util.Calendar;

import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlTime;

/**
 * @author Andrew Taylor
 */
public class CalendarConverter extends AbstractTypeConverter<Calendar> {

    
    public Class<Calendar> targetType() {
        return Calendar.class;
    }
    
    
    public Calendar convert(XmlObject xmlValue) {
        Calendar value;
        if (xmlValue instanceof XmlDateTime) {
            value = ((XmlDateTime) xmlValue).getCalendarValue();
        } else if (xmlValue instanceof XmlDate) {
            value = ((XmlDate) xmlValue).getCalendarValue();
        } else if (xmlValue instanceof XmlTime) {
            value = ((XmlTime) xmlValue).getCalendarValue();
        } else {
            throw noConversionAvailable(xmlValue);
        }
        return value;
    }
}
