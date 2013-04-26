/*
 * Copyright 2012 the original author or authors.
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

package org.brekka.stillingar.jaxb;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.conversion.TemporalAdapter;
import org.brekka.stillingar.core.conversion.TypeConverter;
import org.brekka.stillingar.core.conversion.TypeConverterListBuilder;
import org.brekka.stillingar.core.dom.DOMConfigurationSourceLoader;
import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
import org.brekka.stillingar.jaxb.conversion.JAXBTemporalAdapter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Snapshot loader of XML files using JAXB
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBConfigurationSourceLoader implements ConfigurationSourceLoader {
    
    /**
     * The context path in which to look for the JAXB beans.
     */
    private final String contextPath;
    
    /**
     * Schemas to use to validate.
     */
    private final Schema schema;
    
    /**
     * Context used by the XPath logic to resolve namespace prefixes
     */
    private final DefaultNamespaceContext xPathNamespaceContext;
    
    /**
     * Conversion manager
     */
    private final ConversionManager conversionManager;
    
    public JAXBConfigurationSourceLoader(String contextPath, List<URL> schemas) {
        this(contextPath, schemas, new DefaultNamespaceContext());
    }
    
    public JAXBConfigurationSourceLoader(String contextPath, List<URL> schemas, DefaultNamespaceContext namespaceContext) {
        this(contextPath, schemas, namespaceContext, new ConversionManager(prepareConverters()));
    }
    
    /**
     * @param contextPath
     */
    public JAXBConfigurationSourceLoader(String contextPath, List<URL> schemas, DefaultNamespaceContext namespaceContext, ConversionManager conversionManager) {
        this.contextPath = contextPath;
        this.xPathNamespaceContext = namespaceContext;
        if (namespaceContext == null) {
            throw new IllegalArgumentException("null passed for namespaceContext");
        }
        if (schemas.isEmpty()) {
            this.schema = null;
        } else {
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            List<Source> sources = new ArrayList<Source>();
            List<InputStream> toClose = new ArrayList<InputStream>();
            try {
                for (URL url : schemas) {
                    InputStream is = url.openStream();
                    toClose.add(is);
                    Source source = new StreamSource(is);
                    sources.add(source);
                }
                this.schema = sf.newSchema(sources.toArray(new Source[schemas.size()]));
            } catch (SAXException e) {
                throw new ConfigurationException(String.format(
                        "Failed to generate schemas for path '%s' from %s", contextPath, schemas), e);
            } catch (IOException e) {
                throw new ConfigurationException(String.format(
                        "Failed to generate schemas for path '%s' from %s", contextPath, schemas), e);
            } finally {
                for (InputStream closeMe : toClose) {
                    closeQuietly(closeMe);
                }
            }
        }
        this.conversionManager = conversionManager;
        if (conversionManager == null) {
            throw new IllegalArgumentException("null passed for conversionManager");
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    @Override
    public synchronized ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException,
            IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(xPathNamespaceContext != null);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(sourceStream);
            JAXBContext jc = JAXBContext.newInstance(this.contextPath);
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema( this.schema );
            Object object = u.unmarshal(document);
            return new JAXBConfigurationSource(document, object, xPathNamespaceContext, conversionManager);
        } catch (JAXBException e) {
            throw new ConfigurationException(String.format(
                    "Failed to establish new JAXB context for path '%s'", this.contextPath), e);
        } catch (SAXException e) {
            throw new ConfigurationException(String.format(
                    "Failed to obtain XML from resource"), e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(String.format(
                    "Failed to obtain XML parser"), e);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) { }
    }
    
    public static List<TypeConverter<?>> prepareConverters() {
        TemporalAdapter temporalAdapter = new JAXBTemporalAdapter();
        return new TypeConverterListBuilder(DOMConfigurationSourceLoader.prepareConverters(temporalAdapter))
            .inPackage("org.brekka.stillingar.core.conversion")
                .addOptionalClass("PeriodConverter")
            .done()
            .toList();
    }
}
