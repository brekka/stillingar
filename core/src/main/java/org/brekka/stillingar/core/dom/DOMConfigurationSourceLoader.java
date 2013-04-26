/**
 * 
 */
package org.brekka.stillingar.core.dom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.brekka.stillingar.core.conversion.BigDecimalConverter;
import org.brekka.stillingar.core.conversion.BigIntegerConverter;
import org.brekka.stillingar.core.conversion.BooleanConverter;
import org.brekka.stillingar.core.conversion.ByteConverter;
import org.brekka.stillingar.core.conversion.CalendarConverter;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.conversion.DateConverter;
import org.brekka.stillingar.core.conversion.DoubleConverter;
import org.brekka.stillingar.core.conversion.EnumConverter;
import org.brekka.stillingar.core.conversion.FloatConverter;
import org.brekka.stillingar.core.conversion.IntegerConverter;
import org.brekka.stillingar.core.conversion.LocaleConverter;
import org.brekka.stillingar.core.conversion.LongConverter;
import org.brekka.stillingar.core.conversion.ShortConverter;
import org.brekka.stillingar.core.conversion.StringConverter;
import org.brekka.stillingar.core.conversion.TemporalAdapter;
import org.brekka.stillingar.core.conversion.TypeConverter;
import org.brekka.stillingar.core.conversion.TypeConverterListBuilder;
import org.brekka.stillingar.core.conversion.URIConverter;
import org.brekka.stillingar.core.conversion.UUIDConverter;
import org.brekka.stillingar.core.conversion.xml.DocumentConverter;
import org.brekka.stillingar.core.conversion.xml.ElementConverter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Properties based configuration loader.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DOMConfigurationSourceLoader implements ConfigurationSourceLoader {

    /**
     * The conversion manager
     */
    private final ConversionManager conversionManager;
    
    /**
     * Namespace context to use in XPath operations (can be null).
     */
    private final DefaultNamespaceContext xPathNamespaceContext;
    
    /**
     * 
     */
    public DOMConfigurationSourceLoader() {
        this(new DefaultNamespaceContext());
    }
    
    /**
     * @param xPathNamespaceContext
     */
    public DOMConfigurationSourceLoader(DefaultNamespaceContext xPathNamespaceContext) {
        this(new ConversionManager(prepareConverters(new TemporalAdapter())), xPathNamespaceContext);
    }
    
    /**
     * @param conversionManager
     */
    public DOMConfigurationSourceLoader(ConversionManager conversionManager, DefaultNamespaceContext xPathNamespaceContext) {
        this.conversionManager = conversionManager;
        if (conversionManager == null) {
            throw new IllegalArgumentException("null passed for conversionManager");
        }
        this.xPathNamespaceContext = xPathNamespaceContext;
        if (xPathNamespaceContext == null) {
            throw new IllegalArgumentException("null passed for xPathNamespaceContext");
        }
    }
    

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    public ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException,
            IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        if (xPathNamespaceContext.hasNamespaces()) {
            documentBuilderFactory.setNamespaceAware(true);
        }
        Document document;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(sourceStream);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Parsing DOM XML", e);
        } catch (SAXException e) {
            throw new ConfigurationException("DOM XML", e);
        }
        return new DOMConfigurationSource(document, xPathNamespaceContext, conversionManager);
    }
    
    public static List<TypeConverter<?>> prepareConverters(TemporalAdapter temporalAdapter) {
        return new TypeConverterListBuilder().<TypeConverter<?>> 
            addAll(
                new BigDecimalConverter(), new BigIntegerConverter(), new BooleanConverter(), new ByteConverter(),
                new DoubleConverter(), new FloatConverter(), new IntegerConverter(), new LongConverter(), 
                new ShortConverter(), new StringConverter(), new URIConverter(), new ElementConverter(), 
                new DocumentConverter(), new LocaleConverter(), new UUIDConverter(), new EnumConverter(),
                new ElementConverter(), new DocumentConverter(), 
                new CalendarConverter(temporalAdapter), new DateConverter(temporalAdapter))
            .inPackage("org.brekka.stillingar.core.conversion")
                .addOptionalClass("DateTimeConverter", temporalAdapter)
                .addOptionalClass("LocalTimeConverter", temporalAdapter)
                .addOptionalClass("LocalDateConverter", temporalAdapter)
                .addOptionalClass("PeriodConverter")
            .done()
            .toList();
    }
}
