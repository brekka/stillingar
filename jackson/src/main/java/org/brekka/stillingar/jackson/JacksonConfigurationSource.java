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

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ValueConfigurationException;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.support.BeanReflectionHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

/**
 * A configuration source based on the Jackson JSON processor. Supports type based lookup (when available) and 
 * json-path based expressions.
 *
 * @author Andrew Taylor
 */
public class JacksonConfigurationSource implements ConfigurationSource {

    private final ConversionManager conversionManager;
    private final ObjectMapper objectMapper;
    private final ReadContext jsonPathContext;
    private final BeanReflectionHelper reflectionHelper;
    
    /**
     * @param value
     */
    public JacksonConfigurationSource(ObjectNode objectNode, Class<?> rootNodeClass,
            ConversionManager conversionManager, ObjectMapper objectMapper) throws IOException {
        this.conversionManager = conversionManager;
        this.objectMapper = objectMapper;
        Object pathNode = objectMapper.treeToValue(objectNode, Map.class);
        this.jsonPathContext = JsonPath.parse(pathNode);
        
        BeanReflectionHelper helper = null;
        if (rootNodeClass != null) {
            Object bean = objectMapper.treeToValue(objectNode, rootNodeClass);
            helper = new BeanReflectionHelper(bean);
        }
        this.reflectionHelper = helper;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSource#isAvailable(java.lang.String)
     */
    @Override
    public boolean isAvailable(String expression) {
        return jsonPathContext.read(expression) != null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSource#isAvailable(java.lang.Class)
     */
    @Override
    public boolean isAvailable(Class<?> valueType) {
        if (reflectionHelper == null) {
            throw new ValueConfigurationException(
                    "Retrieval by type is not supported when no type information is available.", valueType, null);
        }
        return reflectionHelper.isAvailable(valueType);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T retrieve(String expression, Class<T> valueType) {
        Object retVal;
        Object result = jsonPathContext.read(expression);
        if (result == null) {
            retVal = null;
        } else if (result instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) result;
            if (coll.isEmpty()) {
                retVal = null;
            } else if (coll.size() == 1) {
                retVal = coll.iterator().next();
            } else {
                throw new ValueConfigurationException(format(
                        "Expected 1 value, found %d", coll.size()), valueType, expression);
            }
        } else {
            retVal = result;
        }
        retVal = toObject(retVal, valueType);
        return convert(valueType, retVal, expression);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSource#retrieve(java.lang.Class)
     */
    @Override
    public <T> T retrieve(Class<T> valueType) {
        if (reflectionHelper == null) {
            throw new ValueConfigurationException(
                    "Retrieval by type is not supported when no type information is available.", valueType, null);
        }
        return reflectionHelper.findValueOf(valueType);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        Object result = jsonPathContext.read(expression);
        Collection<?> vals;
        if (result instanceof Collection<?>) {
            vals = (Collection<?>) result;
        } else {
            vals = Arrays.asList(result);
        }
        List<T> results = new ArrayList<T>(vals.size());
        for (Object object : vals) {
            object = toObject(object, valueType);
            T value = convert(valueType, object, expression);
            if (value != null) {
                results.add(value);
            }
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.api.ConfigurationSource#retrieveList(java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(Class<T> valueType) {
        if (reflectionHelper == null) {
            throw new ValueConfigurationException(
                    "List retrieval by type is not supported when no type information is available.", valueType, null);
        }
        return reflectionHelper.findListOf(valueType);
    }
    

    /**
     * @param retVal
     * @return
     */
    protected Object toObject(Object retVal, Class<?> expectedClass) {
        if (expectedClass == byte[].class
                && retVal instanceof String) {
            // Let Jackson convert the string to a byte array
        } else if (retVal instanceof Map == false) {
            // Not an object
            return retVal;
        }
        JsonNode tree = objectMapper.valueToTree(retVal);
        try {
            return objectMapper.treeToValue(tree, expectedClass);
        } catch (JsonProcessingException e) {
            throw new ValueConfigurationException(
                    "Failed to convert JSON model into expected type.", expectedClass, null);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T convert(Class<T> expectedType, Object object, String expression) {
        T value;
        if (object == null) {
            // Leave as null
            value = null;
        } else if (expectedType.isAssignableFrom(object.getClass())) {
            value = (T) object;
        } else if (conversionManager.hasConverter(expectedType)) {
            try {
                value = conversionManager.convert(object, expectedType);
            } catch (IllegalArgumentException e) {
                throw new ValueConfigurationException(format(
                        "Conversion failure"), expectedType, expression, e);
            }
        } else {
            throw new ValueConfigurationException(format(
                    "No conversion available from type '%s'", object.getClass()
                    .getName()), expectedType, expression);
        }
        return value;
    }
}
