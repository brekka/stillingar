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

package org.brekka.stillingar.core.conversion.xml;

import org.brekka.stillingar.core.conversion.AbstractTypeConverter;
import org.w3c.dom.Element;

/**
 * @author Andrew Taylor
 */
public class ElementConverter extends AbstractTypeConverter<Element> {

    public final Class<Element> targetType() {
        return Element.class;
    }

    public Element convert(Object obj) {
        Element element;
        if (obj instanceof Element) {
            element = (Element) obj;
        } else {
            element = super.convert(obj);
        }
        return element;
    }
}
