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

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;


/**
 * @author Andrew Taylor
 */
public class DateTimeConverter extends AbstractTypeConverter<DateTime> {

    /**
     * Adapter that will perform the conversion
     */
    private final TemporalAdapter temporalAdapter;
    
    
    /**
     * 
     */
    public DateTimeConverter() {
        this(new TemporalAdapter());
    }
    
    /**
     * @param temporalAdapter
     */
    public DateTimeConverter(TemporalAdapter temporalAdapter) {
        this.temporalAdapter = temporalAdapter;
    }
    
    /**
     * Target type
     */
    public final Class<DateTime> targetType() {
        return DateTime.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TypeConverter#convert(java.lang.Object)
     */
    @Override
    public DateTime convert(Object obj) {
        DateTime value;
        if (obj instanceof DateTime) {
            value = (DateTime) obj;
        } else {
            Calendar cal = temporalAdapter.toCalendar(obj, true, true, targetType());
            value = new DateTime(cal, ISOChronology.getInstance());
        }
        return value;
    }
}
