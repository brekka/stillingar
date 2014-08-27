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

/**
 * Load resource from a provided path
 *
 * @author Ben.Gilbert
 */
public class PathDirectory implements BaseDirectory, ApplicationContextAware {

    /**
     * The path.
     */
    private final String path;
    
    /**
     * Application context, if available
     */
    private ApplicationContext applicationContext;

    /**
     * The directory itself
     */
    public PathDirectory() {
        this(null);
    }

    /**
     * A path (either relative to the application or an absolute path)
     * 
     * @param path
     *            The path
     */
    public PathDirectory(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        this.path = path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        Resource resource = applicationContext.getResource(path);
        if (!resource.exists()) {
            resource = new UnresolvableResource("Path '%s' not found", path);
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
        return "Path directory";
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
