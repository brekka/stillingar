/*
 * Copyright 2014 the original author or authors.
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

package org.brekka.stillingar.jackson.support;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Generates default object mapper instances for when none is specified
 *
 * @author Andrew Taylor
 */
public class ObjectMapperFactory {
    
    public static final ObjectMapper getInstance() {
        ObjectMapper objectMapper = new ObjectMapper(); 
        registerJodaIfAvailable(objectMapper);
        return objectMapper;
    }

    /**
     * @param objectMapper
     */
    private static void registerJodaIfAvailable(ObjectMapper objectMapper) {
        try {
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.joda.JodaModule());
        } catch (NoClassDefFoundError e) {
            // It or one of its dependencies does not exist
        }
    }
}
