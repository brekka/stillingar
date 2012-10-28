/**
 * 
 */
package org.brekka.stillingar.core.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;

/**
 * Properties based configuration loader.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PropertiesConfigurationSourceLoader implements ConfigurationSourceLoader {

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    public ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException,
            IOException {
        Properties props = new Properties();
        if (encoding != null) {
            props.load(new InputStreamReader(sourceStream, encoding));
        } else {
            props.load(sourceStream);
        }
        return new PropertiesConfigurationSource(props);
    }
}
