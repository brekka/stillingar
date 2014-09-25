/**
 * 
 */
package org.brekka.stillingar.core.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.brekka.stillingar.core.conversion.BigDecimalConverter;
import org.brekka.stillingar.core.conversion.BigIntegerConverter;
import org.brekka.stillingar.core.conversion.BooleanConverter;
import org.brekka.stillingar.core.conversion.ByteConverter;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.conversion.DoubleConverter;
import org.brekka.stillingar.core.conversion.EnumConverter;
import org.brekka.stillingar.core.conversion.FloatConverter;
import org.brekka.stillingar.core.conversion.IntegerConverter;
import org.brekka.stillingar.core.conversion.LocaleConverter;
import org.brekka.stillingar.core.conversion.LongConverter;
import org.brekka.stillingar.core.conversion.ShortConverter;
import org.brekka.stillingar.core.conversion.StringConverter;
import org.brekka.stillingar.core.conversion.TypeConverter;
import org.brekka.stillingar.core.conversion.URIConverter;
import org.brekka.stillingar.core.conversion.UUIDConverter;
import org.brekka.stillingar.core.conversion.xml.DocumentConverter;
import org.brekka.stillingar.core.conversion.xml.ElementConverter;

/**
 * Properties based configuration loader.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PropertiesConfigurationSourceLoader implements ConfigurationSourceLoader {

    static final List<TypeConverter<?>> CONVERTERS = Arrays.<TypeConverter<?>> asList(
            new BigDecimalConverter(), new BigIntegerConverter(), new BooleanConverter(), new ByteConverter(),
            new DoubleConverter(), new FloatConverter(), new IntegerConverter(), new LongConverter(), 
            new ShortConverter(), new StringConverter(), new URIConverter(), new ElementConverter(), 
            new DocumentConverter(), new LocaleConverter(), new UUIDConverter(), new EnumConverter());
    
    /**
     * The conversion manager
     */
    private final ConversionManager conversionManager;
    
    
    /**
     * 
     */
    public PropertiesConfigurationSourceLoader() {
        this(new ConversionManager(CONVERTERS));
    }
    
    /**
     * @param conversionManager
     */
    public PropertiesConfigurationSourceLoader(ConversionManager conversionManager) {
        this.conversionManager = conversionManager;
    }
    

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    @Override
    public ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException,
            IOException {
        Properties props = new Properties();
        if (encoding != null) {
            props.load(new InputStreamReader(sourceStream, encoding));
        } else {
            props.load(sourceStream);
        }
        return new PropertiesConfigurationSource(props, conversionManager);
    }
}
