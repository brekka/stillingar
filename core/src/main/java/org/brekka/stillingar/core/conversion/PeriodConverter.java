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

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;


/**
 * @author Andrew Taylor
 */
public class PeriodConverter extends AbstractTypeConverter<Period> {

    /**
     * Target type
     */
    @Override
    public final Class<Period> targetType() {
        return Period.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.conversion.TypeConverter#convert(java.lang.Object)
     */
    @Override
    public Period convert(Object obj) {
        Period value;
        if (obj instanceof Period) {
            value = (Period) obj;
        } else if (obj instanceof String) {
            String period = (String) obj;
            value = ISOPeriodFormat.standard().parsePeriod(period);
        } else {
            value = super.convert(obj);
        }
        return value;
    }
}
