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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.ConfigurationSourceLoader;
import org.xml.sax.SAXException;

/**
 * Snapshot loader of XML files using JAXB
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBSnapshotLoader implements ConfigurationSourceLoader {

    /**
     * The context path in which to look for the JAXB beans.
     */
    private final String contextPath;
    
    /**
     * Schemas to use to validate.
     */
    private final Schema schema;
    
    /**
     * @param contextPath
     */
    public JAXBSnapshotLoader(String contextPath, List<URL> schemas) {
        this.contextPath = contextPath;
        if (schemas.isEmpty()) {
            this.schema = null;
        } else {
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            List<Source> sources = new ArrayList<Source>();
            for (URL url : schemas) {
                // TODO open and close streams
//                Source source = new StreamSource();
            }
            try {
                this.schema = sf.newSchema(sources.toArray(new Source[schemas.size()]));
            } catch (SAXException e) {
                throw new ConfigurationException(String.format(
                        "Failed to generate schemas for path '%s' from %s", contextPath, schemas), e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSourceLoader#parse(java.io.InputStream, java.nio.charset.Charset)
     */
    @Override
    public synchronized ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException,
            IOException {
        try {
            
            JAXBContext jc = JAXBContext.newInstance(this.contextPath);
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema( this.schema );
            Object jaxbObject = u.unmarshal(sourceStream);
            return new JAXBConfigurationSource(jaxbObject);
            
        } catch (JAXBException e) {
            throw new ConfigurationException(String.format(
                    "Failed to establish new JAXB context for path '%s'", this.contextPath), e);
        }
    }

}
