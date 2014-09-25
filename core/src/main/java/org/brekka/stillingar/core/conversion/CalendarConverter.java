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

import java.util.Calendar;


/**
 * @author Andrew Taylor
 */
public class CalendarConverter extends AbstractTypeConverter<Calendar> {
    
    /**
     * Adapter that will perform the conversion
     */
    private final TemporalAdapter temporalAdapter;
    
    /**
     * 
     */
    public CalendarConverter() {
        this(new TemporalAdapter());
    }
    
    /**
     * @param temporalAdapter
     */
    public CalendarConverter(TemporalAdapter temporalAdapter) {
        this.temporalAdapter = temporalAdapter;
    }

    @Override
    public final Class<Calendar> targetType() {
        return Calendar.class;
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TypeConverter#convert(java.lang.Object)
     */
    @Override
    public Calendar convert(Object obj) {
        Calendar value;
        if (obj instanceof Calendar) {
            value = (Calendar) obj;
        } else {
            value = temporalAdapter.toCalendar(obj, true, true, targetType());
        }
        return value;
    }
}
