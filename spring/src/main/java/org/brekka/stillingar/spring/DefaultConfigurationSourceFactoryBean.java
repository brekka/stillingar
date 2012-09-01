/**
 * 
 */
package org.brekka.stillingar.spring;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.ConfigurationSourceLoader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class DefaultConfigurationSourceFactoryBean implements FactoryBean<ConfigurationSource> {

    private final Resource resource;
    
    private final ConfigurationSourceLoader loader;
    
    private final Charset encoding;
    
    /**
     * @param resource
     * @param loader
     * @param encoding
     */
    public DefaultConfigurationSourceFactoryBean(Resource resource, ConfigurationSourceLoader loader, Charset encoding) {
        this.resource = resource;
        this.loader = loader;
        this.encoding = encoding;
    }
    
    public DefaultConfigurationSourceFactoryBean(Resource resource, ConfigurationSourceLoader loader) {
        this(resource, loader, null);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public ConfigurationSource getObject() throws Exception {
        InputStream is = null;
        try {
            is = resource.getInputStream();
            return loader.parse(is, encoding);
        } finally {
            is.close();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return ConfigurationSource.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

}
