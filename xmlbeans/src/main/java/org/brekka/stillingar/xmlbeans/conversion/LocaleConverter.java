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

import java.util.Locale;

import org.apache.xmlbeans.XmlLanguage;
import org.apache.xmlbeans.XmlToken;

/**
 * Extract a {@link Locale} from the XML value. The standard xsd 'language' type will be parsed using {@link Locale#forLanguageTag(String)}
 * while any other token will parsed as standard Locale strings.
 */
public class LocaleConverter extends org.brekka.stillingar.core.conversion.LocaleConverter {

    @Override
    public Locale convert(Object xmlValue) {
        Locale value;
        if (xmlValue instanceof XmlLanguage) {
            value = Locale.forLanguageTag(((XmlLanguage) xmlValue).getStringValue());
        } else if (xmlValue instanceof XmlToken) {
            String locale = ((XmlToken) xmlValue).getStringValue();
            if (locale.contains("_")) {
                // Handle as Java locale language and country
                String[] parts = locale.split("_");
                value = new Locale(parts[0], parts[1]);
            } else {
                // Just a language
                value = new Locale(locale);
            }
        } else {
            value = super.convert(xmlValue);
        }
        return value;
    }
}
