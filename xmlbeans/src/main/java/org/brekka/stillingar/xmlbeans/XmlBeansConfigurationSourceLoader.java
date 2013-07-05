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

package org.brekka.stillingar.xmlbeans;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.brekka.stillingar.core.conversion.CalendarConverter;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.conversion.DateConverter;
import org.brekka.stillingar.core.conversion.TemporalAdapter;
import org.brekka.stillingar.core.conversion.TypeConverter;
import org.brekka.stillingar.core.conversion.TypeConverterListBuilder;
import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
import org.brekka.stillingar.xmlbeans.conversion.BigDecimalConverter;
import org.brekka.stillingar.xmlbeans.conversion.BigIntegerConverter;
import org.brekka.stillingar.xmlbeans.conversion.BooleanConverter;
import org.brekka.stillingar.xmlbeans.conversion.ByteArrayConverter;
import org.brekka.stillingar.xmlbeans.conversion.ByteConverter;
import org.brekka.stillingar.xmlbeans.conversion.DocumentConverter;
import org.brekka.stillingar.xmlbeans.conversion.DoubleConverter;
import org.brekka.stillingar.xmlbeans.conversion.ElementConverter;
import org.brekka.stillingar.xmlbeans.conversion.EnumConverter;
import org.brekka.stillingar.xmlbeans.conversion.FloatConverter;
import org.brekka.stillingar.xmlbeans.conversion.IntegerConverter;
import org.brekka.stillingar.xmlbeans.conversion.LocaleConverter;
import org.brekka.stillingar.xmlbeans.conversion.LongConverter;
import org.brekka.stillingar.xmlbeans.conversion.ShortConverter;
import org.brekka.stillingar.xmlbeans.conversion.StringConverter;
import org.brekka.stillingar.xmlbeans.conversion.URIConverter;
import org.brekka.stillingar.xmlbeans.conversion.UUIDConverter;
import org.brekka.stillingar.xmlbeans.conversion.XmlBeansTemporalAdapter;


/**
 * Loader of Apache XMLBeans based snapshots.
 * 
 * @author Andrew Taylor
 */
public class XmlBeansConfigurationSourceLoader implements ConfigurationSourceLoader {

    private final ConversionManager conversionManager;

    private final DefaultNamespaceContext xpathNamespaces;

    private boolean validate = true;
    
    /**
     * Options passed to the XmlObject.parse(...) operation. Defaults are to strip comments
     * (not used for configuration) but can be overridden via the corresponding setter.
     */
    private XmlOptions loadXmlOptions;

    public XmlBeansConfigurationSourceLoader() {
        this(new ConversionManager(prepareConverters()));
    }
    
    public XmlBeansConfigurationSourceLoader(ConversionManager conversionManager) {
        this(conversionManager, new DefaultNamespaceContext());
    }
    
    public XmlBeansConfigurationSourceLoader(DefaultNamespaceContext xpathNamespaces) {
        this(new ConversionManager(prepareConverters()), xpathNamespaces);
    }
    
    public XmlBeansConfigurationSourceLoader(ConversionManager conversionManager, DefaultNamespaceContext xpathNamespaces) {
        if (conversionManager == null) {
            throw new IllegalArgumentException("null passed for conversion manager");
        }
        this.conversionManager = conversionManager;
        if (xpathNamespaces == null) {
            throw new IllegalArgumentException("null passed for xpathNamespaces");
        }
        this.xpathNamespaces = xpathNamespaces;
        
        XmlOptions loadXmlOptions = new XmlOptions();
        loadXmlOptions.setLoadStripComments();
        this.loadXmlOptions = loadXmlOptions;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    @Override
    public ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws IOException {
        if (sourceStream == null) {
            throw new IllegalArgumentException("A source stream is required");
        }
        try {
            
            XmlObject xmlBean = XmlObject.Factory.parse(sourceStream, loadXmlOptions);
            if (this.validate) {
                validate(xmlBean);
            }
            return new XmlBeansConfigurationSource(xmlBean, this.xpathNamespaces, conversionManager);
        } catch (XmlException e) {
            throw new ConfigurationException(String.format(
                    "This does not appear to be an XML document"), e);
        }
    }

    protected void validate(XmlObject bean) {
        List<XmlError> errors = new ArrayList<XmlError>();
        XmlOptions validateOptions = new XmlOptions();
        validateOptions.setErrorListener(errors);
        if (!bean.validate(validateOptions)) {
            throw new ConfigurationException(String.format(
                    "Configuration XML does not validate. Errors: %s", errors));
        }
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
    
    public static List<TypeConverter<?>> prepareConverters() {
        TemporalAdapter temporalAdapter = new XmlBeansTemporalAdapter();
        return new TypeConverterListBuilder().<TypeConverter<?>> 
            addAll(
                new BigDecimalConverter(), new BigIntegerConverter(), new BooleanConverter(), new ByteConverter(),
                new DoubleConverter(), new FloatConverter(), new IntegerConverter(), new LongConverter(), 
                new ShortConverter(), new StringConverter(), new URIConverter(), new ElementConverter(), 
                new DocumentConverter(), new LocaleConverter(), new UUIDConverter(), new EnumConverter(),
                new ElementConverter(), new DocumentConverter(),  new ByteArrayConverter(),
                new CalendarConverter(temporalAdapter), new DateConverter(temporalAdapter))
            .inPackage("org.brekka.stillingar.core.conversion")
                .addOptionalClass("DateTimeConverter", temporalAdapter)
                .addOptionalClass("LocalTimeConverter", temporalAdapter)
                .addOptionalClass("LocalDateConverter", temporalAdapter)
            .done()
            .addOptionalClass("org.brekka.stillingar.xmlbeans.conversion.PeriodConverter")
            .toList();
        
    }
    
    /**
     * @param loadXmlOptions the loadXmlOptions to set
     */
    public void setLoadXmlOptions(XmlOptions loadXmlOptions) {
        this.loadXmlOptions = loadXmlOptions;
    }
}
