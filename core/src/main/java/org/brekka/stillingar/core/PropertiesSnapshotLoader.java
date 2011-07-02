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

package org.brekka.stillingar.core;

import static java.lang.String.format;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration snapshot loader based on {@link Properties}. 
 * 
 * @author Andrew Taylor
 */
public class PropertiesSnapshotLoader implements ConfigurationSnapshotLoader {
    
    private final Charset encoding;

    /**
     * User default encoding
     */
    public PropertiesSnapshotLoader() {
        this(null);
    }

    public PropertiesSnapshotLoader(Charset encoding) {
        this.encoding = encoding;
    }


    public ConfigurationSnapshot load(URL toLoad, long timestamp) {
        Properties props = new Properties();

        InputStream is = null;
        try {
            is = toLoad.openStream();
            if (encoding != null) {
                props.load(new InputStreamReader(is, encoding));
            } else {
                props.load(is);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load properties", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return new PropertiesConfigurationSnapshot(toLoad, timestamp, props);
    }

    
    private class PropertiesConfigurationSnapshot implements ConfigurationSnapshot {

        private final URL location;
        
        private final long timestamp;
        
        private final Properties properties;
        
        
        
        public PropertiesConfigurationSnapshot(URL location, long timestamp, Properties properties) {
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

        public <T> T retrieve(Class<T> valueType) {
            throw new ConfigurationException("A property key must be specified when using Properties");
        }

        public <T> T retrieve(Class<T> valueType, String expression) {
            String value = properties.getProperty(expression);
            return resolve(valueType, value, expression);
        }

        public <T> List<T> retrieveList(Class<T> valueType) {
            throw new ConfigurationException("A property key must be specified when using Properties");
        }

        /**
         * Compose a list based on index numbering
         */
        public <T> List<T> retrieveList(Class<T> valueType, String expression) {
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
                Class<?> type = primitiveTypeFor(valueType);
                if (type == null) {
                    type = valueType;
                }
                PropertyEditor editor = PropertyEditorManager.findEditor(valueType);
                if (editor == null) {
                    throw new ConfigurationException(format("Unable to find PropertyEditor " +
                    		"to convert value '%s' to requested type '%s' for key '%s'", value, valueType.getName(), key));
                }
                editor.setAsText((String) value);
                retVal = (T) editor.getValue();
            }
            return retVal;
        }
    }
    
    
    
    /**
     * Copied from java.beans.ReflectionUtils since it is not public
     * @param wrapper
     * @return
     */
    public static Class<?> primitiveTypeFor(Class<?> wrapper) {
        if (wrapper == Boolean.class) return Boolean.TYPE;
        if (wrapper == Byte.class) return Byte.TYPE;
        if (wrapper == Character.class) return Character.TYPE;
        if (wrapper == Short.class) return Short.TYPE;
        if (wrapper == Integer.class) return Integer.TYPE;
        if (wrapper == Long.class) return Long.TYPE;
        if (wrapper == Float.class) return Float.TYPE;
        if (wrapper == Double.class) return Double.TYPE;
        if (wrapper == Void.class) return Void.TYPE;
        return null;
    }
}
