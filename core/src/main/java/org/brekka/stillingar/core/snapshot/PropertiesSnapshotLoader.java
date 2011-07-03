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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.brekka.stillingar.core.ConfigurationException;

/**
 * Configuration snapshot loader based on {@link Properties}. 
 * 
 * @author Andrew Taylor
 */
public class PropertiesSnapshotLoader implements SnapshotLoader {
    
    /**
     * Encoding to be used for the properties file.
     */
    private final Charset encoding;

    /**
     * Use default encoding
     */
    public PropertiesSnapshotLoader() {
        this(null);
    }

    /**
     * @param encoding the encoding of the properties file
     */
    public PropertiesSnapshotLoader(Charset encoding) {
        this.encoding = encoding;
    }


    public Snapshot load(URL toLoad, long timestamp) {
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
        return new PropertiesSnapshot(toLoad, timestamp, props);
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
