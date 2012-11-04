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

package org.brekka.stillingar.jaxb.conversion;


import static java.lang.String.format;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Andrew Taylor
 */
public class URIConverter extends AbstractTypeConverter<URI> {

    
    public Class<URI> targetType() {
        return URI.class;
    }
    
    public URI convert(Object value) {
        URI uri;
        if (value instanceof String) {
            String uriStr = (String) value;
            try {
                uri = new URI(uriStr);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(format("Failed to parse URI '%s'",
                        uriStr), e);
            }
        } else {
            throw noConversionAvailable(value);
        }
        return uri;
    }
    
}
