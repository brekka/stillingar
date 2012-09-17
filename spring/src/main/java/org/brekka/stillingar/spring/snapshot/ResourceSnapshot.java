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
import java.net.URI;
import java.util.Date;

import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.springframework.core.io.Resource;

/**
 * A snapshot of a {@link Resource}, retaining the moment in time the contents of the mutable resource were parsed and
 * loaded in the the {@link ConfigurationSource}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class ResourceSnapshot implements Snapshot {

    /**
     * The source representation of the resource.
     */
    private final ConfigurationSource configurationSource;

    /**
     * Value of the resource's last modified timestamp at the moment this snapshot was created.
     */
    private final Date timestamp;

    /**
     * A reference to the underlying (mutable) resource
     */
    private final Resource resource;

    /**
     * 
     * @param configurationSource The source representation of the resource.
     * @param timestamp Value of the resource's last modified timestamp at the moment this snapshot was created.
     * @param resource A reference to the underlying (mutable) resource
     */
    public ResourceSnapshot(ConfigurationSource configurationSource, Date timestamp, Resource resource) {
        this.configurationSource = configurationSource;
        this.timestamp = timestamp;
        this.resource = resource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.snapshot.Snapshot#getSource()
     */
    public ConfigurationSource getSource() {
        return configurationSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.snapshot.Snapshot#getTimestamp()
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.snapshot.Snapshot#getLocation()
     */
    @Override
    public URI getLocation() {
        try {
            return getResource().getURI();
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Failed to get location from resource '%s'", getResource()));
        }
    }

    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s[%s|%3$tF %3$tT]", 
                getClass().getSimpleName(), resource.toString(), new Date(lastModified()));
    }

    /**
     * @param lastModified
     * @return
     */
    private long lastModified() {
        try {
            return resource.lastModified();
        } catch (IOException e) {
            return 0;
        }
    }

}
