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

package org.brekka.stillingar.core.conversion;

import static java.lang.String.format;

import java.util.Calendar;
import java.util.Date;

/**
 * Support class for temporal types, using the JodaTime library if it is available.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractTemporalConverter<T> extends AbstractTypeConverter<T> {
    
    /**
     * Cache whether JodaTime is available.
     */
    private final boolean jodaTimeAvailable;
    
    /**
     * Determines whether JodaTime is available, caching the fact
     */
    public AbstractTemporalConverter() {
        boolean jodaTimeAvailable = false;
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.joda.time.format.ISODateTimeFormat");
            jodaTimeAvailable = true;
        } catch (ClassNotFoundException e) {
        }
        this.jodaTimeAvailable = jodaTimeAvailable;
    }
    
    /**
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected T parseString(String dateTimeStr) {
        if (!jodaTimeAvailable) {
            throw new IllegalArgumentException(format(
                    "Date/time conversion unavailable for value '%s' to '%s'." +
                    " Add JodaTime library to enable default ISO conversion.", 
                    dateTimeStr, targetType().getName()));
        }
        if (dateTimeStr.length() < 14
                && dateTimeStr.endsWith("Z")) {
            dateTimeStr = dateTimeStr.substring(0, dateTimeStr.length() - 1);
        }
        org.joda.time.format.DateTimeFormatter dateTimeParser;
        if (dateTimeStr.charAt(2) == ':') {
            dateTimeParser = org.joda.time.format.ISODateTimeFormat.timeParser();
        } else {
            dateTimeParser = org.joda.time.format.ISODateTimeFormat.dateTimeParser();
        }
        org.joda.time.DateTime dateTime = dateTimeParser.parseDateTime(dateTimeStr);
        Class<T> targetType = targetType();
        T value;
        if (Calendar.class == targetType) {
            value = (T) dateTime.toCalendar(null);
        } else if (Date.class == targetType) {
            value = (T) dateTime.toDate();
        } else {
            value = super.convert(dateTimeStr);
        }
        return value;
    }
}
