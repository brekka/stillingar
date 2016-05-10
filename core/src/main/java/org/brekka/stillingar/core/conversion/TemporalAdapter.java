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
 * TODO Description of TemporalAdapter
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TemporalAdapter {

    /**
     * Cache whether JodaTime is available.
     */
    protected final boolean jodaTimeAvailable;

    /**
     * Determines whether JodaTime is available, caching the fact
     */
    public TemporalAdapter() {
        boolean jodaTimeAvailableLocal = false;
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.joda.time.format.ISODateTimeFormat");
            jodaTimeAvailableLocal = true;
        } catch (ClassNotFoundException e) {
        }
        this.jodaTimeAvailable = jodaTimeAvailableLocal;
    }


    public Calendar toCalendar(final Object obj, final boolean supportsDate, final boolean supportsTime, final Class<?> expectedType) {
        Calendar value;
        if (obj instanceof Calendar) {
            value = (Calendar) obj;
        } else if (obj instanceof Date) {
            value = Calendar.getInstance();
            value.setTime((Date) obj);
        } else if (obj instanceof String) {
            String dateTimeStr = (String) obj;
            if (!jodaTimeAvailable) {
                throw new IllegalArgumentException(format(
                        "Date/time conversion unavailable for value '%s' to '%s'." +
                                " Add JodaTime library to enable default ISO conversion.",
                                dateTimeStr, expectedType.getName()));
            }
            org.joda.time.format.DateTimeFormatter dateTimeParser;
            if (dateTimeStr.charAt(2) == ':') {
                dateTimeParser = org.joda.time.format.ISODateTimeFormat.timeParser();
            } else if (dateTimeStr.length() > 10 && dateTimeStr.charAt(10) != 'T') {
                dateTimeStr = dateTimeStr.substring(0, 10) + "T" + dateTimeStr.substring(10);
                dateTimeParser = org.joda.time.format.ISODateTimeFormat.dateParser();
            } else {
                dateTimeParser = org.joda.time.format.ISODateTimeFormat.dateTimeParser();
            }
            org.joda.time.DateTime dateTime = dateTimeParser.parseDateTime(dateTimeStr);
            value = dateTime.toCalendar(null);
            if (!supportsDate) {
                value.clear(Calendar.YEAR);
                value.clear(Calendar.MONTH);
                value.clear(Calendar.DAY_OF_MONTH);
            }
            if (!supportsTime) {
                value.clear(Calendar.HOUR_OF_DAY);
                value.clear(Calendar.MINUTE);
                value.clear(Calendar.SECOND);
                value.clear(Calendar.MILLISECOND);
            }
        } else {
            throw new IllegalArgumentException(format(
                    "No temporal conversion available for value of type '%s' to '%s'.",
                    obj.getClass().getName(), expectedType.getName()));
        }
        return value;
    }
}
