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


import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Andrew Taylor
 */
public class ElementConverter extends org.brekka.stillingar.core.conversion.xml.ElementConverter {
    
    @Override
    public Element convert(Object obj) {
        Element value;
        if (obj instanceof XmlObject) {
            XmlObject xmlObject = (XmlObject) obj;
            value = xmlObjectToElement(xmlObject);
        } else {
            value = super.convert(obj);
        }
        return value;
    }
    
    protected Element xmlObjectToElement(XmlObject xmlValue) {
        Node domNode = xmlValue.getDomNode();
        Element value = null;
        if (domNode instanceof Element) {
            value = ((Element) domNode);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Unable to extract Element from XmlObject of type '%s'", xmlValue.schemaType()));
        }
        return value;
    }
}
