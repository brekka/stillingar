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

package org.brekka.stillingar.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.conversion.TemporalAdapter;
import org.brekka.stillingar.core.conversion.TypeConverter;
import org.brekka.stillingar.core.conversion.TypeConverterListBuilder;
import org.brekka.stillingar.core.dom.DOMConfigurationSourceLoader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Loader for {@link JacksonConfigurationSource} instances
 *
 * @author Andrew Taylor
 */
public class JacksonConfigurationSourceLoader implements ConfigurationSourceLoader {

    private final ObjectMapper objectMapper;
    
    private final Class<?> rootNodeClass;
    
    private final ConversionManager conversionManager;
    
    public JacksonConfigurationSourceLoader(ObjectMapper objectMapper, Class<?> rootNodeClass) {
        this(objectMapper, rootNodeClass, null);
    }
    
    /**
     * @param objectMapper
     */
    public JacksonConfigurationSourceLoader(ObjectMapper objectMapper, Class<?> rootNodeClass,
            ConversionManager conversionManager) {
        Objects.requireNonNull(objectMapper, "An object mapper is required");
        this.objectMapper = objectMapper;
        this.rootNodeClass = rootNodeClass;
        this.conversionManager = conversionManager != null ? conversionManager : new ConversionManager(
                prepareConverters());
    }


    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    @Override
    public ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException,
            IOException {
        Objects.requireNonNull(sourceStream, "source stream is null");
        ObjectNode objectNode;
        try {
            // Encoding is ignored, should always be UTF-8
            objectNode = objectMapper.readValue(sourceStream, ObjectNode.class);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException(String.format(
                    "This does not appear to be a valid JSON document"), e);
        }
        JacksonConfigurationSource source = new JacksonConfigurationSource(objectNode, rootNodeClass, conversionManager, objectMapper);
        return source;
    }
    
    
    public static List<TypeConverter<?>> prepareConverters() {
        TemporalAdapter temporalAdapter = new TemporalAdapter();
        return new TypeConverterListBuilder(DOMConfigurationSourceLoader.prepareConverters(temporalAdapter))
            .toList();
    }
}
