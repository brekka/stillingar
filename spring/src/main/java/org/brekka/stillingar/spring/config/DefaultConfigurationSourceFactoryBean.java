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

package org.brekka.stillingar.spring.config;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * Loads a default configuration file from the classpath using {@link ConfigurationSourceLoader}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class DefaultConfigurationSourceFactoryBean implements FactoryBean<ConfigurationSource> {

    /**
     * The resource to load the default configuration from
     */
    private final Resource resource;
    
    /**
     * Will load a {@link ConfigurationSource} based on the contents of the resource.
     */
    private final ConfigurationSourceLoader loader;
    
    /**
     * The encoding of the resource if it needs to be explicitly stated.
     */
    private final Charset encoding;
    
    /**
     * @param resource The resource to load the default configuration from
     * @param loader Will load a {@link ConfigurationSource} based on the contents of the resource.
     * @param encoding The encoding of the resource if it needs to be explicitly stated (can be null).
     */
    public DefaultConfigurationSourceFactoryBean(Resource resource, ConfigurationSourceLoader loader, Charset encoding) {
        this.resource = resource;
        this.loader = loader;
        this.encoding = encoding;
    }
    
    /**
     * @param resource The resource to load the default configuration from
     * @param loader Will load a {@link ConfigurationSource} based on the contents of the resource.
     */
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
            if (is != null) {
                is.close();
            }
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
