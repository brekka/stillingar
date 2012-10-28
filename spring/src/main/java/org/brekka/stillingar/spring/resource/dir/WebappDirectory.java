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

package org.brekka.stillingar.spring.resource.dir;

import org.brekka.stillingar.spring.resource.BaseDirectory;
import org.brekka.stillingar.spring.resource.UnresolvableResource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * Load resource relative to the webapp WEB-INF directory.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class WebappDirectory implements BaseDirectory, ApplicationContextAware {

    /**
     * The relative path within the WEB-INF path.
     */
    private final String path;
    
    /**
     * Application context, if available
     */
    private ApplicationContext applicationContext;

    /**
     * The WEB-INF directory itself
     */
    public WebappDirectory() {
        this(null);
    }

    /**
     * A base location that is a path within the WEB-INF directory.
     * 
     * @param path
     *            The relative path within the WEB-INF directory.
     */
    public WebappDirectory(String path) {
        String newPath = "WEB-INF/";
        if (path != null) {
            newPath = newPath + path;
            if (!newPath.endsWith("/")) {
                newPath += "/";
            }
        }
        this.path = newPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        if (!ClassUtils.isPresent("org.springframework.web.context.WebApplicationContext", this.getClass().getClassLoader())) {
            return new UnresolvableResource("Not a webapp");
        }
        if (applicationContext instanceof org.springframework.web.context.WebApplicationContext == false) {
            return new UnresolvableResource("Web context not available");
        }
        
        Resource resource = applicationContext.getResource(path);
        if (!resource.exists()) {
            resource = new UnresolvableResource("Webapp path '%s' not found", path);
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
        return "Webapp directory";
    }

    /**
     * Retrieve the sub-path within the WEB-INF directory.
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    
}
