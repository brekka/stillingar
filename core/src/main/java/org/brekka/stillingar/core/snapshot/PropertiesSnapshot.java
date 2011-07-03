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

package org.brekka.stillingar.core.snapshot;

import static java.lang.String.format;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.brekka.stillingar.core.ConfigurationException;

/**
 * A snapshot implementation that is backed by a {@link Properties} instance. The nature of properties is that all of
 * their values are of type {@link String}, thus the type based retrieve methods will be useless and thus have been
 * implementated as throwing {@link UnsupportedOperationException}.
 * 
 * The valueType used in combination with the expression does not have to be just {@link String}, anything registered
 * with the {@link PropertyEditorManager} will be resolvable.
 * 
 * @author Andrew Taylor
 */
public class PropertiesSnapshot implements Snapshot {

    /**
     * The location of the properties
     */
    private final URL location;

    /**
     * The last modified of the resource that was loaded
     */
    private final long timestamp;

    /**
     * The properties from which configuration values will be resolved.
     */
    private final Properties properties;

    /**
     * 
     * @param location
     *            The location of the properties
     * @param timestamp
     *            The last modified of the resource that was loaded
     * @param properties
     *            The properties from which configuration values will be resolved.
     */
    public PropertiesSnapshot(URL location, long timestamp, Properties properties) {
        this.location = location;
        this.timestamp = timestamp;
        this.properties = properties;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public URL getLocation() {
        return location;
    }

    /**
     * NOT supported. Always throws {@link ConfigurationException}.
     */
    public <T> T retrieve(Class<T> valueType) {
        throw new ConfigurationException("A property key must be specified when using Properties");
    }

    /**
     * Retrieve the property value that corresponds to <code>key</code>. The valueType can be any type supported by
     * {@link PropertyEditorManager}.
     * 
     * @param key
     *            the properties key of the value to return.
     * @param valueType
     *            can be any type supported by the {@link PropertyEditorManager}.
     */
    public <T> T retrieve(String key, Class<T> valueType) {
        String value = properties.getProperty(key);
        return resolve(valueType, value, key);
    }

    /**
     * NOT supported. Always throws {@link ConfigurationException}.
     */
    public <T> List<T> retrieveList(Class<T> valueType) {
        throw new ConfigurationException("A property key must be specified when using Properties");
    }

    /**
     * Retrieve the list of values that are defined by the indexed <code>key</code>. The valueType can be any type supported by
     * {@link PropertyEditorManager}.
     * 
     * @param key
     *            the properties key of the value to return.
     * @param valueType
     *            can be any type supported by the {@link PropertyEditorManager}.
     */
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        List<T> valueList = new ArrayList<T>();
        String value = properties.getProperty(expression);
        if (value == null) {
            value = properties.getProperty(expression + ".0");
        }
        int index = 1;
        while (value != null) {
            valueList.add(resolve(valueType, value, expression));
            value = properties.getProperty(expression + "." + (index++));
        }
        return valueList;
    }

    @SuppressWarnings("unchecked")
    protected <T> T resolve(Class<T> valueType, String value, String key) {
        T retVal = null;
        if (valueType == String.class) {
            retVal = (T) value;
        } else if (value != null) {
            Class<?> type = PropertiesSnapshotLoader.primitiveTypeFor(valueType);
            if (type == null) {
                type = valueType;
            }
            PropertyEditor editor = PropertyEditorManager.findEditor(valueType);
            if (editor == null) {
                throw new ConfigurationException(format("Unable to find PropertyEditor "
                        + "to convert value '%s' to requested type '%s' for key '%s'", value, valueType.getName(), key));
            }
            editor.setAsText((String) value);
            retVal = (T) editor.getValue();
        }
        return retVal;
    }
}