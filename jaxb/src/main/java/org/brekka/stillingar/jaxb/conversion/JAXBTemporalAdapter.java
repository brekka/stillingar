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

package org.brekka.stillingar.jaxb.conversion;

import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.brekka.stillingar.core.conversion.TemporalAdapter;

/**
 * TODO Description of JAXBTemporalAdapter
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBTemporalAdapter extends TemporalAdapter {

    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TemporalAdapter#toCalendar(java.lang.Object, boolean, boolean, java.lang.Class)
     */
    @Override
    public Calendar toCalendar(Object obj, boolean supportsDate, boolean supportsTime, Class<?> expectedType) {
        Calendar value;
        if (obj instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar xCal = (XMLGregorianCalendar) obj;
            value = xCal.toGregorianCalendar();
        } else {
            value = super.toCalendar(obj, supportsDate, supportsTime, expectedType);
        }
        return value;
    }
}
