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
import java.util.Date;

/**
 * @author Andrew Taylor
 */
public class DateConverter extends AbstractTypeConverter<Date> {
    
    /**
     * Adapter that will perform the conversion
     */
    private final TemporalAdapter temporalAdapter;
    
    /**
     * 
     */
    public DateConverter() {
        this(new TemporalAdapter());
    }
    
    /**
     * @param temporalAdapter
     */
    public DateConverter(TemporalAdapter temporalAdapter) {
        this.temporalAdapter = temporalAdapter;
    }

    public final Class<Date> targetType() {
        return Date.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TypeConverter#convert(java.lang.Object)
     */
    @Override
    public Date convert(Object obj) {
        Date value;
        if (obj instanceof Date) {
            value = (Date) obj;
        } else {
            Calendar cal = temporalAdapter.toCalendar(obj, true, true, targetType());
            value = cal.getTime();
        }
        return value;
    }
}
