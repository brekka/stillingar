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

package org.brekka.stillingar.spring.snapshot;

import java.io.IOException;

import org.brekka.stillingar.api.ConfigurationException;
import org.springframework.core.io.Resource;

/**
 * Determines whether a resource has changed based on the lastModified date.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PollingResourceMonitor implements ResourceMonitor {

    /**
     * The resource being inspected.
     */
    private Resource resource;
    
    /**
     * The lastModified of the resource the last time hasChanged was called.
     */
    private long previousPoolLastModfied;
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.snapshot.ResourceMonitor#initialize(org.springframework.core.io.Resource)
     */
    @Override
    public void initialise(Resource resource) {
        this.resource = resource;
        this.previousPoolLastModfied = lastModified();
    }


    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.snapshot.ResourceMonitor#hasChanged()
     */
    @Override
    public boolean hasChanged() {
        long resourceLastModified = lastModified();
        if (resourceLastModified <= previousPoolLastModfied) {
            // Resource has not changed
            return false;
        }
        this.previousPoolLastModfied = resourceLastModified;
        return true;
    }

    /**
     * @param resourceLastModified
     * @return
     */
    private long lastModified() {
        try {
            return resource.lastModified();
        } catch (IOException e) {
            throw new ConfigurationException(String.format(
                    "Unable to determine the last modified for the resource '%s'", resource));
        }
    }

}
