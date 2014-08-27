/*
 * Copyright 2014 the original author or authors.
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

package org.brekka.stillingar.spring.resource.dir;

import org.brekka.stillingar.spring.resource.BaseDirectory;
import org.brekka.stillingar.spring.resource.UnresolvableResource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Identify a base directory based on a resource lookup via {@link ApplicationContext#getResource(String)}. This supports
 * the use of Spring property based variable replacement which is more flexible than system properties.
 *
 * @author Ben.Gilbert
 */
public class ResourceDirectory implements BaseDirectory, ApplicationContextAware {

    /**
     * The location to resolve.
     */
    private final String location;
    
    /**
     * Application context, if available
     */
    private ApplicationContext applicationContext;

    /**
     * The location which can be anything handled by {@link ApplicationContext#getResource(String)}.
     * 
     * @param location
     *            The location
     */
    public ResourceDirectory(String location) {
        this.location = location;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        Resource resource = applicationContext.getResource(location);
        if (!resource.exists()) {
            resource = new UnresolvableResource("Resource location '%s' not found", location);
        }
        return resource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return "Resource based directory";
    }

    /**
     * The location from which to resolve the base directory.
     * 
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
