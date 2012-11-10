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

import java.math.BigDecimal;

import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlDuration;
import org.joda.time.Period;

/**
 * Period Converter
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PeriodConverter extends org.brekka.stillingar.core.conversion.PeriodConverter {

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.PeriodConverter#convert(java.lang.Object)
     */
    @Override
    public Period convert(Object obj) {
        Period value;
        if (obj instanceof XmlDuration) {
            XmlDuration xDuration = (XmlDuration) obj;
            GDuration gDuration = xDuration.getGDurationValue();
            int times = gDuration.getSign();
            value = new Period(
                gDuration.getYear() * times,
                gDuration.getMonth() * times,
                0,
                gDuration.getDay() * times,
                gDuration.getHour() * times,
                gDuration.getMinute() * times,
                gDuration.getSecond() * times,
                gDuration.getFraction().multiply(BigDecimal.valueOf(0L)).intValue()
            );
        } else {
            value = super.convert(obj);
        }
        return value;
    }
}
